(ns polylith.clj.core.entity.core
  (:require [polylith.clj.core.entity.dep-extractor :as dep-extractor]
            [polylith.clj.core.entity.path-extractor :as path-extractor]
            [polylith.clj.core.entity.profile-src-splitter :as profile-src-splitter]))

(defn path-entries [ws-dir dev? src-paths test-paths settings]
  (let [{:keys [profile-src-paths profile-test-paths]} (profile-src-splitter/extract-paths dev? settings)]
    (path-extractor/path-entries ws-dir src-paths test-paths profile-src-paths profile-test-paths)))

(defn deps-entries [dev? src-deps test-deps settings]
  (let [profile-deps (profile-src-splitter/extract-deps dev? settings)]
    (dep-extractor/dep-entries src-deps test-deps profile-deps)))
