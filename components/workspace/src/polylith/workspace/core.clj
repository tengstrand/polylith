(ns polylith.workspace.core
  (:require [polylith.workspace-clj.interface :as ws-clj]
            [polylith.workspace.dependencies :as deps]
            [polylith.file.interface :as file]))

;(ws-clj/read-workspace-from-disk "." {:polylith {:top-namespace "polylith"}})
;(ws-clj/read-workspace-from-disk "../clojure-polylith-realworld-example-app")
;(ws-clj/read-workspace-from-disk "../Nova/project-unicorn" {:polylith {:top-namespace ""}})

(def workspace (ws-clj/read-workspace-from-disk "../clojure-polylith-realworld-example-app"))
(def components (:components workspace))
(def interface-names (vec (sort (map #(-> % :interface :name) components))))
(def component (nth components 5))
(def imports (:imports component))

(def acomponent (assoc component :messages {:errors ["err1"] :warnings ["w"]}))

(deps/with-dependencies "clojure.realworld." component interface-names)
(deps/with-dependencies "clojure.realworld." acomponent interface-names)

