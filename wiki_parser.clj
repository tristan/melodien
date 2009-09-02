(ns wiki-parser
  (:use clojure.contrib.duck-streams
	clojure.contrib.str-utils))

(defn get-wikipedia-page [page]
  (slurp* (str "http://en.wikipedia.org/wiki/" page)))


(defn get-tables [page]
;  (re-find #"</?\w((\s+\w+(\s*=\s*(?:\".*?\"|'.*?'|[^'\">\s]+))?)+\s*|\s*)/?>" page))
  (re-seq #"<table(\s+[^>]*)?class=\"wikitable sortable\"(\s+[^>]*)?>([\s\S]*?)</table>" page))

(defn remove-unwanteds [stuff]
  (re-gsub #"\"" ; in 2008 they decided it would be fun to put "s around everything
	   ""
	   (re-gsub #"\[.*?\]"
		    ""
		    (re-gsub #"[<]/?\w+(\s+[^>]*)?[>]" 
			     ""
			     stuff))))

(defn get-headings [table]
  (map #(remove-unwanteds (last %))
       (re-seq #"<th>([\s\S]*?)</th>" table)))

(defn get-tds [table]
  (map #(remove-unwanteds (last %))
       (re-seq #"<td>([\s\S]*?)</td>" table)))

(defn str-to-key [key]
  (get {"#" :#
	"Song" :title
	"Artist" :artist
	"Country of Origin" :country
	"Year of release" :release-date}
       key key))

(defn list-to-map [lst]
  (if (empty? lst)
    {}
    (assoc (list-to-map (rest lst)) (str-to-key (first (first lst))) (last (first lst)))))

(defn build-map [headings tds]
  (if (empty? tds)
    '()
    (lazy-seq
     (cons (list-to-map
	    (map #(vector %1 %2) headings (take (count headings) tds)))
	    (build-map headings (drop (count headings) tds))))))