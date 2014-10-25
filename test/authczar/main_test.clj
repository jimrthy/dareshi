(ns dareshi.main-test
  (:require [midje.sweet :refer :all]
            [dareshi.main :refer :all]))

(facts "Can load namespace OK"
       (+ 1 1) => truthy)
