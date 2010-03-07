(defmacro timetest
  "Executes reps repetitions of body, five times, in the
  clojure.core/time macro."
  [reps body]
  `(dotimes [j# 5] (time (dotimes [i# ~reps] ~body))))

(defn clojure-version-string []
  (str (:major *clojure-version*) \.
       (:minor *clojure-version*) \.
       (:incremental *clojure-version*)
       (when-let [q (:qualifier *clojure-version*)]
         (when-not (empty? q) (str \- q)))))

(defmacro compare-times
  "Executes each body n times, prints description followed by elapsed
  times."
  [description n body1 body2]
  `(do (println "\n###" (clojure-version-string)
                "BEFORE" ~description)
       (timetest ~n ~body1)
       (println "###" (clojure-version-string)
                "AFTER" ~description)
       (timetest ~n ~body2)))

(defmacro version-gte [major minor & body]
  (if (or (> (:major *clojure-version*) major)
          (and (= major (:major *clojure-version*))
               (> (:minor *clojure-version*) minor)))
    `(do ~@body)
    nil))

(println "\n\n##### Clojure" (clojure-version-string))

(let [s (identity "Hello, World!")]
  (compare-times "Eliminating reflection" 10000
                 (.length s)
                 (.length #^String s)))

(version-gte 1 1
  (compare-times "Using transient vectors" 1000
                 (loop [r [], i 0]
                   (if (= i 1000)
                     r
                     (recur (conj r i) (inc i))))
                 (loop [r (transient []), i 0]
                   (if (= i 1000)
                     (persistent! r)
                     (recur (conj! r i) (inc i))))))

(version-gte 1 1
  (compare-times "Using transient maps" 1000
                 (loop [r {}, i 0]
                   (if (= i 100)
                     r
                     (recur (assoc r i i) (inc i))))
                 (loop [r (transient {}), i 0]
                   (if (= i 100)
                     (persistent! r)
                     (recur (assoc! r i i) (inc i))))))

(compare-times "Using loop primitives" 100000
               (loop [sum 0, x 1]
                 (if (= x (int 100))
                   sum
                   (recur (+ sum x) (inc x))))
               (loop [sum (int 0), x (int 1)]
                 (if (= x (int 100))
                   sum
                   (recur (+ sum x) (inc x)))))

(compare-times "Using == in primitive loops" 100000
               (loop [sum (int 0), x (int 1)]
                 (if (= x (int 100))
                   sum
                   (recur (+ sum x) (inc x))))
               (loop [sum (int 0), x (int 1)]
                 (if (== x (int 100))
                   sum
                   (recur (+ sum x) (inc x)))))

(compare-times "Using unchecked ops in primitive loops" 100000
               (loop [sum (int 0), x (int 1)]
                 (if (== x (int 100))
                   sum
                   (recur (+ sum x) (inc x))))
               (loop [sum (int 0), x (int 1)]
                 (if (== x (int 100))
                   sum
                   (recur (unchecked-add sum x) (unchecked-inc x)))))

(compare-times "Using binary arithmetic ops" 1000000
               (+ 2 4 6 8) (+ 2 (+ 4 (+ 6 8))))

(let [ary (int-array (range 100))]
  (compare-times "Using amap" 10000
                 (dorun (map inc ary))
                 (dorun (amap ary i ret (inc (aget ary i))))))

(let [ary (int-array (range 100))]
  (compare-times "Using areduce" 10000
                 (reduce + ary)
                 (areduce ary i ret 0 (+ ret (aget ary i)))))

(let [v [1 2 3]]
  (compare-times "Avoiding destructuring" 1000000
                 (let [[x y z] v] (+ x y z))
                 (let [x (nth v 0)
                       y (nth v 1)
                       z (nth v 2)] (+ x y z))))

(def *value* 42)

(let [value *value*]
  (compare-times "Avoiding Var lookups" 1000000
                 (* *value* *value*)
                 (* value value)))
