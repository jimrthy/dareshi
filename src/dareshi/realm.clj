(ns dareshi.realm
  (:import [org.apache.shiro AuthorizingRealm]
           [org.apache.shiro.authc AuthenticationToken
            AuthorizationException
            SimpleAuthenticationInfo
            UnknownAccountException
            UsernamePasswordToken])
  (:require [com.stuartsierra.component :as component]
            [dareshi.persistence :as db]))

(defrecord Realm [database]
  component/Lifecycle

  AuthorizingRealm
  (doGetAuthenticationInfo
    [this ^UsernamePasswordToken token]
    ;; TODO: Add caching
    (let [login-name (.getUsername token)
          matches (db/query-principal-by-login-name login-name)]
      (if (seq matches)
        (let [credentials (first matches)]
          (SimpleAuthenticationInfo. (:login-name credentials)
                                     (:password credentials)
                                     (.getName this)))
        (throw (UnknownAccountException.)))))

  (doGetAuthorizationInfo
    "What are the subjects associated with a set of principles allowed to do?
Pulled from
http://shiro-user.582556.n2.nabble.com/How-to-add-a-role-to-the-subject-td5562700.html"
    [this ^PrincipalCollection principals]
    ;; In the original, User and Role are POJOs (beans) that were loaded from the
    ;; database.
    (let [principals-list (.byType principals (.class User))  ; I think this approach is probably wrong
          ]
      (if (seq principals-list)
        (let [principal-ids (map (fn [principal]
                                   (.getId principal))
                                 principals-list)
              authorizations (db/query-authorizations-by-principals database principal-ids)
              result (SimpleAuthorizationInfo. (:roles authorizations))]
          (.setObjectPermissions result (:permissions authorizations))
          result)
        (throw (AuthorizationException. "Empty principals list"))))))


(defn new-realm
  [{:keys [database]}]
  (map->Realm {:database database}))
