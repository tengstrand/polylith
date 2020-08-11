(ns polylith.clj.core.workspace-clj.profile
  (:require [clojure.string :as str]
            [polylith.clj.core.util.interfc :as util]))

(defn profile [[profile-key {:keys [extra-paths extra-deps]}]]
  [(keyword (subs (name profile-key) 4))
   (util/ordered-map :paths extra-paths
                     :deps extra-deps)])

(defn profile->settings [aliases]
  (into {} (map profile
                (filterv #(str/starts-with? (-> % first name) "dev+") aliases))))