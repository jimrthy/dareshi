(ns dareshi.realm
  (:import [org.apache.shiro.authc
            AccountException
            AuthenticationToken
            SimpleAuthenticationInfo
            UnknownAccountException
            UsernamePasswordToken]
           [org.apache.shiro.authz
            AuthorizationException
            SimpleAuthorizationInfo]
           [org.apache.shiro.realm AuthorizingRealm]
           [org.apache.shiro.subject PrincipalCollection])
  (:require [com.stuartsierra.component :as component]
            [dareshi.persistence :as db]))

(defn auth-proxy
  "The JdbcRealm mentions that it might also be possible to override
getRoleNamesForUser and/or getPermissions."
  [database]
  (proxy [AuthorizingRealm] []
    (doGetAuthenticationInfo
      [^UsernamePasswordToken token]
      ;; TODO: Add caching
      (if-let [login-name (.getUsername token)]
        (let [matches (db/query-principal-by-login-name database login-name)]
          (if (seq matches)
            (let [credentials (first matches)]
              ;; Q: Should this be SaltedAuthenticationInfo instead?
              ;; The latest apache shiro docs say so.
              ;; Then again, SimpleAuthenticationInfo implements that interface.
              ;; So hopefully not.
              ;; JdbcRealm calls .toCharArray on the password. That seems a likely
              ;; source of breakage.
              (SimpleAuthenticationInfo. (:login-name credentials)
                                         (:password credentials)
                                         (.getName this))
              ;; At this point, JdbcRealm sets the info's salt, if any.
              ;; Q: Should I?
              )
            (throw (UnknownAccountException.))))
        (throw (AccountException. "NULL user account illegal"))))

    (doGetAuthorizationInfo
      [^PrincipalCollection principals]
      ;; What are the subjects associated with a set of principles allowed to do?
      ;; Pulled from
      ;; http://shiro-user.582556.n2.nabble.com/How-to-add-a-role-to-the-subject-td5562700.html

      ;; In the original, User and Role are POJOs (beans) that were loaded from the
      ;; database.
      ;; I think this approach is probably wrong. And would be, even if I were passing in
      ;; the User class, like the original, as opposed to just a place-holder string that
      ;; allowed the compiler to work.
      (let [principals-list (.byType principals (.class "User"))
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

  (start [this]
    (assoc this :auther (auth-proxy database)))
  (stop [this]
    this))

(defn new-realm
  [{:keys [database]}]
  (map->Realm {:database database}))
