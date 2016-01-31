# Spark-Text

A tour of some of how to use Spark's text mining abilities to mine Japanese text.

(Japanese Text sold separately.)

## Data format

The Word2Vec main class expects a plain text file with one sentence per line.
 
The LDA main class expects a plain text file that has one document per line.

## Running

sbt assembly

spark-submit --master local --class com.yumusoft.spark.Main target/scala-2.10/spark-text-1.0.0-SNAPSHOT.jar
spark-submit --master local --class com.yumusoft.kuromoji.Main target/scala-2.10/spark-text-1.0.0-SNAPSHORT.jar
spark-submit --master local --class com.yumusoft.word2vec.Main sentences.txt target/scala-2.10/spark-text-1.0.0-SNAPSHOT.jar
spark-submit --master local --class com.yumusoft.lda.Main.Main textfile.txt target/scala-2.10/spark-text-1.0.0-SNAPSHOT.jar

## Disclaimer

This is sample code for my Scala Matsuri presentation.  It doesn't cache properly and might have bugs.

Programmer discretion adviced.

Pull-requests welcome.

## License

Feel free to use this code under the terms of the Apache License.
