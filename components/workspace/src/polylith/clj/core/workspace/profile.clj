(ns polylith.clj.core.workspace.profile
  (:require [polylith.clj.core.util.interfc :as util]))

(defn- with-deps [deps profile-deps]
  (merge deps (util/stringify-and-sort-map profile-deps)))

(defn lib-deps [dev? deps active-dev-profiles profile->settings]
  (if dev?
    (reduce #(with-deps %1 (-> %2 profile->settings :deps))
            deps
            active-dev-profiles)
    deps))

(defn- with-paths [path? paths profile-paths]
  (concat paths (filter path? profile-paths)))

(defn select-src-paths [path-key dev? paths active-dev-profiles profile->settings]
  (if dev?
    (vec (sort (reduce #(concat %1 (-> %2 profile->settings path-key))
                       paths
                       active-dev-profiles)))
    paths))

(defn src-paths [dev? paths active-dev-profiles profile->settings]
  (select-src-paths :src-paths dev? paths active-dev-profiles profile->settings))

(defn test-paths [dev? paths active-dev-profiles profile->settings]
  (select-src-paths :test-paths dev? paths active-dev-profiles profile->settings))
