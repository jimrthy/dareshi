(ns authczar.main-test
  (:require [midje.sweet :refer :all]
            [authczar.main :refer :all]))

(facts "Can load namespace OK"
       (+ 1 1) => truthy)
