(ns bombaywalla.vozi-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.set :as cset]
            [bombaywalla.vozi :as sut]))

(deftest aggregates-test
  (testing "Check to see that both types of aggregate transforms are the same modulo the name."
    (let [base-plot (sut/line-plot)
          op "max"
          field "X"
          as-field "maxX"
          aggregate (sut/aggregate-transform base-plot op field as-field)
          joinaggregate (sut/joinaggregate-transform base-plot op field as-field)
          xfm-aggregate (update-in joinaggregate
                                   [:transform 0]
                                   cset/rename-keys
                                   {:joinaggregate :aggregate})
          ]
      (is (= aggregate xfm-aggregate)))))
