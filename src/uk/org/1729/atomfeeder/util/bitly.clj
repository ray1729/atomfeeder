(ns uk.org.1729.atomfeeder.util.bitly
  (:require [clj-http.client :as http]
            [hiccup.util :refer [url-encode]]))

(def ^:dynamic *base-url* "https://api-ssl.bitly.com/v3/")

(def path-for {:shorten "shorten"
               :expand  "expand"
               :info    "info"})

(defn build-url
  [access-token action params]
  (if-let [path (path-for action)]    
    (str *base-url* path "?" (url-encode (merge {"access_token" access-token} params)))
    (throw (Exception. (str "Unrecognized action: " action)))))

(defn client
  [{:keys [access-token]}]
  (fn [action params]
    (let [res (http/get (build-url access-token action params) {:as :json})]
      (get-in res [:body :data]))))
