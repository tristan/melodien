; used to generate jquery and javascripts easier
(ns jquery
  (:use clojure.contrib.str-utils))

(defn eol [line]
  (str line ";\n"))

(defn 
  #^{:test (fn []
	     (assert (= (selector) "$(this)"))
	     (assert (= (selector "test") "$(\"test\")")))}
  selector
  [& query]
  (if (nil? query)
    "$(this)"
    (str "$(\"" (str-join " " query) "\")")))

(defn fncall [name obj & inputs]
  (str obj "." name "("
       (when (not (nil? inputs))
	 (str-join ", " inputs))
       ")"))

(defn
  #^{:test (fn []
	     (assert (= (defvar "test" "0") "var test = 0"))
	     (assert (= (defvar "test" "0" true) "test = 0")))}
  defvar [name default & public]
  (str
   (when (or (nil? public) (= public false))
     "var ")
   name " = " default))

(defn function [arglist & body]
  (str "function (" (str-join "," arglist) ") {\n" (apply #'str body) "}"))

(defn
  #^{:test (fn []
	     (assert (= (j+ 1 2 3 4 5) "1+2+3+4+5"))
	     (assert (= (j+ 1 2) "1+2")))}
  j+
  ([a] a)
  ([a b] (str a "+" b))
  ([a b & more]
     (reduce j+ (str a "+" b) more)))

(defn 
  #^{:test (fn []
	     (assert (= (j+= "test" 1) "test+=1"))
	     (assert (= (j+= "test" 1 2 3 4) "test+=1+2+3+4")))}
  j+= 
  ([var a] (str var "+=" a))
  ([var a & more]
     (reduce j+ (str var "+=" a) more)))