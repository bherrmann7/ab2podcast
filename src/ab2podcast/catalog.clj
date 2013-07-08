(ns ab2podcast.catalog
  (:require [clojure.pprint]
            ))

(defn scrub [podcast file]
  (let [file-string (.toString file)]
    ;(println (str "Scrub "  file))
    [(.replace (.replace (subs file-string (+ 1 (count (.toString podcast)))) "/" " ") ".mp3" "") file]
    ))

(defn make-podcast-directory [podcast start-dir]
  [(.getName podcast) (map #(scrub podcast %) (sort (keep #(if (.endsWith (.getName %) ".mp3") %) (file-seq podcast))))]
  )


(defn find-start-dir []
  (let [home-dir (System/getProperty "user.home")
        ubuntu-dir (str home-dir "/ab")
        osx-dir (str home-dir "/Music/iTunes/iTunes Media/Music/")]
    (if (.isDirectory (clojure.java.io/file osx-dir))
      osx-dir
      ubuntu-dir)
    )
  )

(defn fetch-catalog []
  (let [start-dir (find-start-dir)
        directory (clojure.java.io/file start-dir)
        list (.listFiles directory)
        podcasts (keep #(if (.isDirectory %) %) (sort list))
        pcasts (map #(make-podcast-directory % start-dir) podcasts)]
    (println (str ":::::::::::::::::::::; fetch-catalog called.  start-dir:" start-dir) )
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
    (println (str "Name:     ") (get subscription 0))
    (println (str "Episodes: ") (count (get subscription 1)))
    ))