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
  [{:keys [database-protocol database-address database-port database-collection]}]
  (map->DatabaseConnectionDescription {:protocol database-protocol
                                       :address database-address
                                       :port database-port
                                       :collection database-collection}))

(defn build-uri
  [{:keys [protocol address port collection]}]
  (str "datomic:" protocol "://" address ":" port "/" collection))

(defrecord Database [database uri]
  component/Lifecycle

  (start [this]
    (log/trace "Starting Database")
    ;; Q: Is getting a connection an expensive thing?
    (assoc this :database (d/connect uri)))

  (stop [this]
    (assoc this :database nil)))

(defn new-persistence
  [{:keys [uri]}]
  (map->Database {:uri uri}))


;;; Actual work

(defn build-principal-by-login-query
  []
  '{:find [?credentials ?login-name ?password]
    :in [$ ?login-name]
    :where [[?credentials :principal/login-name ?login-name]
            [?credentials :principal/password ?password]]})

(defn query-principal-by-login-name
  [database login-name]
  (map #((zipmap [:entity :login-name :password]
                 %))
       (d/q (build-principal-by-login-query) database login-name)))

;; TODO: Start by testing this
(defn build-authorization-by-principal-query
  []
  '{:find [?principal ?role ?permission]
    :in [$ [?id ...]]
    :where [[?principal :principal/id ?id]
            [?principal :principal/role-name ?role]
            [?role :principal/permission ?permission]]})

(defn query-authorizations-by-principals
  [db principal-ids]
  (reduce (fn [acc [_ role permission]]
            (assoc acc :roles role, :permissions permission))
          {:roles #{}, :permissions #{}}
          (d/q (build-authorization-by-principal-query)
               db principal-ids)))
