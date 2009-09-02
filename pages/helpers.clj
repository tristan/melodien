(ns pages.helpers
  (:use compojure.html.page-helpers
	clojure.contrib.str-utils
	[compojure.html.gen :only (html)]))

(defn embed-js [& script]
  (str-join "\n" (map #(html (javascript-tag (slurp %))) script)))