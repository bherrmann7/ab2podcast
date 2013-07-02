(ns ab2podcast.service
  (:require [io.pedestal.service.http :as bootstrap]
            [io.pedestal.service.http.route :as route]
            [io.pedestal.service.http.body-params :as body-params]
            [io.pedestal.service.http.route.definition :refer [defroutes]]
            [hiccup.page :as page]
            [ring.util.response :as ring-resp]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s" (clojure-version))))

(defn home-page
  [request]
  (ring-resp/response (page/html5
                        [:head [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
                         [:link {:href "/bootstrap/css/bootstrap.min.css" :rel "stylesheet" :media "screen"}]
                         ]
                        [:body [:div {:class "content"}  [:div {:class "span6" }
                                [:h2 "AudioBook to Podcast Server"]
                                [:script {:src "http://code.jquery.com/jquery.js"}]
                                [:script {:src "/bootstrap/js/bootstrap.min.js"}]

                                [:table {:class "table"}
                                 [:tr [:th "Audio Book"]
                                  [:th "Podcast URL"]
                                  ]
                                 ]
                                ]]]
                        )))

(defroutes routes
  [[["/" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params) bootstrap/html-body]
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
