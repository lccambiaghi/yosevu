(ns app.feed
  (:require [clj-rss.core :as rss]
            [app.resource :as rc]))

(defn rss-entry [{:keys [html] {:keys [title subtitle date id tags]} :metadata}]
  {:title       (first title)
   :link        (str "https://luca.cambiaghi.me/" (first id))
   :guid        (str "https://luca.cambiaghi.me/" (first id))
   :pubDate     (.parse (java.text.SimpleDateFormat."yyyy-MM-dd") (first date))
   ;; :description     (first subtitle)
   :description (apply str "<![CDATA[" html "]]>")
   })

(defn rss-feed []
  (let [posts   (rc/get-posts "src/posts")
        entries (map rss-entry (vals posts))]
    (rss/channel-xml {:title "Luca Cambiaghi's blog" :link "https://luca.cambiaghi.me" :description "Content of Luca Cambiaghi's blog"}
                     entries)))

(spit "public/feed.xml" (rss-feed))
