(ns uk.org.1729.atomfeeder.cli
  (:require [clojure.java.io :as io]
            [clojure.tools.cli :refer [cli]]
            [uk.org.1729.atomfeeder.core :as atomfeeder])
  (:gen-class true))

(defn file-or-resource
  [path]
  (let [f (io/file path)]
    (if (.exists f) f (io/resource path))))

(defn -main
  [& args]
  (let [[opts args doc] (cli args
                             ["-c" "--conf"]
                             ["-n" "--[no-]dry-run"])]
    (if-let [config-file (file-or-resource (:conf opts))]
      (let [context (atomfeeder/init config-file)]
        (atomfeeder/run context :dry-run (:dry-run opts)))
      (throw (Exception. (str "Configuration file not found: " (:conf opts)))))))
