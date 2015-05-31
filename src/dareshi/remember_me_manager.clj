(ns dareshi.remember-me-manager
  (:require [com.stuartsierra.component :as component]
            [ribol.core :refer (raise)]
            [schema.core :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Schema

(s/defrecord DatomicRememberMeManager [schema]
  component/Lifecycle
  (start
   [this]
   (raise :not-implemented))
  (stop
   [this]
   (raise :not-implemented)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Public

(defn ctor
  []
  (map->DatomicRememberMeManager {}))
