(defproject authczar "0.1.0-SNAPSHOT"
  :description "I need to stash things like users, roles, and passwords in a database.
Since I'm trying to use datomic for everything else, this seemed like a reasonable
experiment."
  :dependencies [[com.datomic/datomic-free "0.9.5078" :exclusions [joda-time org.slf4j/slf4j-api]]
                 [com.stuartsierra/component "0.2.2"]
                 [com.taoensso/timbre "3.3.1"]
                 [org.apache.shiro/shiro-core "1.2.3"]
                 [org.clojure/clojure "1.7.0-alpha1"]
                 [prismatic/schema "0.3.3"]
                 [prismatic/plumbing "0.3.5"]]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main ^:skip-aot authczar.main
  :profiles {:dev {:dependencies [[clj-ns-browser "1.3.1"]
                                  [datomic-schema-grapher "0.0.1" :exclusions [hiccup]]
                                  [im.chit/ribol "0.4.0"]
                                  [midje "1.6.3" :exclusions [commons-codec joda-time]]
                                  [org.clojure/tools.namespace "0.2.7"]
                                  [org.clojure/java.classpath "0.2.2"]]
                   :source-paths ["dev"]}
             :uberjar {:aot :all}}
  :repl-options {:init-ns user
                 :welcome (println "Run (dev) to start")}
  :target-path "target/%s"
  :url "http://frereth.com")
