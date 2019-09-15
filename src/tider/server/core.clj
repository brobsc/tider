(ns tider.server.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.util.response :refer [not-found]]
            [clojure.java.io :as io]
            [dotenv :refer [env]])
  (:gen-class))

(def port (or (Integer/parseInt (env :APP_PORT))
              3000))
(defonce server (atom {}))

(defmulti handler :uri)

(defmethod handler "/"
  [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (-> "public/index.html" io/resource slurp)})

(defmethod handler :default
  [request]
  (not-found "Page not found :("))

(defn run-server [handler-fn]
  (reset! server (run-jetty handler-fn {:port port :join? false}))
  (println (format "Started server on http://localhost:%d" port)))

(defn -main []
  (-> handler
      (wrap-resource "public/")
      (wrap-content-type)
      (run-server)))
