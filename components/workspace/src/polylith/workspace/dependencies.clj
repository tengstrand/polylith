(ns polylith.workspace.dependencies
  (:require [clojure.tools.deps.alpha :as tools-deps]
            [polylith.workspace.environment :as env]))

(defn select-deps [{:keys [environments]} env include-tests? additional-deps]
  (let [envs (env/select environments env include-tests?)]
    (merge additional-deps
           (into (sorted-map) (mapcat :deps envs)))))

(defn resolve-libs [workspace env include-tests? additional-deps]
  (let [deps (select-deps workspace env include-tests? additional-deps)]
    (tools-deps/resolve-deps workspace {:extra-deps deps})))