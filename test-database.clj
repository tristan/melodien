; missing includes......

(defn print-mp3s []
  (sql/with-connection db
		       (sql/with-query-results res ["select * from mp3s where artist='Rancid'"]
					       (println res))))

(sql/with-connection db (sql/transaction (drop-mp3-table) (create-mp3-table)))
(add-mp3s-from-folder "e:\\music")
(println "adding complete!!")
(print-mp3s)