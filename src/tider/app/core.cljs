(ns tider.app.core
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [reitit.frontend :as reitit]
            [clerk.core :as clerk]
            [accountant.core :as accountant]
            [tider.app.reddit :as w]
            [tider.app.utils :refer [unescape from-now
                                     short-score]]))

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

(defn post-card
  ([post]
   [post-card post false])
  ([post self?]
   [:div.post
    [:div.score (short-score (:score post))]
    [:div
     [:a.title {:href (:url post)} (unescape (:title post))]
     [:a.domain.muted-link {:href (str "http://" (:domain post))} (str "(" (:domain post) ")")]]
    [:div
     (when self?
       (when (seq (:selftext post))
         [:div.selftext
          {:dangerouslySetInnerHTML
           #js {:__html (unescape
                          (:selftext_html post))}}]))]
    [:div.post-footer.gap-bar
     [:a.user.muted-link {:href ""} (:author post)]
     [:span.muted-link (from-now (:created_utc post))]
     [:a.comments.muted-link {:href (:permalink post)}
      (str (:num_comments post)
       " comments")]]]))

(defn home []
  [:p "choose a sub"])

(defn subreddit []
  (let [sub (r/atom nil)
        target (-> (session/get :route)
                   (get-in [:route-params :subreddit]))
        _ (w/subr target (partial reset! sub))]
    (fn []
      [:div.subreddit
        [:h1.title "/r/" target]
        [:div.posts
         (for [post @sub]
           ^{:key (:permalink post)}
           [post-card post])]])))

(defn comment-card
  ([comm]
   [comment-card comm 0])
  ([comm level]
   [:div.comment-card
    [:div.score
     [:p (short-score (:ups comm))]
     [:div.dotted-border]]
    [:div.comment-header.gap-bar
     [:a.user.muted-link {:href ""} (:author comm)]
     [:span.muted-link (from-now (:created_utc comm))]]
    [:div.selftext
     {:dangerouslySetInnerHTML
      #js {:__html (unescape
                     (:body_html comm))}}]
    [:div.replies
     (for [reply (->> (get-in (:replies comm) [:data :children])
                      (mapv :data))]
       [comment-card reply (inc level)])]]))


(defn comments []
  (let [page (r/atom nil)
        target (.. js/window -location -pathname)
        _ (w/post target #(reset! page %))]
    (fn []
      (when @page
        [:div
         [post-card (:self @page) true]
         (for [comm (:comments @page)]
           ^{:key (:id comm)}
           [comment-card comm])]))))


(defn page-for [route]
  (case route
    :home #'home
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
       (when (not= (last (seq path)) \/)
         (accountant/navigate! (str path "/")))
       (let [path (if (= (last (seq path)) \/) path (str path "/"))
             match (reitit/match-by-path router path)
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
