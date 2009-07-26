
"
database structure:

mp3s
----
id (key)
song name
artist
album
track-no
disc-no
file-location : file location is relative to a specified music storage directory
                to ensure portability
md5-hash
comments

tags-table
----------
tag-name
mp3-id

playlists
---------
id (key)
name
comments

"
(ns database
  (:refer-clojure)
  (:require (clojure.contrib [sql :as sql])
	    utils))

(def db {:classname "org.apache.derby.jdbc.EmbeddedDriver"
         :subprotocol "derby"
         :subname "music.ting.db"
         :create true})

(defn create-mp3-table []
  (sql/create-table
   :mp3s
   [:id :int "PRIMARY KEY" "GENERATED ALWAYS AS IDENTITY"]
   [:title "varchar(256)"]
   [:artist "varchar(256)"]
   [:album "varchar(256)"]
   [:date :int]
   [:track :int]
   [:disc :int]
   [:file "varchar(1024)"]
   [:md5 "varchar(32)"]))

(defn drop-mp3-table []
  (try
   (sql/drop-table :mp3s)
   (catch Exception _)))

(defn add-mp3s-from-folder [dir]
  (sql/with-connection db
    (doseq [mp3s (utils/scan-for-mp3s dir)] (sql/insert-records :mp3s mp3s))))

(defn run-sql-query [query]
  (sql/with-connection db
    (sql/with-query-results res [query] (doall res))))