(ns test-jquery
  (:use jquery))

(defn jquery-example []
  (eol
   (trigger "change"
	    (trigger "change" 
		     (selector "select") 
		     (function '()
			       (eol (defvar "str" "\"\""))
			       (eol
				(trigger "each"
					 (selector "select option:selected")
					 (function '()
						   (eol
						    (j+= "str"
							 (trigger "text" (selector))
							 "\" \"")))))
			       (eol (trigger "text" (selector "div") "str")))))))

(test #'selector)
(test #'j+)
(test #'j+=)
(test #'defvar)