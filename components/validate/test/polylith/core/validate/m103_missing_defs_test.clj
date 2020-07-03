(ns polylith.core.validate.m103-missing-defs-test
  (:require [clojure.test :refer :all]
            [polylith.core.util.interfc.color :as color]
            [polylith.core.validate.m103-missing-defs :as m103]))

(def interfaces [{:name "auth"
                  :definitions [{:name "add-two", :type "function", :parameters ["x"]}]
                  :implementing-components ["auth"]}
                 {:name "invoice"
                  :type "interface"
                  :definitions [{:name "abc" :type "data"}
                                {:name "func1", :type "function", :parameters ["a"]}
                                {:name "func1", :type "function", :parameters ["b"]}
                                {:name "func1", :type "function", :parameters ["a" "b"]}
                                {:name "func1", :type "function", :parameters ["x" "y"]}]
                  :implementing-components ["invoice" "invoice2"]}
                 {:name "payFment"
                  :definitions [{:name "pay", :type "function", :parameters ["a"]}
                                {:name "pay", :type "function", :parameters ["b"]}]
                  :implementing-components ["payment"]}
                 {:name "user"
                  :type "interface"
                  :definitions [{:name "data1" :type "data"}
                                {:name "func1", :type "function", :parameters []}
                                {:name "func2", :type "function", :parameters ["a" "b"]}
                                {:name "func2", :type "function", :parameters ["x" "y"]}
                                {:name "func3", :type "function", :parameters ["a" "b" "c"]}
                                {:name "func3", :type "function", :parameters ["x" "y" "z"]}
                                {:name "func4", :type "function", :parameters [] :sub-ns "subns"}
                                {:name "func5", :type "function", :parameters ["a" "b" "c" "d"]}]
                  :implementing-components ["user1" "user2"]}])

(def components [{:name "auth"
                  :type "component"
                  :interface {:name "auth",
                              :definitions [{:name "add-two", :type "function", :parameters ["x"]}]}}
                 {:name "invoice"
                  :type "component"
                  :interface {:name "invoice"
                              :definitions [{:name "abc" :type "data"}
                                            {:name "func1", :type "function", :parameters ["a"]}
                                            {:name "func1", :type "function", :parameters ["a" "b"]}]}}
                 {:name "invoice2"
                  :type "component"
                  :interface {:name "invoice"
                              :definitions [{:name "func1", :type "function", :parameters ["b"]}
                                            {:name "func1", :type "function", :parameters ["x" "y"]}]}}
                 {:name "payment"
                  :type "component"
                  :interface {:name "payment"
                              :definitions [{:name "pay", :type "function", :parameters ["a"]}
                                            {:name "pay", :type "function", :parameters ["b"]}]}}
                 {:name "user1"
                  :type "component"
                  :interface {:name "user"
                              :definitions [{:name "data1" :type "data"}
                                            {:name "func1", :type "function", :parameters []}
                                            {:name "func2", :type "function", :parameters ["a" "b"]}
                                            {:name "func3", :type "function", :parameters ["a" "b" "c"]}
                                            {:name "func4", :type "function", :parameters [] :sub-ns "subns"}
                                            {:name "func5", :type "function", :parameters ["a" "b" "c" "d"]}]}}
                 {:name "user2"
                  :type "component"
                  :interface {:name "user"
                              :definitions [{:name "func2", :type "function", :parameters ["x" "y"]}
                                            {:name "func3", :type "function", :parameters ["x" "y" "z"]}
                                            {:name "func5", :type "function", :parameters ["a" "b" "c" "d"]}]}}])

(deftest errors--when-having-a--component-with-missing-definitionss--return-error-message
  (is (= [{:type "error",
           :code 103,
           :colorized-message "Missing definitions in invoice2's interface: abc",
           :message           "Missing definitions in invoice2's interface: abc",
           :components ["invoice2"]}
          {:type "error",
           :code 103,
           :colorized-message "Missing definitions in user2's interface: data1, func1[], subns/func4[]",
           :message "Missing definitions in user2's interface: data1, func1[], subns/func4[]",
           :components ["user2"]}]
         (m103/errors interfaces components color/none))))
