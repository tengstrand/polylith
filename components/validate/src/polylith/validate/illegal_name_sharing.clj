(ns polylith.validate.illegal-name-sharing
  (:require [clojure.set :as set]
            [clojure.string :as str]))

(defn errors [interface-names components bases]
  "Makes sure that a base cannot have the same name as an interface or component"
  (let [component-names (set (map :name components))
        component-and-interface-names (set/union interface-names component-names)
        base-names (set (map :name bases))
        shared-names (set/intersection component-and-interface-names base-names)]
    (if (empty? shared-names)
      []
      [(str "A Base can't have the same name as an interface or component: "
            (str/join ", " (sort shared-names)))])))