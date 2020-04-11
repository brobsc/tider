(ns tider.app.utils
  (:require [goog.string]
            [cljs.core.async :refer [<! timeout]]
            ["moment" :as moment])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def JSONP_TIMEOUT 3000)

(defn jsonp [uri callback err]
  (let [run? (atom false)
        checked-fn (fn [res] (do (callback res) (reset! run? true)))
        script (js/document.createElement "script")
        head (js/document.querySelector "head")
        stripped-uri (->> uri seq (drop 5) (apply str))]
    (do
      (set! js/window.callback checked-fn)
      (set! script.src (str stripped-uri "?jsonp=callback"))
      (.appendChild head script)
      (go
        (<! (timeout JSONP_TIMEOUT))
        (when (not @run?) (err)))
      (.removeChild head script))))

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


