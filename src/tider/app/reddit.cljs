(ns tider.app.reddit
  (:require [tider.app.utils :refer [jsonp]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def reddit-url "http://reddit.com")

(defn subr-url [s]
  (str reddit-url "/r/" s ".json"))

(defn perma-url [s]
  (str reddit-url s ".json"))

(defn req
  [uri f1 cb]
  (go
    (jsonp uri
       (fn [res]
         (->> (js->clj res :keywordize-keys true)
              (#(get-in % f1))
              (mapv :data)
              (cb)))
       (fn [res] (js/console.error res)))))

(defn subr
  "Loads a subrredit `s` and executes `f` (callback) at the formatted response."
  [s f]
  (req (subr-url s) [:data :children] f))

(defn post
  [s f]
  (let [new-fn (fn [r]
                 (->> r
                      (mapv :children)
                      (mapv #(mapv :data %))
                      (zipmap [:self :comments])
                      ((fn [post]
                         (assoc post :self (get-in post [:self 0]))))
                      f))]
    (req (perma-url s) [] new-fn)))
