(ns polylith.clj.core.change.core
  (:require [polylith.clj.core.change.entity :as entity]
            [polylith.clj.core.change.indirect :as indirect]
            [polylith.clj.core.change.to-test :as to-test]
            [polylith.clj.core.util.interfc :as util]
            [polylith.clj.core.git.interfc :as git]
            [polylith.clj.core.util.interfc :as util]))

(defn changed-files-info [ws-path sha1 sha2]
  "Returns changed files.
    - if none of 'sha1' or 'sha2' is set: diff against local changes
    - if only one of 'sha1' or 'sha2' is set: diff between the SHA and HEAD
    - if both 'sha1' and 'sha2' is set: diff changes between sha1 and sha2"
    (util/ordered-map :sha1 sha1
                      :sha2 sha2
                      :files (git/diff ws-path sha1 sha2)))

(defn changes [{:keys [environments]}
               {:keys [sha1 sha2 files]}]
   (let [deps (map (juxt :name :deps) environments)
         {:keys [changed-components
                 changed-bases
                 changed-environments]} (entity/changed-entities files)
         changed-bricks (set (concat changed-components changed-bases))
         indirect-changes (indirect/indirect-changes deps changed-bricks)
         bricks-to-test (to-test/bricks-to-test environments changed-components changed-bases indirect-changes)
         environments-to-test (to-test/environments-to-test environments changed-bricks changed-environments)]
     (util/ordered-map :sha1 sha1
                       :sha2 sha2
                       :git-command (git/diff-command sha1 sha2)
                       :changed-components changed-components
                       :changed-bases changed-bases
                       :changed-environments changed-environments
                       :indirect-changes indirect-changes
                       :bricks-to-test bricks-to-test
                       :environments-to-test environments-to-test
                       :changed-files files)))

(defn with-changes
  ([{:keys [ws-path] :as workspace}]
   (with-changes workspace (changed-files-info ws-path "HEAD" nil)))
  ([workspace changes-info]
   (assoc workspace :changes (changes workspace changes-info))))
