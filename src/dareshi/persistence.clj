(ns dareshi.persistence
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d]
            [taoensso.timbre :as log]))

;;; This doesn't need any life cycle. It's really just a basic struct
;;; Then again...it seems worth setting up defaults from config
;;; Or, maybe, that's where these values come from. Out of the .jar
;;; or home directories. So we should really be querying some more
;;; "normal" local database (or web service?) for the actual
;;; values to use.
(defrecord DatabaseConnectionDescription [protocol address port collection]
  component/Lifecycle)

(defn new-connection-description
  [{:keys database-protocol database-address database-port database-collection}]
  (map->DatabaseConnectionDescription {:protocol database-protocol
                                       :address database-address
                                       :port database-port
                                       :collection database-collection}))

(defn build-uri
  [{:keys [protocol address port collection]}]
  (str "datomic:" protocol "://" address ":" port "/" collection))

(defrecord Database []
  component/Lifecycle

  (start [{:keys [database-connection-descriptoin] :as component}]
    (log.trace "Starting Database")
    ;; Q: Is getting a connection an expensive thing?
    (let [uri (build-uri database-connection-description)
          conn d/connect uri]
      (raise :finish-this)))

  (stop [component]
    (raise :not-implemented)))
