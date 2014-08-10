(ns dareshi.realm-test
  (:require [com.stuartsierra.component :as component]
            [dareshi.realm :as realm]
            [dareshi.system :as sys])
  (:use midje.sweet))

(comment (let [base-system (sys/new-production-system nil)
               system (atom (into base-system {:database-protocol "mem"
                                               :database-address "localhost"
                                               :database-collection "test-auth"
                                               :database-port 4335}))]
           (with-state-changes [(before :facts (swap! system component/start))
                                (after :facts (swap! system component/stop))]
             ;; TODO: Write more facts!
             (fact "Setup/teardown works")
             )))
