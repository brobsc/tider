(ns tider.app.core
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [reitit.frontend :as reitit]
            [clerk.core :as clerk]
            [accountant.core :as accountant]
            [tider.app.reddit :as w]))

(defonce proggit (r/atom {}))

;; Routing managament taken (and then adapted) from the Reagent Template at https://github.com/reagent-project/reagent-template/blob/1fa2ff20ef149ba2006ab52d7e23b7c684cb727d/resources/leiningen/new/reagent/src/cljs/reagent/core.cljs

(def router
  (reitit/router
   [["/" :home]
    ["/r"
      ["/:subreddit/" :subreddit]
      ["/:subreddit/comments/:id/:title/" :comments]]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

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

(defn posts []
  (let [sub (r/atom nil)
        _ (w/subr "programming" (partial reset! sub))]
    (fn []
       [:div.posts
        (for [post @sub]
          ^{:key (:permalink post)}
          [post-card post])])))

(defn subreddit []
  (let [sub (r/atom nil)
        target (-> (session/get :route)
                   (get-in [:route-params :subreddit]))
        _ (w/subr target (partial reset! sub))]
    (fn []
       [:div.posts
        (for [post @sub]
          ^{:key (:permalink post)}
          [post-card post])])))

(defn comments []
  (let [page (r/atom {})
        target (.. js/window -location -pathname)
        _ (w/post target #(reset! page %))]
    (fn []
      [:div
       [:h1 (get-in @page [:self :title])]])))

(defn page-for [route]
  (case route
    :home #'posts
    :subreddit #'subreddit
    :comments #'comments))

(defn app []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div.container
       [:div
        [:span.title "tider for reddit"]
        [:span.subtitle "  a tidier reddit experience"]]
       [page]
       [:footer ""]])))

(defn start []
  (r/render [app]
    (.getElementById js/document "content")))

(defn ^:export init []
  (clerk/initialize!)
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (let [match (reitit/match-by-path router path)
             current-page (:name (:data match))
             route-params (:path-params match)]
         (r/after-render clerk/after-render!)
         (session/put! :route {:current-page (page-for current-page)
                               :route-params route-params})
         (clerk/navigate-page! path)))
     :path-exists?
     (fn [path]
       (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (start))
