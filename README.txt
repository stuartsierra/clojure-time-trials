clojure-time-trials

Clojure Time Trials

by Stuart Sierra, http://stuartsierra.com/

This repository contains tests to evaluate the effectiveness of
different optimization techniques on different versions of Clojure.

HOW TO USE

Clone this repository, then copy the JAR files for different versions
of Clojure into this directory. For example, I use
"clojure-1.0.0.jar", "clojure-1.1.0.jar", and
"clojure-1.2.0-master-SNAPSHOT.jar".

The shell script "run.sh" will execute the same script
(time-trials.clj) once for each Clojure JAR in the directory, printing
the comparisons to standard output.
