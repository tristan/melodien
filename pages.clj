(ns pages
  (:require pages.whiteboard))

(defn whiteboard []
  (pages.whiteboard/artists))

(defn testview [txt]
  (str "<html><body>" txt "</body></html>"))
  

(def patterns (list
		{:regex #"^whiteboard/" :fn whiteboard}
		{:regex #"^test/([a-z]+)/$" :fn (fn [txt] (testview txt))}
		))

(defn dispatch [path]
  (let [pattern (first (filter #(not (empty? (re-matches (% :regex) path))) patterns))]
    (if (nil? pattern)
      (do
	(println "PAGE NOT FOUND: " path) 
	nil)
      (let [parameters (rest (re-matches (pattern :regex) path))]
	(apply (pattern :fn) parameters)))))
  