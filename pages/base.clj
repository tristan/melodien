(ns pages.base
  (:use compojure))

(defn render [& body]
  (html
   [:html
    [:head
     [:title "Melodien"]]
    [:body
     body]]))