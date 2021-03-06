(ns app.core
  (:require [app.resource :as rc]
            [app.rain :refer [canvas]]
            [bidi.bidi :as bidi]
            [clojure.string :refer [split]]
            [pushy.core :as pushy]
            [reagent.core :as r]
            [goog.string :as gstring]
            ["hyvor-talk-react" :as HyvorTalk]
            ["highlight.js" :as hljs]))

;; State

(def state (r/atom {:posts (rc/get-posts "src/posts")}))

;; Routes

(def app-routes
  ["/" {""               :index
        "about"          :about
        ["" :post-id]    :post
        ["tag/" :tag-id] :tag
        "resume"         :resume
        true             :not-found}])

(defn current-page []
  (:current-page @state))

;; Views
(defn about []
  [:div.mt-12
   [:article.markdown.mt-6
    {:dangerouslySetInnerHTML {:__html (:html (rc/get-about-html "src/about.md"))}}]
   ;; [:> (canvas)]
   ])

(defn tag-template [tag]
  [:a.text-blue-600.text-sm.t.ml-3.border-b.border-transparent.hover:border-blue-600
   {:key  tag
    :href (bidi/path-for
           app-routes
           :tag
           :tag-id
           tag)}
   tag])

(defn tags [post]
  [:div.-mt-px
   (for [tag (split (first (:tags (:metadata post))) " ")]
     (tag-template tag))])

(defn index [posts]
  [:main.mt-12
   (for [post posts]
     [:div.my-4.py-4 {:key (first (:id (:metadata (last post))))}
      [:a.text-lg.font-medium.border-b.border-transparent.hover:border-gray-900
       {:href (bidi/path-for
               app-routes
               :post
               :post-id
               (first (:id (:metadata (last post)))))}
       (first (:title (:metadata (last post))))]
      [:div.flex.items-center.mt-1
       [:time.text-sm.tracking-wide.mt-px (first (:date (:metadata (last post))))]
       (tags (last post))]
      [:p.tracking-wide.mt-2 (first (:subtitle (:metadata (last post))))]])])

(defn not-found []
  [:div.mt-12
   [:p "Not Found"]])

(defn- highlight-block [node]
  (if (nil? (.querySelector (r/dom-node node) "pre code"))
    nil
    (doseq [block (array-seq (.querySelectorAll (r/dom-node node) "pre code"))]
      (.highlightBlock hljs block))))

(defn- comments [post-id]
  [:> HyvorTalk/Embed {:websiteId 793 :id post-id :loadMode "scroll"}])

(defn post [post-id]
  (r/create-class
   {:component-did-mount highlight-block
    :reagent-render
    (fn []
      (if (nil? ((keyword post-id) (:posts @state)))
        (not-found)
        [:div.mt-12
         [:h2.text-2xl.font-medium
          (first (:title (:metadata ((keyword post-id) (:posts @state)))))]
         [:div.flex.mt-2.items-center
          [:time.text-sm.tracking-wide.mt-px (first (:date (:metadata ((keyword post-id) (:posts @state)))))]
          (tags ((keyword post-id) (:posts @state)))]
         [:article.markdown.mt-6
          {:dangerouslySetInnerHTML {:__html (:html ((keyword post-id) (:posts @state)))}}]
         [comments post-id]]))}))

(defn filter-by-tag [posts tag-id]
  (filter #(re-find (re-pattern tag-id) (first (:tags (:metadata (val %))))) posts))

;; Routing

(defn pages [path]
  (case (:handler (:current-page @state))
    :index [index (:posts @state)]
    :about [about]
    :post  [post (:post-id (:route-params (:current-page @state)))]
    :tag   [index (filter-by-tag (:posts @state) (:tag-id (:route-params (:current-page @state))))]
    :resume "https://luca.cambiaghi.me.com/resume.pdf"
    [not-found]))

(defn set-page! [match]
  (swap! state assoc :current-page match))

;; App
(defn header []
  [:header
   [:h1.text-gray-900.text-xl.leading-snug.tracking-wide
    [:a.border-b.border-transparent.hover:border-gray-900
     {:href       (bidi/path-for app-routes :index)
      :aria-label "Luca"}
     "Luca"]
    [:a.border-b.border-transparent.hover:border-gray-900
     {:href       (bidi/path-for app-routes :about)
      :aria-label "about"
      :style      {:float "right"}}
     "about"]]
   [:code.text-xs.tracking-tight
    {:aria-label "Thread thoughts, read, evaluate, print"}
    "(-> thoughts read eval print)"]])

(defn footer []
  [:div
   [:iframe {:src "html/predict-bw.html" :frame-border "0" :scrolling "no" :width "100%" :height "300"}]])

(defn app []
  [:div.container.mx-auto.max-w-4xl.m-4.p-4.mt-10.text-gray-900
   [header]
   (pages current-page)
   [footer]])

(def history
  (pushy/pushy set-page! (partial bidi/match-route app-routes)))

;; Start

(defn ^:export start! []
  (pushy/start! history)
  (r/render [app]
            (.getElementById js/document "app")))
