(defproject com.frereth/dareshi "0.1.0-SNAPSHOT"
  :description "I need to stash things like users, roles, and passwords in a database.
Since I'm trying to use datomic for everything else, this seemed like a reasonable
experiment."
  :dependencies [[com.datomic/datomic-free "0.9.5385" :exclusions [joda-time org.slf4j/slf4j-api
                                                                   org.clojure/tools.cli]]
                 [com.stuartsierra/component "0.3.1"]
                 [com.taoensso/timbre "4.5.1"]
                 ;; TODO: Switch this out for hara.event
                 [im.chit/ribol "0.4.1"]
                 [org.apache.shiro/shiro-core "1.2.5"]
                 [org.clojure/clojure "1.9.0-alpha7"]
                 [prismatic/schema "1.1.2"]
                 [prismatic/plumbing "0.5.3"]]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main ^:skip-aot dareshi.main
  :plugins {}
  :profiles {:dev {:plugins [[org.clojure/tools.namespace "0.2.11"]
                             [org.clojure/java.classpath "0.2.2"]]
                   :source-paths ["dev"]}
             ;; TODO: I strongly suspect that I don't want to do this
             :uberjar {:aot :all}}
  :repl-options {:init-ns user
                 :welcome (println "Run (dev) to start")}
  :target-path "target/%s"
  :url "http://frereth.com")
