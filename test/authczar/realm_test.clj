(ns authczar.realm-test
  (:require [clojure.pprint :refer (pprint)]
            [com.stuartsierra.component :as component]
            [authczar.realm :as realm]
            [authczar.system :as sys])
  (:use midje.sweet))

(let [base-system (sys/new-production-system nil)
      system (atom (into base-system {:database-connection-description {:protocol "mem"
                                                                        :address "localhost"
                                                                        :collection "test-auth"
                                                                        :port 4335}}))]
  (with-state-changes [(before :facts (swap! system component/start))
                       (after :facts (swap! system component/stop))]
    ;; This is more a matter of checking my basic understanding of how the pieces
    ;; fit together than anything useful.
    ;; Have to start with baby steps.
    (fact "Setup/teardown works")
    (fact "Can add a user and query for permissions"
          false => truthy)
    ;; TODO: Write more facts!
    ))
