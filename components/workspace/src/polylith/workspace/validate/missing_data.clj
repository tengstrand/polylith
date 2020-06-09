(ns polylith.workspace.validate.missing-data
  (:require [clojure.set :as set]
            [clojure.string :as str]))

(defn ->data-ifc [{:keys [declarations]}]
  (set (filter #(= 'data (:type %)) declarations)))

(defn component-errors [interface component]
  (let [data-interface (->data-ifc interface)
        comp-interface (->data-ifc (:interface component))
        missing-data-defs (set/difference data-interface comp-interface)
        data-defs (str/join ", " (map :name missing-data-defs))]
    (when (-> missing-data-defs empty? not)
      [(str "Missing data definition in the " (:name component) " component: " data-defs)])))

(defn interface-errors [{:keys [implemented-by] :as interface} name->component]
  (let [ifc-components (map name->component implemented-by)]
    (mapcat #(component-errors interface %) ifc-components)))

(defn errors [interfaces components]
  (let [name->component (into {} (map (juxt :name identity) components))]
    (mapcat #(interface-errors % name->component) interfaces)))