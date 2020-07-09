(ns polylith.clj.core.workspace.lib-imports
  (:require [clojure.string :as str]))

(defn library? [import top-ns interface-names]
  (if (str/starts-with? import top-ns)
    (let [interface-ns (subs import (count top-ns))
          index (str/index-of interface-ns ".")
          interface (if (< index 0)
                      interface-ns
                      (subs interface-ns 0 index))]
      (not (contains? interface-names interface)))
    true))

(defn lib-imports-src [top-ns interface-names brick]
  (vec (sort (filter #(library? % top-ns interface-names)
                     (set (mapcat :imports (:namespaces-src brick)))))))

(defn lib-imports-test [top-ns interface-names brick]
  (vec (sort (filter #(library? % top-ns interface-names)
                     (set (mapcat :imports (:namespaces-test brick)))))))