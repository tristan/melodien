(ns utils
  (:refer-clojure)
  (:use clojure.contrib.str-utils)
  (:require (utils [mp3 :as mp3] [md5 :as md5]))
  (:import
     (java.io File)))

; func to generate lazy-seq of mp3s

(defn list-all-mp3s [#^String dir]
  (filter #(and (.isFile %) (.endsWith (.. % getName toLowerCase) ".mp3")) (file-seq (File. dir))))

(defn cleanup-file-path [s e]
  "helper function used by scan-for-mp3s"
  (let [split (re-split (re-pattern (re-gsub #"\." "\\\\." (re-gsub #"\\" "\\\\\\\\" s))) e)]
    (if (= 1 (count split))
      e
      (if (= 2 (count split))
	(last split)
	(str-join s (drop 1 split))))))

(defn scan-for-mp3s 
  "Recursivle scans a directory for mp3s and produces a lazy seq of
   maps containing id3 tag info filename and md5 hash"
  ([#^String dir] (scan-for-mp3s dir (utils/list-all-mp3s dir)))
  ([dir mp3s]
      (if (empty? mp3s)
	nil
	(let [f (merge 
		 (mp3/get-id3 (first mp3s)) 
		 {:file (cleanup-file-path dir (.getPath (first mp3s)))
		  :md5 (md5/md5-sum-file (first mp3s))
		  })]
	  (lazy-seq
	   (cons f (scan-for-mp3s dir (rest mp3s))))))))