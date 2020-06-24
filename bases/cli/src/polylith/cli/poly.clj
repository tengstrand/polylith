(ns polylith.cli.poly
  (:require [polylith.spec.interface :as spec]
            [polylith.test.interface :as test]
            [polylith.cli.cmd.check :as check]
            [polylith.cli.cmd.help :as help]
            [polylith.cli.cmd.print-ws :as print-ws]
            [polylith.workspace.interface :as ws]
            [polylith.workspace-clojure.interface :as ws-clj]
            [polylith.change.interface :as change]
            [clojure.java.io :as io])
  (:gen-class))

(defn -main [& [cmd env]]
  (let [ws-path (.getAbsolutePath (io/file ""))
        workspace (-> ws-path
                      ws-clj/workspace-from-disk
                      ws/enrich-workspace
                      change/with-changes)]
    (if-not (spec/valid-config? (:settings workspace))
      (println "Expected to find a :polylith key in 'deps.edn' at the root of the workspace.")
      (try
        (case cmd
          "check" (check/execute workspace)
          "help" (help/execute)
          "test" (test/run workspace env)
          "ws" (print-ws/execute workspace)
          (help/execute))
        (catch Exception e
          (println (or (-> e ex-data :err) (.getMessage e)))
          (System/exit (or (-> e ex-data :exit-code) 1)))
        (finally
          (System/exit 0))))))