(ns polylith.clj.cli.poly
  (:require [polylith.clj.cli.cmd.check :as check]
            [polylith.clj.cli.cmd.help :as help]
            [polylith.clj.cli.cmd.ws :as info]
            [polylith.clj.test-runner.interfc :as test-runner]
            [polylith.clj.workspace-clj.interfc :as ws-clj]
            [polylith.core.change.interfc :as change]
            [polylith.core.file.interfc :as file]
            [polylith.core.workspace.interfc :as ws])
  (:gen-class))

(defn -main [& [cmd arg]]
  (let [ws-path (file/absolute-path "")
        workspace (-> ws-path
                      ws-clj/workspace-from-disk
                      ws/enrich-workspace
                      change/with-changes)
        color-mode (-> workspace :settings :color-mode)]
    (try
      (case cmd
        "check" (check/execute workspace)
        "help" (help/execute color-mode)
        "test" (test-runner/run workspace arg)
        "ws" (info/execute workspace arg)
        (help/execute color-mode))
      (catch Exception e
        (println (or (-> e ex-data :err) (.getMessage e)))
        (System/exit (or (-> e ex-data :exit-code) 1)))
      (finally
        (System/exit 0)))))
