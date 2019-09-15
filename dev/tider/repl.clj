(ns tider.repl
  (:require [reply.main :as reply]
            [tider.server.core]))

(defn -main []
  (println "Connecting to REPL at port 6688")
  (reply/launch-nrepl {:attach "6688"})
  (shutdown-agents))
