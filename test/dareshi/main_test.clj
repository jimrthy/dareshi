(ns authczar.main-test
  (:require [authczar.main :refer :all]
            [clojure.test :refer (are deftest is testing use-fixtures)]))

(deftest load
  ;; Really just testing that the namespace can load
  (testing "Fundamentals"
    (is (= 2 (+ 1 1)) "Math works")))
