(ns tider.app.core
  (:require [reagent.core :as r]
            [tider.app.reddit :as w]))

(defonce proggit (r/atom {}))

(defn post-card [post]
  [:div.post
   [:div.score (:score post)]
   [:div
    [:a.title {:href (:url post)} (:title post)]
    [:a.domain.muted-link {:href (str "http://" (:domain post))} (str "(" (:domain post) ")")]]
   [:div.gap-bar
    [:a.user.muted-link {:href ""} (:author post)]
    [:a.comments.muted-link {:href (:permalink post)}
     (str (:num_comments post)
      " comments")]]])

(defn app []
  (let [sub (r/atom nil)
        _ (w/subr "programming" (partial reset! sub))]
    (fn []
      [:div.container
       [:div
        [:span.title "tider for reddit"]
        [:span.subtitle "  a tidier reddit experience"]]
       [:div.posts
        (for [post @sub]
          ^{:key (:permalink post)}
          [post-card post])]])))

(defn start []
  (r/render [app]
    (.getElementById js/document "content")))

(defn ^:export init []
  (start))
