(ns app.core
  (:require [app.resource :as rc]
            [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [reagent.core :as r]))

;; State

(def state (r/atom {:posts (rc/get-posts "src/posts")}))

;; Routes

(def app-routes
  ["/" {"" :index
        ["" :post-id] :post
        ["" :page-id] :page}])

(defn current-page []
  (:current-page @state))

;; Views

(defn index []
  [:div
   [:h2 "Index"]
   (for [post (:posts @state)]
     [:div {:key (first (:id (:metadata (last post))))}
      [:a {:href (bidi/path-for
                  app-routes
                  :post
                  :post-id
                  (first (:id (:metadata (last post)))))}
       (first (:title (:metadata (last post))))]])
   [:p
    [:a {:href (bidi/path-for app-routes :page :page-id "another-page")} "another page"]]])

(defn post [post-id]
  [:div
   [:div
    [:a {:href (bidi/path-for app-routes :index)} "index"]
    [:article {:dangerouslySetInnerHTML {:__html (:html ((keyword post-id) (:posts @state)))}}]]])

(defn page [page-id]
  [:div
   [:div
    [:a {:href (bidi/path-for app-routes :index)} "index"]
    [:div "another page"]]])

;; Routing

(defn pages [path]
  (case (:handler (:current-page @state))
    :index [index]
    :page [page :another-page]
    :post [post (:post-id (:route-params (:current-page @state)))]
    [[:div "default page"]]))

(defn set-page! [match]
  (swap! state assoc :current-page match))

(defn app []
  [:div (pages current-page)])

(def history
  (pushy/pushy set-page! (partial bidi/match-route app-routes)))

;; Start

(defn ^:export start! []
  (pushy/start! history)
  ;; (js/alert @state)
  (r/render [app]
   (.getElementById js/document "app")))