(ns dareshi.realm
  (:import [javax.naming.directory SearchControls]
           [javax.naming.ldap LdapContext]
           [org.apache.shiro.authc
            AccountException
            AuthenticationToken
            SimpleAuthenticationInfo
            UnknownAccountException
            UsernamePasswordToken]
           [org.apache.shiro.authz
            AuthorizationException
            SimpleAuthorizationInfo]
           [org.apache.shiro.realm AuthorizingRealm]
           [org.apache.shiro.realm.ldap JndiLdapRealm
            LdapContextFactory
            LdapUtils]
           [org.apache.shiro.subject PrincipalCollection])
  (:require [com.stuartsierra.component :as component]
            [dareshi.persistence :as db]
            [ribol.core :refer (raise)]
            [taoensso.timbre :as log]))

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

(defn get-role-names-for-groups
  "Should map fully-qualified LDAP group names as returned by
the directory server to role names.
The example that the ActiveDirectoryRealm gives as the key is
CN=Group,OU=company,DC=MyDomain,DC=local"
  [group-names]
  ;; For that matter, I have some doubts about how this map should
  ;; get set.
  ;; It seems like it very definitely lives in the database,
  ;; but I'm badly fuzzy on AD after that point.
  (raise [:not-implemented
          {:reason "I have some doubts about what this looks like"}]))

(defn ldap-member-roles
  [user-name attr]
  (when (= "memberOf" (.getID attr))
    (let [group-names (LdapUtils/getAllAttributeValues attr)]
      (log/debug "Groups found for user [" user-name "]: " group-names)
      (get-role-names-for-groups group-names))))

(defn ldap-roles-by-group
  [user-name user]
  (log/debug "Retrieving group names for user [" (.getName user) "]")
  (when-let [attrs (.getAttributes user)]
    (let [attr-enum (.getAll attrs)]
      (map (partial ldap-member-roles user-name)
           attr-enum))))

(defn ldap-proxy
  "Realm that handles authentication through LDAP, and authorization
based on LDAP groups (according to permissions associated in database?)

Based strongly on
stackoverflow.com/questions/12173492/shiro-jndildaprealm-authorization-against-ldap"
  [database]
  (proxy [JndiLdapRealm] []
    (queryforAuthorizationInfo [^PrincipalCollection principals ^LdapContextFactory ctxFactory]
      (let [user-name (.getAvailablePrincipal this principals)
            ldap-ctx (.getSystemLdapContext ctxFactory)]
        (try
          (.getRoleNamesForUser this user-name ldap-ctx)
          (finally
            (LdapUtils/closeContext ldap-ctx)))))
    (buildAuthorizationInfo [role-names]
      ;; Q: What's the type hint for Set<String> ?
      (SimpleAuthorizationInfo. role-names))

    (getRoleNamesForUser
      [^String user-name ^LdapContext ldap-ctx]
      (let [search-controls (SearchControls.)
            search-filter "(&(objectClass=*)(CN={0}))"
            search-arguments [user-name]]
        (.setSearchScope search-controls SearchControls/SUBTREE_SCOPE)
        (let [users (.search ldap-ctx (.searchBase this) search-filter search-arguments search-controls)]
          ;; users is a javax.naming.NamingEnumeration
          ;; Q: Can I map over that?
          (as-> 
           (map (partial ldap-roles-by-group user-name)
                users) x
                (concat x)
                (filter identity x)
                (set x)))))))

(defrecord LdapRealm [database auther]
  component/Lifecycle
  (start [this]
    (assoc this :auther (ldap-proxy database)))
  (stop [this]
    this))

(defn new-ldap-realm
  [{:keys [database]}]
  (map->LdapRealm {:database database}))
