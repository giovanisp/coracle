(ns coracle.core
  (:gen-class)
  (:require [scenic.routes :refer :all]
            [ring.util.response :as r]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.adapter.jetty :refer [run-jetty]]
            [monger.core :as m]
            [monger.collection :as mc]))

(defn not-found-handler [req]
  (-> (r/response {:error "not found"}) (r/status 404)))

(defn add-activity [db req]
  (prn req)
  (prn db)
  (mc/insert db "activities" (:body req))
  (-> (r/response {}) (r/status 201)))

(defn _handler [db]
  (scenic-handler (load-routes-from-file "routes.txt")
                  {:add-activity    (partial add-activity db)
                   :show-activities (fn [req] (r/response {:some :activities}))}
                   not-found-handler))

(defn handler [db]
  (-> (_handler db)
      (wrap-json-body :keywords? false)
      (wrap-json-response {:key-fn identity})))

(def port 7000)
(def server (atom nil))

(defn start-server [db port]
  (reset! server (run-jetty (handler db) {:port port})))

(defn -main [& args]
  (prn "starting server...")
  (let [db (-> (m/connect-via-uri "mongodb://localhost:27017/coracle") :db)]
    (start-server db port)))
