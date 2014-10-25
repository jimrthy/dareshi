(ns authczar.main
  "Main entry point.

Using this as a library seems problematic. But the basic idea
probably makes sense."
  (:require clojure.pprint)
  (:gen-class))

;; We wrap the system in a system wrapper so that we can define a
;; print-method that will avoid recursion.
(defrecord SystemWrapper [p]
  clojure.lang.IDeref
  (deref [this] (deref p))
  clojure.lang.IFn
  (invoke [this a] (p a)))

(defmethod print-method SystemWrapper [_ writer]
  (.write writer "#system \"<system>\""))

(defmethod print-dup SystemWrapper [_ writer]
  (.write writer "#system \"<system>\""))

(.addMethod clojure.pprint/simple-dispatch SystemWrapper
   (fn [x]
     (print-method x *out*)))

(defn new-system-wrapper []
  (->SystemWrapper (promise)))

(defn -main [& args]
  ;; We eval so that we don't AOT anything beyond this class
  (eval '(do (require 'authczar.system)
             (require 'authczar.main)
             (require 'com.stuartsierra.component)

             (println "Starting reusable.components")

             (let [systemref (authczar.main/new-system-wrapper)
                   system (com.stuartsierra.component/start
                           (authczar.system/new-production-system systemref))]

               (deliver systemref system)

               (println "System started")
               (println "Ready...")))))
