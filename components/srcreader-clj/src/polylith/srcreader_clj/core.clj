(ns polylith.srcreader-clj.core
  (:require [clojure.string :as str]
            [polylith.file.interface :as file]
            [polylith.core.interface :as core]
            [polylith.srcreader-clj.readcomponentsfrom-disk :as componentsfromdisk]
            [polylith.srcreader-clj.readbasesfromdisk :as basesfromdisk]))

(defn top-namespace [{:keys [top-namespace]}]
  "Makes sure the top namespace ends with a dot (.) - if not empty."
  (if (str/blank? top-namespace)
    ""
    (if (str/ends-with? top-namespace ".")
      top-namespace
      (str top-namespace "."))))

(defn read-workspace-from-disk [ws-path {:keys [polylith] :as config}]
  (let [top-ns (top-namespace polylith)
        top-src-dir (str/replace top-ns "." "/")
        component-names (file/directory-paths (str ws-path "/components"))
        components (componentsfromdisk/read-components-from-disk ws-path top-src-dir component-names)
        bases (basesfromdisk/read-bases-from-disk ws-path top-src-dir)
        aliases (core/aliases config)]
        ;interface-names (vec (sort (map #(-> % :interface :name) components)))]
    ;messages (validate/error-messages interface-names components bases)]
    {:polylith polylith
     :components components
     :bases bases
     :aliases aliases}))
;:messages messages}))

;(read-workspace-from-disk "." {:polylith {:top-namespace "polylith"}})
;(read-workspace-from-disk "../clojure-polylith-realworld-example-app" {:polylith {:top-namespace "clojure.realworld"}})
;(read-workspace-from-disk "../Nova/project-unicorn" {:polylith {:top-namespace ""}})
