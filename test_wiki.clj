(ns test-wiki
  (:use wiki-parser
	database
	clojure.contrib.str-utils)
)

;(println ((first (run-sql-query "select * from mp3s where artist='The The' and title='Uncertain Smile'")) :file))

(defn escape [string]
  (re-gsub #"'" "\\\\'" string))

(defn test1 []
  (println
   (count (filter
	   #(not (empty? (run-sql-query ["select * from mp3s where artist=? and title=?"
					 (:artist %) (:title %)])))
	   (let [tbl (last (first (get-tables (get-wikipedia-page "Triple_J_Hottest_100,_1989"))))]
	     (build-map (get-headings tbl) (get-tds tbl))))))
)

(defn test2 []
  (loop [tracks (let [tbl (last (first (get-tables (get-wikipedia-page "Triple_J_Hottest_100,_1989"))))]
		  (build-map (get-headings tbl) (get-tds tbl)))]
    (if (empty? tracks)
      nil
      (let [track (first tracks)]
	(println "Processing: " track)
	(println (str "select * from mp3s where artist='" 
		      (escape (:artist track))
			"' and title='" 
			(escape (:title track))))
	(println (run-sql-query ["select * from mp3s where LOWER(artist)=? and LOWER(title)=?"
				 (:artist track) (:title track)]))
	(recur (rest tracks))))))

(defn get-jjj-table-for-year [year]
  ;(let [tbl (last (first (get-tables (get-wikipedia-page (str "Triple_J_Hottest_100_of_All_Time,_" year)))))]
  (let [tbl (last (first (get-tables (get-wikipedia-page (str "Triple_J_Hottest_100,_" year)))))]
    (build-map (get-headings tbl) (get-tds tbl))))

(defn html-missing-from-years [years]
  (println "<html><body>")
  (loop [y years]
    (when (not (empty? y))
      (println "<p>" (first y) "...</p><p>\n")
      (loop [missing
	     (filter
	      #(empty? (run-sql-query ["select * from mp3s where LOWER(artist)=? and LOWER(title)=?"
				       (.toLowerCase (:artist %)) (.toLowerCase (:title %))]))
	      (get-jjj-table-for-year (first y)))]
	(when (not (empty? missing))
	  (println (:artist (first missing)) "-" (:title (first missing)) 
		   (str 
		    "<a href=\"http://en.wikipedia.org/w/index.php?title=Special:Search&redirs=0&search="
		    (str-join "+" (re-split #" " (str (:artist (first missing)) " " (:title (first missing)))))
		    "&fulltext=Search&ns0=1\">[wikipedia search]</a><br/>\n"))
	  (recur (rest missing))))
      (println "</p>")
      (recur (rest y))))
  (println "</body></html>"))

(defn build-m3u [base-path years]
  (when (not (empty? years))
    (loop [mp3s (get-jjj-table-for-year (first years))]
      (when (not (empty? mp3s))
	(let [mp3 (first (run-sql-query ["select * from mp3s where LOWER(artist)=? and LOWER(title)=?"
					 (.toLowerCase (:artist (first mp3s))) (.toLowerCase (:title (first mp3s)))]))]
	  (when (not (nil? mp3))
	    (println (str base-path (:file mp3))))
	  (recur (rest mp3s)))))
    (recur base-path (rest years))))

;(html-missing-from-years '(1991 1993 1994 1995 1996 1997 1998 1999 2000 2001))
;(build-m3u "e:\\music" '(1991 1993 1994 1995 1996 1997 1998 1999 2000 2001))
(html-missing-from-years '(2002,2003,2004,2005,2006,2007,2008))
;(build-m3u "e:\\music" '(2002,2003,2004,2005,2006,2007,2008))