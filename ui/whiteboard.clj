(ns ui.whiteboard
  (:import (org.eclipse.swt SWT)
	   (org.eclipse.jface.window ApplicationWindow)
	   (org.eclipse.swt.widgets Display
				    Shell
				    Canvas
				    Composite)
	   (org.eclipse.swt.events MouseAdapter
				   MouseTrackListener
				   PaintListener)
	   (org.eclipse.swt.graphics LineAttributes
				     Color
				     GC)
	   (org.eclipse.swt.layout ;GridLayout
				   ;GridData
				   FormLayout
				   FormData
				   FormAttachment)
	   ))

(comment 
(defn close-button [parent]
  (let [cb (Canvas. parent SWT/TRANSPARENT)]
    (.setForeground cb (.getSystemColor (.getDisplay parent) SWT/COLOR_WHITE))
    (set! (. cb alpha) 0)
    (.addMouseListener cb
		       (proxy [MouseAdapter] []
			 (mouseDown [e]
				    (.dispose parent)
				    )))
    (.addPaintListener cb
		       (proxy [PaintListener] []
			 (paintControl [e]
				       (let [width (.. cb getBounds width)
					     height (.. cb getBounds height)
					     offset (/ width 3)
					     base-fg (. cb getForeground)
					     darker-fg (Color. (.getDevice (. e gc))
							       (/ (. base-fg getRed) 2)
							       (/ (. base-fg getGreen) 2)
							       (/ (. base-fg getBlue) 2))
					     ]
					 (doto (. e gc)
					   (.setAntialias SWT/ON)
					   (.setAlpha (. cb alpha))
					   (.setBackground 
					    (.getSystemColor (.getDevice (. e gc))
							     SWT/COLOR_BLACK))
					   (.fillOval 1 1 (- width 3) (- height 3))
					   (.setLineAttributes (LineAttributes. (float 2.0)))
					   (.setForeground darker-fg)
					   (.drawOval 1 1 (- width 3) (- height 3))
					   (.setForeground (.getSystemColor (.getDevice (. e gc))
									    SWT/COLOR_WHITE))
					   (.setLineAttributes (LineAttributes. (float 6.0)
										SWT/CAP_ROUND
										SWT/JOIN_ROUND))
					   (.drawLine offset offset (- width offset 1)
						      (- height offset 1))
					   (.drawLine (- width offset 1) offset offset
						      (- height offset 1))
					   (.setForeground base-fg)
					   (.setLineAttributes (LineAttributes. (float 4.2)
										SWT/CAP_ROUND
										SWT/JOIN_ROUND))
					   (.drawLine offset offset (- width offset 1)
						      (- height offset 1))
					   (.drawLine (- width offset 1) offset offset
						      (- height offset 1))
					   )
					 (.dispose darker-fg)))))
    cb))
		       
    
		) ; /comment	 

(defn do-interpolate-colour [percent colour1 colour2]
  (if (= percent 100)
    colour2
    (Color. (.getDevice (GC. (.. Display getDefault)))
	    (int (- (.getRed colour1) (* (- (.getRed colour1)
					    (.getRed colour2))
					 (/ percent 100.0))))
	    (int (- (.getBlue colour1) (* (- (.getGreen colour1)
					     (.getGreen colour2))
					  (/ percent 100.0))))
	    (int (- (.getBlue colour1) (* (- (.getBlue colour1)
					     (.getBlue colour2))
					  (/ percent 100.0)))))))

(defn interpolate-colour 
  ([widget reference fps run-time colour1 colour2]
     (interpolate-colour widget reference (/ 1000 fps) 0 run-time colour1 colour2))
  ([widget reference time-increment run-time end-time colour1 colour2]
     (when (< run-time end-time)
       (send-off *agent* #'interpolate-colour
		 widget reference time-increment (+ time-increment run-time) end-time colour1 colour2))
     (let [new-colour (do-interpolate-colour (* (/ run-time end-time) 100) colour1 colour2)]
       (dosync (ref-set reference new-colour))
       (when (< run-time end-time)
	 (.dispose new-colour))
       (. Thread (sleep time-increment)))))

(defn window []
  (doto
      (proxy [ApplicationWindow] [nil]
	(configureShell [shell]
			(proxy-super configureShell shell)
			(doto shell
			  (.setText "Whiteboard")
			  ))
	(createContents [parent]
			(let [comp (doto (Composite. parent SWT/NONE)
				     (.setLayout (FormLayout.)))
			      cb-colour (ref (.getSystemColor (.getDevice (GC. (.. Display getDefault))) SWT/COLOR_RED))
			      cb (Canvas. comp SWT/TRANSPARENT)]
			  (.setLayoutData cb (let [data (FormData.)]
					       (set! (. data top) (FormAttachment. 0 5))
					       (set! (. data left) (FormAttachment. 0 5))
					       (set! (. data bottom) (FormAttachment. 100 -5))
					       (set! (. data right) (FormAttachment. 100 -5))
					       data))
			  (.addPaintListener cb (proxy [PaintListener] []
						  (paintControl [e]
								(.setAlpha (. e gc) 255)
								(.setBackground (. e gc)
										@cb-colour)
								(.fillRectangle (. e gc)
										(. e x)
										(. e y)
										(. e width)
										(. e height)))))
			  (.addMouseTrackListener cb (proxy [MouseTrackListener] []
						       (mouseHover [e])
						       (mouseEnter [e]
								   (let [dev (.getDevice (GC. (.. Display getDefault)))]
								     (letfn [(animation [colour1 colour2 percent wait]
										      (when (not (= percent 100))
											(send-off *agent* #'animation
												  colour1 colour2
												  (+ percent 10)
												  wait))
										      (let [new-colour (if (= percent 100)
													 colour2
													 (Color. dev
													       (int (- (.getRed colour1) (* (- (.getRed colour1)
																	       (.getRed colour2))
																	    (/ percent 100.0))))
													       (int (- (.getBlue colour1) (* (- (.getGreen colour1)
																		(.getGreen colour2))
																	     (/ percent 100.0))))
													       (int (- (.getBlue colour1) (* (- (.getBlue colour1)
																		(.getBlue colour2))
																	     (/ percent 100.0))))))]
											(dosync (ref-set cb-colour new-colour))
											(.redraw cb)
											(when (not (= percent 100))
											  (.dispose new-colour))
											(. Thread (sleep wait))))]
								       (send-off (agent nil) #'animation (.getSystemColor dev SWT/COLOR_RED)
										 (.getSystemColor dev SWT/COLOR_BLUE)
										 10
										 100))))
						       (mouseExit [e]
								  (dosync (ref-set cb-colour SWT/COLOR_RED))
								  (.redraw cb))))
			  cb))
	)
    (.setBlockOnOpen true)
    (.open))
  (.. Display getCurrent dispose))