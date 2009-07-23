(ns utils.mp3
  (:refer-clojure)
  (:use [clojure.contrib.duck-streams :only (append-spit)])
  (:import
   (java.io File)
   (javax.sound.sampled AudioSystem)
   (org.tritonus.share.sampled TAudioFormat)
   (org.tritonus.share.sampled.file TAudioFileFormat)))

(defn get-id3 [#^File mp3]
  (try
   (let [props (.properties (AudioSystem/getAudioFileFormat mp3))]
     (let [track (try
		  (Integer/parseInt (.get props "mp3.id3tag.track"))
		  (catch java.lang.NumberFormatException _ nil))
	   disc (try
		 (Integer/parseInt (.get props "mp3.id3tag.disc"))
		 (catch java.lang.NumberFormatException _ nil))
	   date (try
		 (Integer/parseInt (.get props "date"))
		 (catch java.lang.NumberFormatException _ nil))
	   res {:title (or (.get props "title") "")
		:artist (or (.get props "author") "")
		:album (or (.get props "album") "")
		}]
       (let [res (if (nil? track) res (assoc res :track track))]
	 (let [res (if (nil? disc) res (assoc res :disc disc))]
	   (let [res (if (nil? date) res (assoc res :date date))]
	     res)))))
   (catch java.io.EOFException _
     (do
       ; some mp3s that are corrupt throw eof exceptions
       (append-spit "eof.log" (str (.getPath mp3) "\n"))
       {}))))
; had to use lots of lets here to avoid adding nil values to the map
; which seemed to break sql/insert-records