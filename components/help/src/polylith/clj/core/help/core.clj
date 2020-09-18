(ns polylith.clj.core.help.core
  (:require [polylith.clj.core.help.check :as check]
            [polylith.clj.core.help.create :as create]
            [polylith.clj.core.help.deps :as deps]
            [polylith.clj.core.help.info :as info]
            [polylith.clj.core.help.test :as test]
            [polylith.clj.core.help.summary :as summary]))

(defn print-help [cmd ent color-mode]
  (case cmd
    "check" (check/print-help color-mode)
    "create" (println "  Not implemented yet!")
    "deps" (println "  Not implemented yet!")
    "diff" (println "  Not implemented yet!")
    "info" (println "  Not implemented yet!")
    "libs" (println "  Not implemented yet!")
    "test" (println "  Not implemented yet!")
    "ws" (println "  Not implemented yet!")
    (summary/print-help)))
