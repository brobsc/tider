(ns tider.app.utils
  (:require [goog.string]
            ["moment" :as moment]))

(def unescape
  (fnil goog.string/unescapeEntities ""))

(defn from-now [i]
  (.fromNow (moment/unix i)))

(defn short-score [i]
   (let [digits (str i)]
    (case (count digits)
     4 (str (apply str (->> digits
                          (interpose ".")
                          (take 3)))
         "K")
     5 (str (apply str (->> digits (take 2)))
         "K")
     6 (str (apply str (->> digits (take 3)))
         "K")
     (apply str digits))))


