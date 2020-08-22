(ns polylith.clj.core.workspace.text-table.env-table
  (:require [polylith.clj.core.workspace.text-table.shared :as shared]
            [polylith.clj.core.workspace.text-table.profile :as profile]
            [polylith.clj.core.text-table.interfc :as text-table]
            [polylith.clj.core.util.interfc.color :as color]
            [polylith.clj.core.path-finder.interfc.extract :as extract]
            [polylith.clj.core.path-finder.interfc.status :as status]))

(defn profile-cell [index env-name column show-resources? path-entries]
  (let [flags (status/env-status-flags path-entries env-name show-resources?)]
    (shared/standard-cell flags column (+ index 3) :purple :center)))

(defn profile-col [index profile ws-dir start-column settings environments show-resources?]
  (let [column (+ start-column (* 2 index))
        path-entries (extract/from-profiles-paths ws-dir settings profile)]
    (concat [(text-table/cell column 1 profile :purple :center :horizontal)]
            (map-indexed #(profile-cell %1 %2 column show-resources? path-entries)
                         (map :name environments)))))
(defn profile-columns [ws-dir start-column environments profiles settings show-resources?]
  (apply concat
         (map-indexed #(profile-col %1 %2 ws-dir start-column settings environments show-resources?)
                      profiles)))

(defn env-cell [environment env-key column row changed-environments color-mode]
  (let [name (env-key environment)
        changed (if (contains? (set changed-environments) name) " *" "")
        env (str (color/environment name color-mode)
                 changed)]
    (shared/standard-cell env column row :none)))

(defn env-column [environments {:keys [changed-environments]} header env-key column color-mode]
  (concat [(shared/header header column)]
          (map-indexed #(env-cell %2 env-key column (+ %1 3) changed-environments color-mode)
                       environments)))

(defn src-cell [index {:keys [name src-paths test-paths profile-src-paths profile-test-paths]} ws-dir environments-to-test show-resources?]
  (let [path-entries (extract/path-entries ws-dir [src-paths, test-paths profile-src-paths profile-test-paths])
        satus-flags (str (status/env-status-flags path-entries name show-resources?)
                         (if (contains? (set environments-to-test) name) "x" "-"))]
    (shared/standard-cell satus-flags 5 (+ index 3) :purple :center)))

(defn src-column [ws-dir environments {:keys [environments-to-test]} show-resources?]
  (concat [(shared/header "source" 5)]
          (map-indexed #(src-cell %1 %2 ws-dir environments-to-test show-resources?)
                       environments)))

(defn loc-cell [index lines-of-code column thousand-sep]
  (shared/number-cell lines-of-code column (+ index 3) :right thousand-sep))

(defn loc-columns [show-loc? environments n#profiles thousand-sep total-col-src total-loc-test]
  (when show-loc?
    (let [column1 (+ 7 (* 2 n#profiles))
          column2 (+ 2 column1)]
      (concat [(shared/header "loc" column1 :none :right)]
              (map-indexed #(loc-cell %1 %2 column1 thousand-sep) (map :lines-of-code-src environments))
              [(shared/number-cell total-col-src column1 (+ (count environments) 3) :right thousand-sep)]
              [(shared/header "(t)" column2 :none :right)]
              (map-indexed #(loc-cell %1 %2 column2 thousand-sep) (map :lines-of-code-test environments))
              [(shared/number-cell total-loc-test column2 (+ (count environments) 3) :right thousand-sep)]))))

(defn table [{:keys [ws-dir settings environments changes]} show-loc? show-resources?]
  (let [{:keys [color-mode thousand-sep]} settings
        profiles (profile/all-profiles settings)
        n#profiles (count profiles)
        total-loc-src (apply + (filter identity (map :lines-of-code-src environments)))
        total-loc-test (apply + (filter identity (map :lines-of-code-test environments)))
        env-col (env-column environments changes "environment" :name 1 color-mode)
        alias-col (env-column environments {} "alias" :alias 3 color-mode)
        src-col (src-column ws-dir environments changes show-resources?)
        profile-cols (profile-columns ws-dir 7 environments profiles settings show-resources?)
        loc-col (loc-columns show-loc? environments n#profiles thousand-sep total-loc-src total-loc-test)
        space-columns (range 2 (* 2 (+ 3 (count profiles) (if show-loc? 2 0))) 2)
        header-spaces (text-table/spaces 1 space-columns (repeat "  "))
        cells (text-table/merge-cells env-col alias-col src-col loc-col profile-cols header-spaces)
        section-cols (if (or show-loc? (-> n#profiles zero? not)) [6 (+ 6 (* 2 n#profiles))] [])
        line-spaces (text-table/spaces 2 section-cols (repeat "   "))
        line (text-table/line 2 cells)]
    (text-table/table "  " color-mode cells line line-spaces)))

(defn print-table [workspace show-loc? show-resources?]
  (text-table/print-table (table workspace show-loc? show-resources?)))
