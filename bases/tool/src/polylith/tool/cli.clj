(ns polylith.tool.cli
  (:require [clojure.java.io :as io]
            [polylith.cmd.interface :as cmd]
            [polylith.spec.interface :as spec]))

(def help-text
  (str
    "Allowed options:\n"
    "  compile [service-or-env] : Takes an optional service or environment name.\n"
    "                             If you run this without any arguments, it will\n"
    "                             compile all components and bases. If you give\n"
    "                             a specific environment or service name it will\n"
    "                             compile components and bases attached to that.\n"
    "\n"
    "  test service-or-env      : Runs tests for given service or environment.\n"
    "                             You can define test sources and dependencies\n"
    "                             under an alias with a namespace service.test/\n"
    "                             or env.test/. E.g., if you have a service with\n"
    "                             name 'backend' you can include :service/backend\n"
    "                             and :service.test/backend aliases in deps.edn\n"))

(defn -main [& [cmd env]]
  (let [ws-path (.getAbsolutePath (io/file ""))
        config (-> "deps.edn" slurp read-string)]
    (if-not (spec/valid-config? (:polylith config))
      (println "deps.edn file should contain a polylith config map with key :polylith.")
      (try
        (case cmd
          "compile" (cmd/compile ws-path config env)
          "test" (cmd/test ws-path config env)
          (println help-text))
        (catch Exception e
          (println (or (-> e ex-data :err) (.getMessage e)))
          (System/exit (or (-> e ex-data :exit-code) 1)))
        (finally
          (System/exit 0))))))
