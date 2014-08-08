(ns dareshi.system
  "Components and their dependency relationships"
  (:refer-clojure :exclude (read))
  (:require
   [clojure.java.io :as io]
   [clojure.tools.reader :refer (read)]
   [clojure.string :as str]
   [clojure.tools.reader.reader-types :refer (indexing-push-back-reader)]
   [com.stuartsierra.component :as component :refer (system-map system-using)]
   [dereshi.db.schema :as schema]
   [dareshi.persistence :refer (new-persistence)]))

(defn ^:private read-file
  [f]
  (read
   ;; This indexing-push-back-reader gives better information if the
   ;; file is misconfigured.
   (indexing-push-back-reader
    (java.io.PushbackReader. (io/reader f)))))

(defn ^:private config-from
  [f]
  (if (.exists f)
    (read-file f)
    {}))

(defn ^:private user-config
  []
  (config-from (io/file (System/getProperty "user.home") ".dareshi.edn")))

(defn ^:private config-from-classpath
  []
  (if-let [res (io/resource "dareshi.edn")]
    (config-from (io/file res))
    {}))

(defn config
  "Return a map of the static configuration used in the component
  constructors."
  []
  (merge (config-from-classpath)
         (user-config)))

(defn new-base-system-map
  "Builds the system map"
  [config systemref]
  (let [{:keys []} config]
    (system-map
     :database-connection-description (db/new-connection-description config)
     :database (db/new-persistence config)
     :schema (schema/new-schema config))))

(defn new-base-dependency-map [system-map]
  "Which components rely on which others?"
  {:database {:uri :database-connection-description}
   :schema {:database :database}})

(defn new-production-system
  "Create the production system"
  [systemref]
  (let [s-map (new-base-system-map (config) systemref)
        d-map (new-base-dependency-map s-map)]
    (with-meta
      (component/system-using s-map d-map)
      {:dependencies d-map})))
