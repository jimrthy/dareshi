(ns authczar.env
  "Because callers need an environment for obtaining the SecurityManager.

And, really, the whole thread-based Subject association that
Shiro assumes by default simply does not play well with the way
I understand clojure multi-threading works."
  (:require [authczar.realm :as realm]
            [authczar.remember-me-manager :as remember-me-manager]
            [com.stuartsierra.component :as component]
            [ribol.core :as ribol :refer (raise)]
            [schema.core :as s]
            [taoensso.timbre :as log])
  (:import [authczar.realm AuthczarRealm]
           [authczar.remember_me_manager DatomicRememberMeManager]
           [org.apache.shiro.env DefaultEnvironment]
           [org.apache.shiro.mgt DefaultSecurityManager]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Schema

(s/defrecord Environment [environment :- DefaultEnvironment
                          realms :- AuthczarRealm
                          remember-me-manager :- DatomicRememberMeManager
                          security-manager :- DefaultSecurityManager]
  component/Lifecycle
  (start
   [this]
   (let [environment (DefaultEnvironment.)
         security-manager (DefaultSecurityManager. (:authers realms))]
     (.setRememberMeManager remember-me-manager)
     (.setSecurityManager environment security-manager)
     (into this {:environment environment
                 :security-manager security-manager})))

  (stop
   [this]
   (when environment
     (.destroy environment))
   (into this {:environment nil
               :security-manager nil})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Public

(defn ctor
  []
  (map->Environment {}))
