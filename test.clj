(require 'utils)
(require '(utils [md5 :as md5]))
(require '(utils [mp3 :as mp3]))
(use 'clojure.contrib.str-utils)

(println (utils/scan-for-mp3s "."))
(println (count (utils/list-all-mp3s "e:\\music")))

(loop [mp3s (utils/scan-for-mp3s "e:\\music")]
  (when-not (empty? mp3s)
    (println (first mp3s))
    (recur (rest mp3s))))