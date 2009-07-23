(ns utils.md5
  (:refer-clojure)
  (:import
     (java.security 
       NoSuchAlgorithmException
       MessageDigest)
     (java.io
      File
      FileInputStream)
     (java.nio
      ByteBuffer)
     (java.math BigInteger)))

(defn md5-sum
  "Compute the hex MD5 sum of a string."
  [#^String str]
  (let [alg (doto (MessageDigest/getInstance "MD5")
              (.reset)
              (.update (.getBytes str)))]
    (try
      (.toString (new BigInteger 1 (.digest alg)) 16)
      (catch NoSuchAlgorithmException e
        (throw (new RuntimeException e))))))

(defn md5-sum-file
  [#^File file]
  (let [channel (.getChannel (FileInputStream. file))
	buf (ByteBuffer/wrap (make-array Byte/TYPE 512))
	digest (MessageDigest/getInstance "MD5")]
    (.reset digest)
    (loop [bytes-read (.read channel buf)]
      (.rewind buf)
      (if (neg? bytes-read)
	; finalise
	(try
	 (.toString (BigInteger. 1 (.digest digest)) 16)
	 (catch NoSuchAlgorithmException e
	   (throw (RuntimeException. e))))
	(let [bytes (if (< bytes-read 512)
		      (let [new-buf (make-array Byte/TYPE bytes-read)]
			(.get buf new-buf 0 bytes-read)
			new-buf)
		      (.array buf))]
	  ;(println bytes-read (count bytes))
	  (.update digest bytes)
	  (.rewind buf)
	  (recur (.read channel buf)))))))