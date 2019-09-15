(ns tider.repl
  (:require [reply.main :as reply]
            [tider.server.core]
            [dotenv :refer [env]]))

(def port (or (env :SERVER_REPL_PORT)
              "6688"))

(defn -main []
  (println (format "Connecting to Server REPL at port %s" port))
  (reply/launch-nrepl {:attach port})
  (shutdown-agents))
