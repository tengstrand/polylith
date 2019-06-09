(ns polylith.cmd.compile
  "Compile Clojure source into .class files."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [polylith.common :as common])
  (:refer-clojure :exclude [compile])
  (:import (java.io File)))

(defn delete-folder [file]
  (let [files (reverse (file-seq file))]
    (doseq [^File f files]
      (when (.exists f)
        (io/delete-file f)))))

(defn ensure-compile-folder [ws-path compile-path]
  (let [full-path      (str ws-path "/" compile-path)
        compile-folder (io/file full-path)]
    (delete-folder compile-folder)
    (.mkdir compile-folder)))

(defn file-path->ext [path]
  (let [last-dot  (str/last-index-of path ".")
        extension (if last-dot (subs path (inc last-dot)) "")]
    extension))

(def accepted-source-file-extensions #{"clj" "cljc"})

(defn file->compile-expr [src-path ^File file]
  (let [path (.getAbsolutePath file)
        ext  (file-path->ext path)]
    (when (and (.exists file)
               (not (.isDirectory file))
               (contains? accepted-source-file-extensions ext))
      (let [ns-str (-> path
                       (str/replace (str src-path "/") "")
                       (str/replace (str "." ext) "")
                       (str/replace "/" ".")
                       (str/replace "_" "-"))]
        (str "(clojure.core/compile (symbol \"" ns-str "\"))")))))

(defn compile-expressions [src-path]
  (->> src-path
       (io/file)
       (file-seq)
       (map #(file->compile-expr src-path %))
       (filter #(not (nil? %)))
       (str/join " ")))

(defn compilation-expr [compile-path src-path interface-expressions]
  (let [src (io/file src-path)]
    (when (or (not (.isDirectory src))
              (not (.exists src)))
      (throw (ex-info "Invalid source path." {:src-path src-path})))
    (let [expressions (str interface-expressions " " (compile-expressions src-path))]
      (str "(binding [*compile-path* \"" compile-path "\"]" expressions ")"))))

(defn compile-item [libraries ws-path compile-path interface-path interface-expressions item type]
  (let [compile-path (str ws-path "/" compile-path)
        item-path    (str ws-path "/" (if (= :base type) "bases" "components") "/" item "/src")
        source-paths [compile-path interface-path item-path]
        classpath    (common/make-classpath libraries source-paths)
        expression   (compilation-expr compile-path item-path interface-expressions)]
    (common/run-in-jvm classpath expression ws-path "Exception during compilation.")
    (println "Compiled" item)))

(defn compile-base [libraries ws-path compile-path interface-path interface-expressions base]
  (compile-item libraries ws-path compile-path interface-path interface-expressions base :base))

(defn compile-component [libraries ws-path compile-path interface-path interface-expressions component]
  (compile-item libraries ws-path compile-path interface-path interface-expressions component :component))

(defn compile [ws-path {:keys [polylith] :as deps} service-or-env]
  (let [{:keys [compile-path] :or {compile-path "target"}} polylith
        libraries             (common/resolve-libraries deps service-or-env)
        interface-path        (str ws-path "/interfaces/src")
        interface-expressions (compile-expressions interface-path)
        paths                 (when service-or-env (common/extract-source-paths ws-path deps service-or-env))
        _                     (when (= [] paths)
                                (throw (ex-info (str "No source paths found. Check service or environment name: " service-or-env)
                                                {:service-or-env service-or-env})))
        all-bases             (common/all-bases ws-path paths)
        all-components        (common/all-components ws-path paths)]
    (when-not (ensure-compile-folder ws-path compile-path)
      (throw (ex-info "Could not create compile folder." {:ws-path      ws-path
                                                          :compile-path compile-path})))
    (doseq [c all-components]
      (compile-component libraries ws-path compile-path interface-path interface-expressions c))
    (doseq [b all-bases]
      (compile-base libraries ws-path compile-path interface-path interface-expressions b))
    (println "\nCompilation successful.")))
