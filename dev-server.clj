; this runs server.clj wrapped by a file watcher so that whenever a .cjl file
; in the current directory (or child directories) is modified it will automatically
; kill the process and create a new one.

; ISSUES:
; command line isn't dynamic

(ns dev-server
  (:use clojure.set
	(clojure.contrib [duck-streams :only (reader)]))
  (:require server (utils [process :as process]))
  (:import
   (java.io File)))

(defn list-all-clj [#^String dir]
  (for [f (filter #(and (.isFile %) (.endsWith (.. % getName toLowerCase) ".clj")) (file-seq (File. dir)))]
    {:file (.getPath f) :last-modified (.lastModified f)}))

(def cmdline
     ;"\"c:\\Program Files\\Java\\jdk1.6.0_14\\bin\\java.exe\" -Xmx1024m -cp .;lib\\compojure\\commons-codec-1.3.jar;lib\\compojure\\commons-fileupload-1.2.1.jar;lib\\compojure\\commons-io-1.4.jar;lib\\compojure\\compojure.jar;lib\\compojure\\grizzly-http-servlet-1.9.10.jar;lib\\compojure\\grizzly-http-webserver-1.9.10.jar;lib\\compojure\\jetty-6.1.15.jar;lib\\compojure\\jetty-util-6.1.15.jar;lib\\compojure\\servlet-api-2.5-20081211.jar;lib\\jl1.0.1.jar;lib\\tritonus_share.jar;lib\\mp3spi1.9.4.jar;.;src;classes;D:\\cygwin\\opt\\clojure\\jline-0.9.94\\jline-0.9.94.jar;D:\\cygwin\\opt\\clojure\\clojure_895b39da.jar;D:\\cygwin\\opt\\clojure\\clojure-contrib.jar;D:\\cygwin\\opt\\db-derby-10.5.1.1-bin\\lib\\derby.jar clojure.lang.Script test-server.clj") ; command line grabbed using AdaptJ's StackTrace
     (process/get-cmdline))
; TODO: find programatic way of retreiving this

(defn file-watching-thread []
  (loop [files (set (list-all-clj "."))]
    (let [new-files (set (list-all-clj "."))]
      (if (empty? (difference files new-files))
	(do
	  ;(println "nothing has changed!")
	  (Thread/sleep 1000)
	  (recur files))
	(do
	  (println "reloading due to file changes:" (difference files new-files))
	  (System/exit 3))))))

(defn stream-reader [prefix stream]
  (with-open [rdr (reader stream)]
    (doseq [l (line-seq rdr)] (println (str prefix l)))))

(defn run []
  (if (nil? (System/getenv "RUNSERVER"))
    (loop []
      (let [p (.exec (Runtime/getRuntime) cmdline
		     (into-array String (cons "RUNSERVER=TRUE" (for [i (System/getenv)] (str (.getKey i) "=" (.getValue i))))))]
	(.start (Thread. (fn [] (stream-reader "std>>> " (.getInputStream p)))))
	(.start (Thread. (fn [] (stream-reader "err>>> " (.getErrorStream p)))))
	; fixes issue where ctrl-c leaves the server process running
	(.addShutdownHook (Runtime/getRuntime) (Thread. (fn [] (.destroy p))))
	(if (= 3 (.waitFor p))
	  (recur)
	  nil)))
    (let [fwt (.start (Thread. file-watching-thread))]
      (server/run))))

(run)
