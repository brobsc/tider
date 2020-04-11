(ns tider.app.core
  (:require [reagent.core :as r]
            [reagent.dom :refer [render]]
            [reagent.session :as session]
            [reitit.frontend :as reitit]
            [clerk.core :as clerk]
            [accountant.core :as accountant]
            [tider.app.reddit :as w]
            [tider.app.utils :refer [unescape from-now
                                     short-score]]))

;; Routing managament taken (and then adapted) from the Reagent Template at https://github.com/reagent-project/reagent-template/blob/1fa2ff20ef149ba2006ab52d7e23b7c684cb727d/resources/leiningen/new/reagent/src/cljs/reagent/core.cljs

(def COMMENT_PARTITION 20)
(def MAX_LEVEL 2)

(def router
  (reitit/router
   [["/" :home]
    ["/error/" :jsonp-error]
    ["/r"
      ["/:subreddit/" :subreddit]
      ["/:subreddit/comments/:id/:title/" :comments]]]))

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
  [:p "watch this space! For now, just replace a reddit.com url with betider.com"])

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
   (let [show-more (r/atom false)]
     (fn []
       (when (:author comm)
         [:div.comment-card
          [:div.score
           [:p "·"]
           [:div.dotted-border]]
          [:div.comment-header.gap-bar
           [:span.comm-score (short-score (:ups comm))]
           [:a.user.muted-link {:href ""} (:author comm)]
           [:span.muted-link (from-now (:created_utc comm))]]
          [:div.selftext
           {:dangerouslySetInnerHTML
            #js {:__html (unescape
                           (:body_html comm))}}]
          (let [replies (->> (get-in (:replies comm) [:data :children])
                             (mapv :data))]
            (when (seq replies)
              (if (and (not= 0 level)
                       (zero? (mod level MAX_LEVEL))
                       (not @show-more))
                [:a.subtle-link.show-more
                 {:on-click #(swap! show-more not)}
                 (str "show " (count replies) " more replies")]
                [:div.replies
                 (for [reply replies]
                   ^{:key (:id reply)}
                   [comment-card reply (inc level)])])))])))))

(defn comment-listing [comments]
  (for [comm comments]
    ^{:key (:id comm)}
    [comment-card comm]))

(defn comments []
  (let [page (r/atom nil)
        target (.. js/window -location -pathname)
        _ (w/post target #(reset! page %))
        limit (r/atom 1)]
    (fn []
     (when @page
       (let [sub (str "/" (get-in @page [:self :subreddit_name_prefixed]))]
         [:div
          [:a.subreddit.title {:href sub} sub]
          [post-card (:self @page) true]
          (let [comments-partitions
                (take @limit
                      (partition
                        COMMENT_PARTITION
                        COMMENT_PARTITION [] (:comments @page)))]
            [:div
             (for [part comments-partitions]
               (comment-listing part))
             (let [comment-count (count (:comments @page))
                   remaining (- comment-count (* COMMENT_PARTITION @limit))]
               (when (pos? remaining)
                 [:a.subtle-link
                  {:on-click #(swap! limit inc)}
                  (str "load more ("
                       remaining
                       " remaining)")]))])])))))

(defn jsonp-error []
  [:div.error-message
   [:h1 "An error occured. Please disable tracking protection for this site and try again"]
   [:p "Currently, we need tracking protection to be disabled so we can load content from reddit."]
   [:p "It's identified as tracking by Firefox, and possibly other content blockers, because we're loading external content here, directly from your browser."]
   [:p "This will be fixed, but, in the meantime, whitelisting/disabling protection for this site is enough."]])

(defn page-for [route]
  (case route
    :home #'home
    :jsonp-error #'jsonp-error
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
       [:footer
        [:a.subtle-link
         {:on-click #(js/window.scrollTo 0 0)}
         "jump to top"]]])))

(defn start []
  (render [app]
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
