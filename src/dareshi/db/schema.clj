(ns dareshi.db.schema
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d]
            [datomic-helpers :as d-h]
            [ribol.core :refer (raise)]
            [taoensso.timbre :as log]))

;;;; Strictly speaking, this component isn't really required
;;;; by anything else.
;;;; And it doesn't really fit into the rest of the architecture.
;;;; It's really something that should get installed into your
;;;; database at build time. Or maybe install time. Depending on
;;;; your architecture.

;;;; Either way, it doesn't particularly belong in here. I don't
;;;; think.
;;;; But it is convenient for dev time.


;;; What a "real" transaction looks like
(def ^:private template-placeholder-meaningless
  {:db/id (d/tempid :db.part/db)
   :db/ident :what-is-this
   :db/valueType :db.type/int
   :db/isComponent false
   :db/cardinality :db.cardinality/one
   :db.install/_attribute :db.part/db}) 


(defn principals
  "Return a map that describes a transaction for generating subject schema"
  []
  (d-h/to-schema-transaction
   {:principal/login-name :db.type/string
    ;; This is actually a ModularCryptFormat string that has the Salt
    ;; and any other useful data embedded
    :principal/password :db.type/string
    :principal/id :db.type/uuid
    ;; Q: Are these really part of principals?
    ;; It seems like they'd make more sense as part of the subject
    ;; identified by a given set of principals
    :principal/role-name [:db.type/string]
    :principal/permission [:db.type/string]}))


(defn build-schema
  "Return a data structure suitable for passing into a transaction
for generating our schema.
This is really where the interesting stuff gets collected and should
evolve. Everything below probably counts as boiler plate."
  []
  (concat (principals)))


(defn initialize-schema
  "Poke the schema into the database"
  [db]
  (d/transact db (build-schema)))


(defrecord Schema [database]
  component/Lifecycle

  (start [this]
    (initialize-schema database)
    this))

(defn new-schema
  [{:keys []}]
  (map->Schema {}))
