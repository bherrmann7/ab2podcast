(ns ab2podcast.service
  (:require [io.pedestal.service.http :as bootstrap]
            [io.pedestal.service.http.route :as route]
            [io.pedestal.service.http.body-params :as body-params]
            [io.pedestal.service.http.route.definition :refer [defroutes]]
            [hiccup.page :as page]
            [ring.util.response :as ring-resp]
            [ab2podcast.catalog :as cat]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s" (clojure-version))))

(defn podcast-row [podcast]
  (let [name (first podcast)
        episodes (last podcast)]
    [:tr [:td name]
     [:td (count episodes)]
     [:td [:a {:href (str "/podcast/" (java.net.URLEncoder/encode name) ".xml")} name ".xml"]
      ]]
    ))

(defn home-page
  [request]
  (ring-resp/response (page/html5
                        [:head [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
                         [:link {:href "/bootstrap/css/bootstrap.min.css" :rel "stylesheet" :media "screen"}]
                         ]
                        [:body [:div {:class "content"} [:div {:class "span6"}
                                                         [:h2 "AudioBook to Podcast Server"]
                                                         [:script {:src "http://code.jquery.com/jquery.js"}]
                                                         [:script {:src "/bootstrap/js/bootstrap.min.js"}]

                                                         [:table {:class "table"}
                                                          [:tr [:th "Audio Book"]
                                                           [:th "Episode Count"]
                                                           [:th "Podcast URL"]
                                                           ]
                                                          (map #(podcast-row %) (cat/fetch-catalog))
                                                          ]
                                                         ]]]
                        )))

(defn make-item [episode]
  (str "        <item>\n"
       "             <title>" (get )

  <title>411 Item 138 ZedCast with Bruce Murray - Voicemail line 206-666-4357 </title>
  <link>http://podcast411.com/forums/viewtopic.php?t=451</link>
  <guid>http://media.libsyn.com/media/podcast411/411_060325.mp3</guid>
  <description> Welcome to the show it is March 25th and this is our 138th
  show.  Today will be an interview with Bruce Murray from the Zedcast
  podcast. Please visit this podcast at http://www.zedcast.com/ </description>
  <enclosure url="http://media.libsyn.com/media/podcast411/411_060325.mp3" length="11779397" type="audio/mpeg"/>
 )

(defn make-items [episodes]

  (clojure.string/join "" (map make-item episodes))

;  (str "<item>"  "block" "</item>" )
  )

(defn podcast-page [request]
  (let [cat (cat/fetch-catalog)
        request-name (.replace (:name (:path-params request)) ".xml" "")
        subscription (first (filter #(= request-name (first %)) cat ))
        episodes (get subscription 1)
    ]
    (clojure.pprint/pprint ["============================== subscription" (get (first subscription) 1) ] )

   {:status 200 :headers {"Content-type" "application/xml"} :body (str
   "<?xml version='1.0' encoding='UTF-8'?>
<rss>
  <channel>
    <title>" request-name "</title>
" (make-items episodes)
"  </channel>
</rss>") } ))

(defroutes routes
  [[["/" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params) bootstrap/html-body]
     ["/podcast/:name" {:get podcast-page}]
     ["/about" {:get about-page}]]]])

;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))

;; Consumed by ab2podcast.server/create-server
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::boostrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty or :tomcat (see comments in project.clj
              ;; to enable Tomcat)
              ;;::bootstrap/host "localhost"
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})
