(ns util.file
  (:refer-clojure)
  (:import
     (java.io
      File
      FileInputStream)))

; func to generate lazy-seq of mp3s

(defn list-all-mp3s [#^String dir]
  (filter #(and (.isFile %) (.endsWith (.. % getName toLowerCase) ".mp3")) (file-seq (File. dir))))