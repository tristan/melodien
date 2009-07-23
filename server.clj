(ns server
  (:use compojure
	clojure.contrib.str-utils
	[database :only (db)])
  (:require (clojure.contrib [sql :as sql])))

(defroutes greeter
  (GET "/"
    (html [:h1 "Hello World!!!"]
	  [:p {:class "test"} (sql/with-connection db
		(sql/with-query-results 
		 res ["select distinct artist from mp3s where artist like 'A%' OR artist like 'a%'"] 
		 (str-join ", " res)))])))

(defn run []
  (run-server {:port 8080}
	      "/*" (servlet greeter)))