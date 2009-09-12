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

(defn interpolate-colour [gc percent colour1 colour2]
  (let [dev (.getDevice gc)]
    (if (= percent 100)
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
					    (/ percent 100.0))))))))

(comment
(defn -interpolate-colour
  [ag widget reference gc time-increment run-time end-time colour1 colour2]
     (println "sent-off")
     (when (< run-time end-time)
       (send-off *agent* #'-interpolate-colour
		 widget reference gc time-increment (+ time-increment run-time) end-time colour1 colour2))
     (println "trying to get new colour")
     (let [new-colour (do-interpolate-colour gc (* (/ run-time end-time) 100) colour1 colour2)]
       (println "setting new colour @ " run-time)
       (dosync (ref-set reference new-colour))
       (.redraw widget)
       (when (< run-time end-time)
	 (.dispose new-colour))
       (. Thread (sleep time-increment)))
     nil)

(defn interpolate-colour 
  [ag widget reference gc fps run-time colour1 colour2]
     (send-off *agent* #'-interpolate-colour widget reference (/ 1000 fps) 0 run-time colour1 colour2)
     nil)
)

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
			      cb-state (ref {:state 'IDLE 
					     :colour (.getSystemColor (.getDevice (GC. (.. Display getDefault))) SWT/COLOR_RED)
					     :dispose? false})
			      cb (Canvas. comp SWT/TRANSPARENT)]
			  (.setLayoutData cb (let [data (FormData.)]
					       (set! (. data top) (FormAttachment. 0 5))
					       (set! (. data left) (FormAttachment. 0 5))
					       (set! (. data bottom) (FormAttachment. 100 -5))
					       (set! (. data right) (FormAttachment. 100 -5))
					       data))
			  (.addPaintListener cb (proxy [PaintListener] []
						  (paintControl [e]
								(letfn [(dodraw [colour]
									       (.setAlpha (. e gc) 255)
									       (.setBackground (. e gc)
											       colour)
									       (.fillRectangle (. e gc)
											       (. e x)
											       (. e y)
											       (. e width)
											       (. e height)))]
								  (if (= (:state @cb-state) 'INTERPOLATE)
								    (if (< (:run-time @cb-state) (:end-time @cb-state))
								      (let [new-colour (interpolate-colour
											(. e gc)
											(* (/ (:run-time @cb-state) (:end-time @cb-state)) 100)
											(:colour1 @cb-state)
											(:colour2 @cb-state))]
									(dodraw new-colour)
									(.dispose new-colour)
									(dosync (ref-set cb-state {:state 'INTERPOLATE
												   :run-time (+ (:run-time @cb-state) (:time-increment @cb-state))
												   :time-increment (:time-increment @cb-state)
												   :end-time (:end-time @cb-state)
												   :colour1 (:colour1 @cb-state)
												   :colour2 (:colour2 @cb-state)}))
									(.redraw cb)
									(. Thread (sleep (:time-increment @cb-state))))
								      (do
									(dosync (ref-set cb-state {:state 'IDLE
												   :colour (:colour2 @cb-state)}))
									(.redraw cb)))
								    (dodraw (:colour @cb-state))))
								)))
			  (.addMouseTrackListener cb (proxy [MouseTrackListener] []
						       (mouseHover [e])
						       (mouseEnter [e]
								   (dosync (ref-set cb-state {:state 'INTERPOLATE
											      :run-time 0
											      :time-increment 10
											      :end-time 500
											      :colour1 (.getSystemColor (.getDevice (GC. (.. Display getDefault))) SWT/COLOR_RED)
											      :colour2 (.getSystemColor (.getDevice (GC. (.. Display getDefault))) SWT/COLOR_BLUE)}))
								   (.redraw cb))								     
						       (mouseExit [e]
								  (dosync (ref-set cb-state {:state 'IDLE
											     :colour (.getSystemColor (.getDevice (GC. (.. Display getDefault))) SWT/COLOR_RED)}))
								  (.redraw cb))))
			  cb))
	)
    (.setBlockOnOpen true)
    (.open))
  (.. Display getCurrent dispose))