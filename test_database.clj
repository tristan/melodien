(ns test-database
  (:use database))

;(println (database/list-artists))

(loop [a (database/simple-search "Perfect")]
  (when (not (empty? a))
    (println (first a))
    (recur (rest a))))

(time (database/run-sql-query ["select * from mp3s"]))

(time
 (do
   (database/list-artists)
   (database/list-albums)
   (database/run-sql-query ["select * from mp3s"])))

; this test wins in speed, will remove functions from the above test in next commit
(time
 (do
   (let [l (database/run-sql-query ["select * from mp3s"])]
     (distinct (map #(:artist %) l))
     (distinct (map #(:album %) l)))))

