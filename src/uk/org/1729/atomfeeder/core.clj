(ns uk.org.1729.atomfeeder.core
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clj-time.core :as ct]
            [uk.org.1729.atomfeeder.config :as config]
            [uk.org.1729.atomfeeder.util.twitter :as twitter]
            [uk.org.1729.atomfeeder.util.bitly :as bitly]
            [uk.org.1729.atomfeeder.util.feed :as feed]))

(def ^:dynamic *max-msg-len* 140)

(defn read-transaction-log
  [path]
  (try
    (with-open [rdr (io/reader path)]
      (reduce (fn [accum entry]
                (assoc-in accum [(:id entry) (:sink entry) :status] (:status entry)))
              {}
              (map edn/read-string (line-seq rdr))))
    (catch java.io.FileNotFoundException _ {})))

(defn build-message
  [preamble entry]
  (loop [msg (str preamble " " (:title entry) " " (:short-url entry)) tags (:tags entry)]
    (if-let [t (first tags)]
      (if (<= (+ (count msg) (count t) 2) *max-msg-len*)
        (recur (str msg " #" (str/replace t #"\s" "_")) (rest tags))
        (recur msg (rest tags)))
      msg)))

(defn send-notification
  [context source sink entry opts]
  (log/info "send-notification" sink entry)
  (let [short-url (:url ((:bitly-client context) :shorten {:longURL (:link entry)}))
        message-text (build-message (:preamble source) (assoc entry :short-url short-url))]
    (log/debug "Message" message-text)
    (when-not (:dry-run opts)
      (case sink
        :twitter ((:twitter-client context) message-text)))))

(defn build-max-age-filter
  [max-age]
  (if max-age
    (fn [entry] (ct/after? (:pubdate entry) (ct/ago max-age)))
    (constantly true)))

(defn send-feed-notifications
  [context opts]
  (let [state (read-transaction-log (:transaction-log context))
        wanted? (build-max-age-filter (:max-age context))]
    (with-open [tlog (java.io.FileWriter. (io/file (:transaction-log context)) true)]
      (doseq [source (:sources context)
              entry  (feed/entries source)
              :when (wanted? entry)
              sink   (:sinks source)]
        (when (not= :sent (get-in state [(:id entry) sink :status]))
          (try 
            (send-notification context source sink entry opts)
            (when-not (:dry-run opts)
              (.write tlog (prn-str {:id (:id entry) :sink sink :status :sent})))
            (catch Exception e (log/error e))))))))

(defn run
  [context & {:as opts}]
  (let [lock-file (io/file (str (:transaction-log context) ".lock"))]
    (with-open [lock (java.io.FileOutputStream. lock-file)]
      (if (.tryLock (.getChannel lock))
        (send-feed-notifications context opts)
        (log/error "Failed to get exclusive lock on transaction log")))))

(defn init
  [config-file]
  (let [conf (config/read config-file)]
    (merge conf {:twitter-client (twitter/client (get-in conf [:credentials :twitter]))
                 :bitly-client   (bitly/client (get-in conf [:credentials :bitly]))})))
