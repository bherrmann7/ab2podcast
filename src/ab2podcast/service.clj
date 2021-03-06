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

(defn mk-link [request & more]
  (clojure.string/join "" (cons (:context-path request) more))
  )

(defn podcast-row [request podcast]
  (let [name (first podcast)
        episodes (last podcast)]
    [:tr [:td name]
     [:td (count episodes)]
     [:td [:a {:href (mk-link request "/podcast/" (java.net.URLEncoder/encode name) ".xml")} name ".xml"]
      ]]
    ))

(defn home-page
  [request]
  (ring-resp/response (page/html5
                        [:head [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
                         [:link {:href "http://netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/css/bootstrap-combined.min.css" :rel "stylesheet" :media "screen"}]
                         ]
                        [:body [:div {:class "content"} [:div {:class "span6"}
                                                         [:h2 "AudioBook to Podcast Server"]
                                                         [:script {:src "http://code.jquery.com/jquery.js"}]
                                                         [:script {:src "http://netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/js/bootstrap.min.js"}]

                                                         [:table {:class "table"}
                                                          [:tr [:th "Audio Book"]
                                                           [:th "Episode Count"]
                                                           [:th "Podcast URL"]
                                                           ]
                                                          (map #(podcast-row request %) (cat/fetch-catalog))
                                                          ]
                                                         ]]]
                        )))

(defn fetch-page [request]
  (let [file (str (cat/find-start-dir) (java.net.URLDecoder/decode (:path (:path-params request))))]
    (println (str "Fetching:" file))
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


(defn file-parts [path]
  (let [
         file-part (first path)
         path-parts (rest path)
         parent-file (.getParentFile file-part)
         ]
    (if parent-file
      (file-parts (concat (list parent-file file-part) path-parts))
      (concat (list file-part) path-parts))))

(defn encode-name [file]
  (java.net.URLEncoder/encode (.getName file))
  )

(defn encode-path [file]
  (let [en-path (clojure.string/join "/" (map #(encode-name %) (file-parts (list (new java.io.File file)))))]
    ;(println (str "Encoded path is " en-path))
    en-path
    ))

(defn make-item [request episode host]
  (let [path (encode-path (subs (.toString (get episode 1)) (count (cat/find-start-dir))))
        full-path (str "http://" host (:context-path request) "/fetch/" path )]

    (str
      "        <item>
                   <title>" (escape-html (get episode 0)) "</title>
             <enclosure url='" full-path "' length='" (.length (get episode 1)) "' type='audio/mpeg'/>
        </item>
        "
      )))

(defn make-items [request episodes host]

  (clojure.string/join "" (map #(make-item request % host) episodes))
  ;  (str "<item>"  "block" "</item>" )
  )

(defn podcast-page [request]
  (let [cat (cat/fetch-catalog)
        request-name (java.net.URLDecoder/decode (.replace (:name (:path-params request)) ".xml" ""))
        subscription (first (filter #(= request-name (first %)) cat))
        episodes (get subscription 1)
        host (get (:headers request) "host")
        ]
    (clojure.pprint/pprint ["============================== subscription" request-name (get (first subscription) 1)])

    {:status 200 :headers {"Content-type" "application/xml"} :body (str
                                                                     "<?xml version='1.0' encoding='UTF-8'?>
                                                                     <rss version='2.0'>
                                                                                                                                         <channel>
                                                                                                                                          <description>Audiobook 2 Podcast: " request-name "</description>
                                                                     <link>https://github.com/bherrmann7/ab2podcast</link>
                                                                      <title>" request-name "</title>
" (make-items request episodes host)
                                                                     "  </channel>
                                                                     </rss>")}))

(defroutes routes
  [[["/" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params) bootstrap/html-body]
     ["/podcast/:name" {:get podcast-page}]
     ["/fetch/*path" {:get fetch-page}]
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
