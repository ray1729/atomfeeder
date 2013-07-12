(ns uk.org.1729.atomfeeder.util.feed
  (:require [net.cgrand.enlive-html :as enlive]
            [clj-time.format :as tf]))

(defn build-link-selector
  [config]
  (if-let [rel (get-in config [:link-rel])]
    [[:link (enlive/attr= :rel rel)]]
    [:link]))

(defn build-entry-parser
  [config]
  (let [link-selector (build-link-selector config)]
    (fn [entry]
      (let [id      (first (enlive/select entry [:id]))
            title   (first (enlive/select entry [:title]))
            link    (first (enlive/select entry link-selector))
            pubdate (or (first (enlive/select entry [:published]))
                        (first (enlive/select entry [:updated])))
            tags    (enlive/select entry [:category])]
        {:id      (first (:content id))
         :title   (first (:content title))
         :link    (get-in link [:attrs :href])
         :pubdate (tf/parse (first (:content pubdate)))
         :tags    (map (comp first :content) tags)}))))

(defn fetch
  [url]
  (enlive/xml-resource (java.net.URL. url)))

(defn entries
  [config]
  (let [feed (fetch (:url config))]
    (map (build-entry-parser config) (enlive/select feed [:feed :> :entry]))))
