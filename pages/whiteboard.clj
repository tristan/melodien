(ns pages.whiteboard
  (:use compojure
	pages.helpers)
  (:require [database :only (run-sql-query)]
	     jquery))
	    

(defn select-multiple [options]
nil)

(defn artists []
  (html
   [:html 
    [:head
     (include-js "/javascript/jquery-1.3.2.min.js")]
    [:body
     [:select {:multiple true :size 10 :id "artists-select"}
      (select-options 
       (map #(vector (% :artist) (% :artist)) 
	    (database/run-sql-query "select distinct artist from mp3s order by artist")))]
     [:select {:id "albums-select" :multiple true :size 10}
      (select-options 
       (map #(vector (% :album) (% :album)) 
	    (database/run-sql-query "select distinct album from mp3s order by album")))]
     [:div {:id "albums-div"} "None"]
     (embed-js "js/embedded/whiteboard1.js")
      ]]))