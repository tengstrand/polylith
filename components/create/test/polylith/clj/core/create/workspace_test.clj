(ns polylith.clj.core.create.workspace-test
  (:require [clojure.test :refer :all]
            [polylith.clj.core.test-helper.interfc :as helper]))

(use-fixtures :each helper/test-setup-and-tear-down)

(deftest create-workspace--trying-to-create-a-workspace-within-another-workspace--prints-out-error-messagex
  (let [output (with-out-str
                 (helper/execute-command "" "create" "workspace" "name:ws1" "top-ns:se.example")
                 (helper/execute-command "ws1" "create" "workspace" "name:ws2" "top-ns:com.example"))]
    (is (= "  A workspace should not be created within another workspace.\n"
           output))))

(deftest create-workspace--missing-top-namespace--prints-out-error-messagex
  (let [output (with-out-str
                 (helper/execute-command "" "create" "x" "name:ws1"))]
    (is (= "  Expected the first argument after 'create' to be any of: w, e, b, c, workspace, environment, base, component.\n"
           output))))

(deftest create-workspace--missing-top-namespace--prints-out-error-message
  (let [output (with-out-str
                 (helper/execute-command "" "create" "workspace" "name:ws1"))]
    (is (= "  A top namespace must be given, e.g.: create w name:my-workspace top-ns:com.my-company\n"
           output))))

(deftest create-workspace--creates-empty-directories-and-a-deps-edn-config-file
  (let [output (with-out-str
                 (helper/execute-command "" "create" "workspace" "name:ws1" "top-ns:se.example"))]
    (is (= ""
           output))

    (is (= #{"bases"
             "components"
             "development"
             "development/src"
             "environments"
             ".git"
             "logo.png"
             "deps.edn"
             "readme.md"}
           (helper/paths "ws1")))

    (is (= ["<img src=\"logo.png\" width=\"30%\" alt=\"Polylith\" id=\"logo\">"
            ""
            "The Polylith documentation can be found here:"
            ""
            "- The [high-level documentation](https://polylith.gitbook.io/polylith)"
            "- The [Polylith Tool documentation](https://github.com/tengstrand/polylith/tree/core)"
            "- The [RealWorld example app documentation](https://github.com/furkan3ayraktar/clojure-polylith-realworld-example-app)"
            ""
            "You can also get in touch with the Polylith Team via our [forum](https://polylith.freeflarum.com) or on [Slack](https://clojurians.slack.com/archives/C013B7MQHJQ)."
            ""
            "<h1>ws1</h1>"
            ""
            "<p>Add your workspace documentation here...</p>"]
           (helper/content "ws1" "readme.md")))

    (is (= [""
            "{:polylith {:vcs \"git\""
            "            :top-namespace \"se.example\""
            "            :interface-ns \"interface\""
            "            :env->alias {\"development\" \"dev\"}"
            "            :ns->lib {clojure             org.clojure/clojure"
            "                      clojure.tools.deps  org.clojure/tools.deps.alpha}}"
            ""
            " :aliases  {:dev {:extra-paths [\"development/src\"]"
            "                  :extra-deps {org.clojure/clojure {:mvn/version \"1.10.1\"}"
            "                               org.clojure/tools.deps.alpha {:mvn/version \"0.8.695\"}}}"
            ""
            "            :test {:extra-paths []}"
            ""
            "            :poly {:main-opts [\"-m\" \"polylith.clj.core.cli.poly\"]"
            "                   :extra-deps {tengstrand/polylith"
            "                                {:git/url   \"https://github.com/tengstrand/polylith.git\""
            "                                 :sha       \"e02a0794d26b0b111d84a36f6d48e9b1848e5913\""
            "                                 :deps/root \"environments/cli\"}}}}}"]
           (helper/content "ws1" "deps.edn")))

    (is (= ["{:color-mode \"dark\""
            " :thousand-separator \",\"}"]
           (helper/content (helper/user-home) "/.polylith/config.edn")))))
