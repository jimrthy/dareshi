(ns dareshi.realm-test
  (:require [clojure.pprint :refer (pprint)]
            [clojure.test :refer (are deftest is testing use-fixtures)]
            [com.stuartsierra.component :as component]
            [dareshi.realm :as realm]
            [dareshi.system :as sys]))

(def system (atom nil))

(defn system-fixture [f]
  (let [base-system (sys/new-production-system nil)
        testing-system (into base-system {:database-connection-description {:protocol "mem"
                                                                            :address "localhost"
                                                                            :collection (name (gensym))
                                                                            :port 4335}})]
    (reset! system (component/start testing-system))
    (try
      (f)
      (finally
        (component/stop @system)
        (reset! system nil)))))

(use-fixtures :each system-fixture)

(deftest loading []
  (testing "Can we start/stop a system?"
    (is (= 1 1) "Setup/teardown work")))

(deftest user-creation []
  (testing "I don't think this belongs here"
    (is (= true false) "If it does, how should it work?")))
