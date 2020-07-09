(ns polylith.clj.core.validate.m104-circular-deps
  (:require [clojure.string :as str]
            [polylith.clj.core.util.interfc :as util]
            [polylith.clj.core.util.interfc.color :as color]))

(defn interface-circular-deps [interface-name completed-deps interface->deps path]
  (if (contains? completed-deps interface-name)
    {:error (conj path interface-name)}
    (mapcat #(interface-circular-deps % (conj completed-deps interface-name) interface->deps (conj path interface-name))
            (interface->deps interface-name))))

(defn component-circular-deps [{:keys [name]} env-name interface->deps iname->component-name color-mode]
  (let [deps-chain (map iname->component-name
                        (-> (interface-circular-deps name #{} interface->deps [])
                            first second))
        message (str "A circular dependency was found in the " env-name " environment: " (str/join " > " deps-chain))
        colorized-msg (str "A circular dependency was found in the " (color/environment env-name color-mode) " environment: " (color/interface (str/join " > " deps-chain) color-mode))]
    (when (-> deps-chain empty? not)
      [(util/ordered-map :type "error"
                         :code 104
                         :message message
                         :colorized-message colorized-msg
                         :components (vec (sort (set deps-chain)))
                         :environment env-name)])))

(defn environment-circular-deps [{:keys [name component-names]} interfaces components color-mode]
  "Calculates circular dependencies for an environment."
  (let [cname->interface-deps (into {} (map (juxt :name :interface-deps) components))
        cname->interface-name (into {} (map (juxt :name #(-> % :interface :name)) components))
        iname->component-name (into {} (map (juxt #(-> % :interface :name) :name)
                                            (filter #(contains? (set component-names) (:name %))
                                                    components)))
        interface->deps (into {} (map (juxt cname->interface-name
                                            cname->interface-deps)
                                      component-names))
        circular-deps (mapcat #(component-circular-deps % name interface->deps iname->component-name color-mode)
                              interfaces)]
    (when circular-deps
      circular-deps)))

(defn errors [interfaces components environments color-mode]
  (when-let [errors (first
                      (sort-by #(-> % :bricks count)
                        (mapcat #(environment-circular-deps % interfaces components color-mode)
                                (filter #(-> % :test? not)
                                        environments))))]
    [errors]))