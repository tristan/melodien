(ns ui.music
  (:import (org.eclipse.jface.window ApplicationWindow)
	   (org.eclipse.swt SWT)
	   (org.eclipse.swt.widgets Display
				    Composite
				    List)
	   (org.eclipse.swt.layout GridLayout
				   GridData
				   FormLayout
				   FormData
				   FormAttachment)
	   )
  (:use database))

(defn app-window []
  (doto
      (proxy [ApplicationWindow] [nil]
	(configureShell [shell]
			(proxy-super configureShell shell)
			(doto shell
			  (.setText "Melodien")
			  (.setMinimumSize 535 400)
			  (.setSize 1024 768)))
	(createContents [parent]
			; [artists][albums] list 10 entries
			; [     songs     ]
			(let [composite (doto (Composite. parent SWT/NONE) ; to hold all items
					  (.setLayout (FormLayout.)))
			      ; the list of artists
			      artists-list (doto (List. composite (reduce #'bit-or (list SWT/BORDER SWT/MULTI SWT/V_SCROLL)))
					     (.setLayoutData (let [data (FormData.)]
							       (set! (. data top) (FormAttachment. 0 0))
							       (set! (. data left) (FormAttachment. 0 0))
							       (set! (. data height) 200)
							       (set! (. data right) (FormAttachment. 50 0))
							       data)))
			      albums-list (doto (List. composite (reduce #'bit-or (list SWT/BORDER SWT/MULTI SWT/V_SCROLL)))
					     (.setLayoutData (let [data (FormData.)]
							       (set! (. data top) (FormAttachment. 0 0))
							       (set! (. data left) (FormAttachment. artists-list 0))
							       (set! (. data height) 200)
							       (set! (. data right) (FormAttachment. 100 0))
							       data)))
			      songs-list (doto (List. composite (reduce #'bit-or (list SWT/BORDER SWT/MULTI SWT/V_SCROLL)))
					     (.setLayoutData (let [data (FormData.)]
							       (set! (. data top) (FormAttachment. artists-list 0))
							       (set! (. data left) (FormAttachment. 0 0))
							       (set! (. data bottom) (FormAttachment. 100 0))
							       (set! (. data right) (FormAttachment. 100 0))
							       data)))
			      ]
			  (let [songs (database/simple-search "")]
			    (doseq [artist (distinct (map #(:artist %) songs))] 
			      (.add artists-list artist))
			    (doseq [album (sort (map #(:album %)
						     (distinct (map #(hash-map :artist (:artist %)
									       :album (:album %))
								    songs))))]
			      (.add albums-list album))
			    (doseq [song songs]
			      (.add songs-list (str (:title song) " | "
						    (:track song) " | "
						    (:artist song) " | "
						    (:album song) " | "))))
			  composite))
	)
    (.setBlockOnOpen true)
    (.open))
  (.. Display getCurrent dispose))