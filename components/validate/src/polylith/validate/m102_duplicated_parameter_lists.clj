(ns polylith.validate.m102-duplicated-parameter-lists
  (:require [clojure.string :as str]
            [polylith.util.interface :as util]
            [polylith.util.interface.color :as color]
            [polylith.validate.shared :as shared]))

(defn duplicated-parameter-lists-error [component-name component-duplication]
  (let  [message (str "Duplicated parameter lists found in " component-name ": "
                      (str/join ", " (map shared/->function-or-macro component-duplication)))
         colorized-msg (str "Duplicated parameter lists found in " (color/component component-name) ": "
                            (str/join ", " (map shared/->function-or-macro component-duplication)))]
    (util/ordered-map :type "error"
                      :code 102
                      :message message
                      :colorized-message colorized-msg
                      :components [component-name])))

(defn component-errors [component]
  (let [component-name (:name component)
        component-id->function (-> component :interface shared/id->functions-or-macro)
        multi-id-functions (mapv second (filter #(> (-> % second count) 1) component-id->function))]
    (mapv #(duplicated-parameter-lists-error component-name %) multi-id-functions)))

(defn errors [components]
  (vec (mapcat component-errors components)))
