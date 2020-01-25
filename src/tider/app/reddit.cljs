(ns tider.app.reddit
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<!]])
  (:import [goog.net Jsonp]
           [goog.string Const]
           [goog.html TrustedResourceUrl]))

(def reddit-url "http://reddit.com")

(defn subr-url [s]
  (str reddit-url "/r/" s ".json"))

(defn perma-url [s]
  (str reddit-url s ".json"))

(defn req
  [uri f1 cb]
  (go (let [jp (Jsonp. (-> uri Const/from TrustedResourceUrl/fromConstant)
                       "jsonp")]
        (.setRequestTimeout jp 3000)
        (.send jp nil
           (fn [res]
             (->> (js->clj res :keywordize-keys true)
                  (#(get-in % f1))
                  (mapv :data)
                  (cb)))
           (fn [res] (js/console.error res))))))

(defn subr
  "Loads a subrredit `s` and executes `f` (callback) at the formatted response."
  [s f]
  (req (subr-url s) [:data :children] f))
