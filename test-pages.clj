(ns test-pages
  (:use pages))


;(println
;(rest (re-matches #"^test/([a-z]+)/([0-9]+)/$" "test/hello/99/"))
;)

(println
 (
   ((first (filter #(not (empty? (re-matches (% :regex) "test/abc/"))) patterns)) :fn)
   "abc")
)

