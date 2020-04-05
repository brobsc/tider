(ns tider.app.utils
  (:require [goog.string]
            ["moment" :as moment]))

(defn jsonp [uri callback _]
  (let [script (js/document.createElement "script")
        _ (set! js/window.jsonpCb callback)
        _ (set! script.src (str (reduce str (drop 5 (seq uri)))
                            "?jsonp=jsonpCb"))
        head (js/document.querySelector "head")
        _2 (.appendChild head script)]
   nil))

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


