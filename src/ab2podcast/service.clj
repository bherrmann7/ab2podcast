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

(defn fetch-page [request]
  (let [file (str (cat/find-start-dir) (java.net.URLDecoder/decode (:path (:path-params request))))]
    (if (.exists (new java.io.File file))
      {
        :status 200
        :headers {"Content-Type" "application/mp3"}
        :body (java.io.FileInputStream. file)
        }

      {:status 200 :headers {"Content-type" "text/plain"} :body (str "Missing: " file)})
    ))

(defn escape-html
  "Change special characters into HTML character entities."
  [text]
  (.. #^String (.toString text)
    (replace "&" "&amp;")
    (replace "<" "&lt;")
    (replace ">" "&gt;")
    (replace "\"" "&quot;")))

(defn make-item [episode]
  (let [path (java.net.URLEncoder/encode (subs (.toString (get episode 1)) (count (cat/find-start-dir))))]

    (str
      "        <item>
                   <title>" (escape-html (get episode 0)) "</title>
             <enclosure url='/fetch/" path "' length='" (.length (get episode 1)) "' type='audio/mpeg'/>
        </item>
        "
      )))

(defn make-items [episodes]

  (clojure.string/join "" (map #(make-item %) episodes))
  ;  (str "<item>"  "block" "</item>" )
  )

(defn podcast-page [request]
  (let [cat (cat/fetch-catalog)
        request-name (java.net.URLDecoder/decode (.replace (:name (:path-params request)) ".xml" ""))
        subscription (first (filter #(= request-name (first %)) cat))
        episodes (get subscription 1)
        ]
    (clojure.pprint/pprint ["============================== subscription" request-name (get (first subscription) 1)])

    {:status 200 :headers {"Content-type" "application/xml"} :body (str
                                                                     "<?xml version='1.0' encoding='UTF-8'?>
                                                                  <rss>
                                                                    <channel>
                                                                      <title>" request-name "</title>
" (make-items episodes)
                                                                     "  </channel>
                                                                     </rss>")}))

(defroutes routes
  [[["/" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params) bootstrap/html-body]
     ["/podcast/:name" {:get podcast-page}]
     ["/fetch/:path" {:get fetch-page}]
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
