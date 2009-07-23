(ns utils.process
  (:import (java.lang.management ManagementFactory))
  (:use (clojure.contrib [duck-streams :only (reader writer)])))

(defn get-cmdline []
  (let [pid (last (first (re-seq #"([0-9]+)@" (.. ManagementFactory getRuntimeMXBean getName))))]
    (let [p (.exec (Runtime/getRuntime) (str "wmic PROCESS where processid=" pid " get Commandline /value")
		   (into-array String (for [i (System/getenv)] (str (.getKey i) "=" (.getValue i)))))]
      (let [w (writer (.getOutputStream p))]
	(.println w "testing")
	(.close w))
      (let [r (with-open [rdr (reader (.getInputStream p))]
		(apply #'str (for [l (line-seq rdr)] l)))] ; have to run str on all so we don't return a lazy-seq.
	(.waitFor p)
	(last (first (re-seq #"CommandLine=([\s\S]+)" r)))))))
