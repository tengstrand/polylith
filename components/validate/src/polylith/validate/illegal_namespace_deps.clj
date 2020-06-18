(ns polylith.validate.illegal-namespace-deps
  (:require [polylith.deps.interface :as deps]))

(defn error-message [{:keys [namespace depends-on-interface depends-on-ns]} brick-name type]
  (when namespace
    (str "Illegal dependency on namespace '" depends-on-interface "." depends-on-ns "' in namespace '" namespace "' in the '" brick-name "' " type
         ". Use '" depends-on-interface ".interface' instead to fix the problem.")))

(defn brick-errors [top-ns {:keys [name interface type namespaces-src]} interface-names errors]
  "Checks for dependencies to component interface namespaces other than 'interface'."
  (let [interface-name (:name interface)
        dependencies (deps/brick-dependencies top-ns interface-name interface-names namespaces-src)
        error-messages (filterv identity (map #(error-message % name type)
                                              (filterv #(not= "interface" (:depends-on-ns %)) dependencies)))]
    (vec (concat errors error-messages))))

(defn errors [top-ns interface-names components bases]
  (vec (mapcat #(brick-errors top-ns % interface-names [])
               (concat components bases))))
