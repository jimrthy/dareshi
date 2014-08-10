(ns dareshi.realm
  (:import [org.apache.shiro.authc AuthenticationToken
            SimpleAuthenticationInfo
            UnknownAccountException
            UsernamePasswordToken]
           [org.apache.shiro.authz AuthorizationException
            SimpleAuthorizationInfo]
           [org.apache.shiro.realm AuthorizingRealm]
           [org.apache.shiro.subject PrincipalCollection])
  (:require [com.stuartsierra.component :as component]
            [dareshi.persistence :as db]))

(defn auth-proxy
  [database]
  (proxy [AuthorizingRealm] []
    (doGetAuthenticationInfo
      [^UsernamePasswordToken token]
      ;; TODO: Add caching
      (let [login-name (.getUsername token)
            matches (db/query-principal-by-login-name database login-name)]
        (if (seq matches)
          (let [credentials (first matches)]
            ;; Q: Should this be SaltedAuthenticationInfo instead?
            ;; The latest apache shiro docs say so.
            ;; Then again, SimpleAuthenticationInfo implements that interface.
            ;; So hopefully not.
            (SimpleAuthenticationInfo. (:login-name credentials)
                                       (:password credentials)
                                       (.getName this)))
          (throw (UnknownAccountException.)))))

    (doGetAuthorizationInfo
      [^PrincipalCollection principals]
      ;; What are the subjects associated with a set of principles allowed to do?
      ;; Pulled from
      ;; http://shiro-user.582556.n2.nabble.com/How-to-add-a-role-to-the-subject-td5562700.html

      ;; In the original, User and Role are POJOs (beans) that were loaded from the
      ;; database.
      (let [principals-list (.byType principals (.class "User"))  ; I think this approach is probably wrong
            ]
        (if (seq principals-list)
          (let [principal-ids (map (fn [principal]
                                     (.getId principal))
                                   principals-list)
                authorizations (db/query-authorizations-by-principals database principal-ids)
                result (SimpleAuthorizationInfo. (:roles authorizations))]
            (.setObjectPermissions result (:permissions authorizations))
            result)
          (throw (AuthorizationException. "Empty principals list")))))))

(defrecord Realm [database auther]
  component/Lifecycle

  (start [component]
    (assoc component :auther (auth-proxy database))))

(defn new-realm
  [{:keys [database]}]
  (map->Realm {:database database}))
