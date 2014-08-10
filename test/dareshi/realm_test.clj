(ns dareshi.realm-test
  (:require [clojure.pprint :refer (pprint)]
            [com.stuartsierra.component :as component]
            [dareshi.realm :as realm]
            [dareshi.system :as sys])
  (:use midje.sweet))

(comment) (let [base-system (sys/new-production-system nil)
                system (atom (into base-system {:database-connection-description {:protocol "mem"
                                                                                  :address "localhost"
                                                                                  :collection "test-auth"
                                                                                  :port 4335}}))]
            (println "Checking realm against " (with-out-str (pprint @system)))
            (with-state-changes [(before :facts (swap! system component/start))
                                 (after :facts (swap! system component/stop))]
              ;; TODO: Write more facts!
              (fact "Setup/teardown works")
              ))
