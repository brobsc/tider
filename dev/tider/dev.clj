(ns tider.dev
  (:require
    [clojure.java.io :as io]
    [cider.nrepl :refer [cider-middleware]]
    [tider.server.core]
    [nrepl.server]
    refactor-nrepl.middleware
    [shadow.cljs.devtools.server :as server]
    [shadow.cljs.devtools.api :as shadow]
    [dotenv :refer [env]]))

(def port (or (Integer/parseInt (env :SERVER_REPL_PORT))
              6688))

(defn start-shadow []
  (println "Starting shadow-cljs server...")
  (server/start!)
  (shadow/watch :app)
  (shadow/repl :app))

(def wrapped-handler
  (apply nrepl.server/default-handler
         (cons #'refactor-nrepl.middleware/wrap-refactor
               (map resolve cider-middleware))))

(defn -main []
  (println "Dev started...")
  (tider.server.core/-main)
  (nrepl.server/start-server
    :port port
    :handler wrapped-handler)
  (spit (doto (io/file "./.nrepl-port") .deleteOnExit) port)
  (println (format "Started nREPL server at port %s" port))
  (start-shadow)
  (shutdown-agents)
  (System/exit 0))
