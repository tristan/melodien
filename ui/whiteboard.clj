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
	      (max 0 (min 255 (int (- (.getRed colour1) (* (- (.getRed colour1)
					      (.getRed colour2))
					   (/ percent 100.0))))))
	      (max 0 (min 255 (int (- (.getGreen colour1) (* (- (.getGreen colour1)
					       (.getGreen colour2))
					    (/ percent 100.0))))))
	      (max 0 (min 255 (int (- (.getBlue colour1) (* (- (.getBlue colour1)
					       (.getBlue colour2))
					    (/ percent 100.0))))))))))

(defmacro get-system-colour [colour]
  (concat (list '.getSystemColor) (list (list '.getDevice (list 'GC. (list '.. 'Display 'getDefault))) colour)))

(defn play [widget to from]
  (let [tocolour (if (instance? java.lang.Integer to) (get-system-colour to) to)
	fromcoluor (if (instance? java.lang.Integer from) (get-system-colour from) from)]))

(defmacro colours-equal? [c1 c2]
  (list '= (list '.getRGB c1) (list '.getRGB c2)))

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
					     :colour (get-system-colour SWT/COLOR_WHITE)})
			      cb (Canvas. comp SWT/TRANSPARENT)]
			  (.setLayoutData cb (let [data (FormData.)]
					       (set! (. data top) (FormAttachment. 0 5))
					       (set! (. data left) (FormAttachment. 0 5))
					       (set! (. data bottom) (FormAttachment. 100 -5))
					       (set! (. data right) (FormAttachment. 100 -5))
					       data))
			  (.addPaintListener cb (proxy [PaintListener] []
						  (paintControl [e]
								;(println (.getSyncThread (.getDisplay cb)))
								(let [interpolating? (= (:state @cb-state) 'INTERPOLATING-COLOR)
								      ; this often is over 100%, dealt with below
								      percent-done (and interpolating? (* 100.0 (/ (- (System/currentTimeMillis)
														      (:start-time @cb-state))
														   (:run-time @cb-state))))
														      
								      colour (if interpolating?
									       (interpolate-colour (. e gc)
												   ; use min to overcome values over 100%
												   (min 100.0 percent-done)
												   (:start-colour @cb-state)
												   (:end-colour @cb-state))
									       (:colour @cb-state))]
								  ;(println interpolating? percent-done colour)
								  (letfn [(dodraw [colour]
										  (.setAlpha (. e gc) 255)
										  (. (. e gc) setBackground
										     colour)
										  (.fillRectangle (. e gc)
												  (. e x)
												  (. e y)
												  (. e width)
												  (. e height)))]
								    (dodraw colour)
								    (when interpolating?
								      (when (not (or (colours-equal? colour (:start-colour @cb-state))
										   (colours-equal? colour (:end-colour @cb-state))))
									(.dispose colour))
								      (if (and interpolating? (>= percent-done 100))
									(dosync (ref-set cb-state {:state 'IDLE
												   :colour (:end-colour @cb-state)}))
									; resize during interpolate causes this to be called too much
									; needs fix
									;(.asyncExec (.getDisplay cb)
									; fixed: timerExec either eats the exceptions or is smart and
									; avoids them. should probably figure out which
									(.timerExec (.getDisplay cb)
										    (/ 1000 (:target-fps @cb-state))
										    (proxy [Runnable] []
											(run []
											     (when (not (.isDisposed cb))
											       ;(. Thread (sleep (/ 1000 (:target-fps @cb-state))))
											       (.redraw cb)))))))))
								  )))
			  (.addMouseTrackListener cb (proxy [MouseTrackListener] []
						       (mouseHover [e])
						       (mouseEnter [e]
								   ;(play cb cb-state 'setBackground (get-system-color SWT/COLOR_BLUE) 
								   ;(get-system-color SWT/COLOR_RED))
								   ;(println (= (get-system-color SWT/COLOR_RED) (.getBackground cb)))
								   ;(println (.getBackground cb))
								   ;(println (get-system-color SWT/COLOR_RED))
								   (let [idle? (= (:state @cb-state) 'IDLE)]
								     (dosync (ref-set cb-state {:state 'INTERPOLATING-COLOR
												:start-time (if idle?
													      (System/currentTimeMillis)
													      (- (System/currentTimeMillis)
														 (- (+ (:start-time @cb-state)
														       (:run-time @cb-state))
														    (System/currentTimeMillis))))
												:target-fps 8
												:run-time 5000
												:start-colour (get-system-colour SWT/COLOR_WHITE)
												:end-colour (get-system-colour SWT/COLOR_BLACK)})))
								   (.redraw cb))
						       (mouseExit [e]
								  ;(dosync (ref-set cb-state {:state 'IDLE
								  ;:colour (get-system-colour SWT/COLOR_RED)}))
								  (let [idle? (= (:state @cb-state) 'IDLE)]
								     (dosync (ref-set cb-state {:state 'INTERPOLATING-COLOR
												:start-time (if idle?
													      (System/currentTimeMillis)
													      (- (System/currentTimeMillis)
														 (- (+ (:start-time @cb-state)
														       (:run-time @cb-state))
														    (System/currentTimeMillis))))
												:target-fps 8
												:run-time 5000
												:start-colour (get-system-colour SWT/COLOR_BLACK)
												:end-colour (get-system-colour SWT/COLOR_WHITE)})))
								  (.redraw cb))))
			  cb))
	)
    (.setBlockOnOpen true)
    (.open))
  (.. Display getCurrent dispose))