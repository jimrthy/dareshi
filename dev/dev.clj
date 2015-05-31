(ns dev
  (:require [dareshi.persistence :as db]
            [dareshi.system :as sys :refer (config new-base-system-map new-base-dependency-map)]
            [clojure.java.io :as io]
            [clojure.pprint :refer (pprint simple-dispatch)]
            [clojure.reflect :refer (reflect)]
            [clojure.repl :refer (apropos dir doc find-doc pst source)]
            [clojure.string :as s]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [com.stuartsierra.component :as component]
            [datomic.api :as d]
            [dev-components :refer (wrap-schema-validation)]
            [taoensso.timbre :as log]))

(def system nil)

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

(defn ensure-map
  "Turn vector style into map style, if necessary.
  For example: [:a :b :c] -> {:a :a, :b :b, :c :c}
  Shamelessly copied from juxt's modular.wire-up (MIT license)"
  [x]
  (if (sequential? x)
    (apply zipmap (repeat 2 x))
    x))

(defn normalize-dependency-map
  "component/using and system/using accept vectors as well as maps. This
  makes it difficult to process (merge, extract, etc.) dependency
  trees. Use this function to normalise so that only the map form is
  used.
  Shamelessly copied from juxt's modular.wire-up (MIT license)"
  [m]
  (reduce-kv
   (fn [s k v]
     (assoc s k (ensure-map v)))
   {} m))

(defn new-dev-system
  "Create a development system
Q: Do I really want to do this?"
  []
  (let [systemref (new-system-wrapper)
        s-map (assoc (new-base-system-map (config) systemref)
                :wrap-schema-validation wrap-schema-validation)
        ;; The next form causes a linter warning, since it isn't
        ;; doing anything.
        ;; I'm pretty sure the original intent was to merge
        ;; the base-dependency map into one that's suitable for
        ;; the REPL
        d-map (merge-with merge
                          (normalize-dependency-map
                           (new-base-dependency-map s-map)))]
    (with-meta
      (component/system-using s-map d-map)
      {:dependencies d-map
       :systemref systemref})))

(defn init
  "Constructs the current development system."
  []
  (alter-var-root #'system
    (constantly (new-dev-system))))

(defn start
  "Starts the current development system."
  []
  (alter-var-root
   #'system
   (fn [system]
     (let [started (component/start system)]
       (deliver (:systemref (meta system)) started)
       started))))

(defn stop
  "Shuts down and destroys the current development system."
  []
  (alter-var-root #'system
                  (fn [s] (when s (component/stop s)))))

(defn go
  "Initializes the current development system and starts it running."
  []
  (init)
  (start)
  :ok)

(defn reset []
  (stop)
  (refresh :after 'dev/go))

(comment (defn show-schema
           []
           (graph-datomic (db/build-uri (:database-connection-description system)))))
