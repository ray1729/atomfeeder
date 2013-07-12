(ns uk.org.1729.atomfeeder.util.twitter
  (:require [oauth.twitter :refer [oauth-client]]))

(def ^:dynamic *update-url* "https://api.twitter.com/1.1/statuses/update.json")

(defn client
  [{:keys [consumer-key consumer-secret access-token access-token-secret]}]
  (let [twitter (oauth-client consumer-key
                              consumer-secret
                              access-token
                              access-token-secret)]
    (fn [status]
      (twitter {:method :post :url *update-url* :form-params {:status status}}))))
