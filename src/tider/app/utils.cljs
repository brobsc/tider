(ns tider.app.utils
  (:require [goog.string]
            ["moment" :as moment]))

(def unescape
  (fnil goog.string/unescapeEntities ""))

(defn from-now [i]
  (.fromNow (moment/unix i)))


