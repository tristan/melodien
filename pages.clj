(ns pages
  (:require pages.whiteboard)
  (:use [compojure.http.helpers :only (serve-file)]))

(defn whiteboard []
  (pages.whiteboard/artists))

(defn testview [txt]
  (str "<html><body>" txt "</body></html>"))
  

(def patterns (list
		{:regex #"^whiteboard/" :fn (fn [] (whiteboard))}
		{:regex #"^test/([a-z]+)/$" :fn (fn [txt] (testview txt))}
		{:regex #"^javascript/([\s\S]+)$" :fn (fn [path] (serve-file "js" path))}
		))

(defn dispatch [path]
  (let [pattern (first (filter #(not (empty? (re-matches (% :regex) path))) patterns))]
    (if (nil? pattern)
      (do
	(println "PAGE NOT FOUND: " path) 
	nil)
      (let [matches (re-matches (pattern :regex) path)]
	(let [parameters (if (vector? matches) (rest matches) (list))]
	  (apply (pattern :fn) parameters))))))
  