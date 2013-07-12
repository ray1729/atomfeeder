(defproject uk.org.1729/atomfeeder "0.1.0-SNAPSHOT"
  :description "Program to periodically parse atom feed and tweet about new articles."
  :url "https://github.com/ray1729/atomfeeder"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/tools.cli "0.2.2"]
                 [org.clojars.runa/clj-schema "0.9.2"]
                 [clj-http "0.7.3"]
                 [clj-time "0.5.1"]
                 [oauth-clj "0.1.4"]
                 [log4j "1.2.17"]
                 [enlive "1.1.1"]
                 [hiccup "1.0.3"]]
  :main uk.org.1729.atomfeeder.cli)
