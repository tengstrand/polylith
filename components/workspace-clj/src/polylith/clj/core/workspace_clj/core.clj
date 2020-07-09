(ns polylith.clj.core.workspace-clj.core
  (:require [clojure.string :as str]
            [polylith.clj.core.common.interfc :as common]
            [polylith.clj.core.file.interfc :as file]
            [polylith.clj.core.util.interfc :as util]
            [polylith.clj.core.workspace-clj.environment :as env]
            [polylith.clj.core.workspace-clj.components-from-disk :as components-from-disk]
            [polylith.clj.core.workspace-clj.bases-from-disk :as bases-from-disk]))

(def ws-reader
  {:name "polylith-clj"
   :project-url "https://github.com/tengstrand/polylith/tree/core"
   :reader-version "1.0"
   :ws-contract-version "1.0"
   :language "Clojure"
   :type-position "postfix"
   :slash "/"
   :file-extensions [".clj" "cljc"]})

(defn workspace-from-disk
  ([ws-path]
   (let [config (read-string (slurp (str ws-path "/deps.edn")))]
     (workspace-from-disk ws-path config)))
  ([ws-path {:keys [polylith]}]
   (let [{:keys [top-namespace color-mode env-short-names]} polylith
         top-ns (common/top-namespace top-namespace)
         top-src-dir (str/replace top-ns "." "/")
         component-names (file/directory-paths (str ws-path "/components"))
         components (components-from-disk/read-components ws-path top-src-dir component-names)
         bases (bases-from-disk/read-bases ws-path top-src-dir)
         environments (env/environments ws-path)
         settings (util/ordered-map :top-namespace top-namespace
                                    :color-mode color-mode
                                    :env-short-names env-short-names)]
     (util/ordered-map :ws-path ws-path
                       :ws-reader ws-reader
                       :settings settings
                       :components components
                       :bases bases
                       :environments environments))))