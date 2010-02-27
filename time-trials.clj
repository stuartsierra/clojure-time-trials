(defmacro nanotime [reps body]
  `(let [start# (System/nanoTime)]
     (dotimes [i# ~reps] ~body)
     (/ (- (System/nanoTime) start#) 1000000.0)))

(defmacro time-trials [sets reps body]
  `(for [i# (range ~sets)] (nanotime ~reps ~body)))

(defn clojure-version-string []
  (str (:major *clojure-version*) \.
       (:minor *clojure-version*) \.
       (:incremental *clojure-version*)
       (when-let [q (:qualifier *clojure-version*)]
         (when-not (= q "") (str \- q)))))

(defmacro compare-times
  "Measures time for n repetitions of each body, averaged over k runs
  (omits first 2 runs from average).  Prints description, followed by
  averages from body1 and body2."
  [description k n body1 body2]
  `(let [times1# (drop 2 (time-trials ~k ~n ~body1))
         times2# (drop 2 (time-trials ~k ~n ~body2))]
     (println (format "%30s %9.2f %9.2f" ~description
                      (/ (apply + times1#) ~(- k 2))
                      (/ (apply + times2#) ~(- k 2))))))

(println (format "%-30s %9s %9s" (str "# Clojure " (clojure-version-string))
                 "BEFORE" "AFTER"))

(let [s (identity "Hello, World!")]
  (compare-times "Eliminate Reflection" 5 10000
                 (.length s)
                 (.length #^String s)))

(compare-times "Use primitives in loops"
               5 100000
               (loop [sum 0, x 1]
                 (if (= x 100)
                   sum
                   (recur (+ sum x) (inc x))))
               (loop [sum (int 0), x (int 1)]
                 (if (= x 100)
                   sum
                   (recur (unchecked-add sum x) (unchecked-inc x)))))
               

(compare-times "Replace = with =="
               5 1000000
               (= 2 4) (== 2 4))

(compare-times "Use Binary Arithmetic"
               5 1000000
               (+ 2 4 6 8) (+ 2 (+ 4 (+ 6 8))))

(let [v [1 2 3]]
  (compare-times "Avoid Destructuring"
                 5 1000000
                 (let [[x y z] v] (+ x y z))
                 (let [x (nth v 0)
                       y (nth v 1)
                       z (nth v 2)] (+ x y z))))

(def *value* 42)

(let [value *value*]
  (compare-times "Avoid Var lookups"
                 5 1000000
                 (* *value* *value*)
                 (* value value)))
