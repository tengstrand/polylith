(ns polylith.clj.core.change.core
  (:require [polylith.clj.core.change.entity :as entity]
            [polylith.clj.core.change.indirect :as indirect]
            [polylith.clj.core.change.to-test :as to-test]
            [polylith.clj.core.util.interfc :as util]
            [polylith.clj.core.git.interfc :as git]
            [polylith.clj.core.util.interfc :as util]))

(defn changed-files-info [ws-dir sha1 sha2]
  "Returns changed files.
    - if none of 'sha1' or 'sha2' is set: diff against local changes
    - if only one of 'sha1' or 'sha2' is set: diff between the SHA and HEAD
    - if both 'sha1' and 'sha2' is set: diff changes between sha1 and sha2"
    (util/ordered-map :sha1 sha1
                      :sha2 sha2
                      :files (git/diff ws-dir sha1 sha2)))

(defn changes [{:keys [environments]}
               {:keys [sha1 sha2 files]}
               {:keys [run-all? run-env-tests?] :as user-input}]
   (let [deps (map (juxt :name :deps) environments)
         {:keys [changed-components
                 changed-bases
                 changed-environments]} (entity/changed-entities files)
         changed-bricks (set (concat changed-components changed-bases))
         env->indirect-changes (indirect/env->indirect-changes deps changed-bricks)
         env->bricks-to-test (to-test/env->bricks-to-test environments changed-components changed-bases env->indirect-changes run-all?)
         environments-to-test (to-test/environments-to-test environments changed-bricks changed-environments run-env-tests?)]
     (util/ordered-map :sha1 sha1
                       :sha2 sha2
                       :git-command (git/diff-command sha1 sha2)
                       :user-input user-input
                       :changed-components changed-components
                       :changed-bases changed-bases
                       :changed-environments changed-environments
                       :env->indirect-changes env->indirect-changes
                       :env->bricks-to-test env->bricks-to-test
                       :environments-to-test environments-to-test
                       :changed-files files)))

(defn with-changes
  ([{:keys [ws-dir] :as workspace} user-input]
   (with-changes workspace (changed-files-info ws-dir "HEAD" nil) user-input))
  ([workspace changes-info user-input]
   (assoc workspace :changes (changes workspace changes-info user-input))))
