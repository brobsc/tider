(ns tider.dev
  (:require [tider.server.core]
            [nrepl.server]
            [shadow.cljs.devtools.server :as server]
            [shadow.cljs.devtools.api :as shadow]))

(defn start-shadow []
  (println "Starting shadow-cljs server...")
  (server/start!)
  (shadow/watch :app)
  (shadow/repl :app))

(defn nrepl-handler []
  (require 'cider.nrepl)
  (ns-resolve 'cider.nrepl 'cider-nrepl-handler))

(defn -main []
  (println "Dev started...")
  (tider.server.core/-main)
  (nrepl.server/start-server :port 6688 :handler (nrepl-handler))
  (println "Server REPL started at port 6688")
  (start-shadow)
  (shutdown-agents))
