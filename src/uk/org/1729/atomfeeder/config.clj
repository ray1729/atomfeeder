(ns uk.org.1729.atomfeeder.config
  (:refer-clojure :exclude [read])
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clj-time.core :as ct]
            [clj-schema.schema :refer [def-map-schema sequence-of optional-path]]
            [clj-schema.simple-schemas :refer [OneOf NonEmptyString URL]]
            [clj-schema.validation :refer [validate-and-handle]])
  (:import java.io.PushbackReader))

(defn parse-duration
  [s]
  (when s
    (when-let [[_ n unit] (re-matches #"^(\d+)\s+(hour|day|week|month)s?$" s)]
      (let [n (Integer/parseInt n)]
        (case unit
          "hour"  (ct/hours n)
          "day"   (ct/days n)
          "week"  (ct/weeks n)
          "month" (ct/months n))))))

(def-map-schema source-schema
  [[:url]                      URL   
   (optional-path [:link-rel]) NonEmptyString
   (optional-path [:preamble]) NonEmptyString
   [:sinks]                    (sequence-of (OneOf :twitter))])

(def-map-schema config-schema
  [[:credentials :bitly :access-token]          NonEmptyString
   [:credentials :twitter :consumer-key]        NonEmptyString
   [:credentials :twitter :consumer-secret]     NonEmptyString
   [:credentials :twitter :access-token]        NonEmptyString
   [:credentials :twitter :access-token-secret] NonEmptyString
   [:transaction-log]                           NonEmptyString
   (optional-path [:max-age])                   (comp boolean parse-duration)
   [:sources]                                   (sequence-of source-schema)])

(defn validate
  [config]
  (validate-and-handle config
                       config-schema
                       (fn [m] (update-in m [:max-age] parse-duration))
                       (fn [_ error-msgs]
                         (throw (AssertionError. (str "Failed to validate configuration: " error-msgs))))))

(defn read
  [file]
  (with-open [rdr (PushbackReader. (io/reader file))]
    (validate (edn/read rdr))))
