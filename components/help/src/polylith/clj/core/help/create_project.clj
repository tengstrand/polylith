(ns polylith.clj.core.help.create-project
  (:require [polylith.clj.core.help.shared :as s]))

(defn help-text [cm]
  (str "  Creates a project.\n"
       "\n"
       "  poly create p name:" (s/key "NAME" cm) "\n"
       "    " (s/key "NAME" cm) " = The name of the project to create.\n"
       "\n"
       "  Example:\n"
       "    poly create p name:myproject"))

(defn print-help [color-mode]
  (println (help-text color-mode)))
