(ns coracle.db
  (:require [monger.collection :as mc]
            [monger.core :as m]
            [monger.joda-time :as jt]                       ;; required for joda integration
            [clojure.walk :refer [stringify-keys]]))

(def coll "activities")

(defn connect-to-db [mongo-uri]
  (-> (m/connect-via-uri mongo-uri)
      :db))

(defn add-activity [db activity]
  (mc/insert db coll activity))

(defn assoc-in-query [m map-path value]
  (if value
    (assoc-in m map-path value)
    m))

(defn construct-query [from to]
  (-> {}
      (assoc-in-query ["@published" "$gt"] from)
      (assoc-in-query ["@published" "$lt"] to)))

(defn fetch-activities [db & {:keys [from to]}]
  (let [query (construct-query from to)]
    (->>
      (mc/find-maps db coll query)
      (map #(dissoc % :_id))
      stringify-keys)))