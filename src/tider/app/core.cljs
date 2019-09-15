(ns tider.app.core
  (:require [reagent.core :as r]))

(defn app []
  [:div
   [:h1 "tider for reddit"]
   [:p "a tidier reddit experience"]])

(defn start []
  (r/render [app]
            (.getElementById js/document "content")))

(defn ^:export init []
  (start))


