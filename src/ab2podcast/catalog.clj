(ns ab2podcast.catalog
  (:require [clojure.pprint]
            ))

(defn scrub [podcast file]
  (let [ file-string (.toString file)]
    [ (.replace (.replace (subs file-string (+ 1 (count (.toString podcast)))) "/" " ") ".mp3" "") file ]
    ))

(defn make-podcast-directory [podcast start-dir]
  [ (.getName podcast) (map #(scrub podcast %) (sort (keep #(if-not (.isDirectory %) % ) (file-seq podcast))))]
  )


(defn fetch-catalog []
  (let [start-dir (str (System/getProperty "user.home") "/ab")
        directory (clojure.java.io/file start-dir)
        list (.listFiles directory)
        podcasts (keep #(if (.isDirectory %) %) (sort list))
        pcasts (map #(make-podcast-directory % start-dir) podcasts)]
    (println ":::::::::::::::::::::; fetch-catalog called")
;    (clojure.pprint/pprint pcasts)
;    (println (count pcasts))
    (println (str "Name:     " (first (first pcasts))))
    (println (str "Episodes: " (count (last (first pcasts)))))
       pcasts
    )
  )

(let [catalog (fetch-catalog)]
  (clojure.pprint/pprint catalog)
  (doseq [subscription catalog]
    (println (str "Name:     " ) (get subscription 0))
    (println (str "Episodes: " ) (count (get subscription 1)))
    ))