#!/bin/sh

java -server -version

for file in *.jar
do
  java -server -cp $file clojure.main time-trials.clj
done


