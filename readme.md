# Spark-Text

A tour of how to use Spark's text mining abilities to mine Japanese text.

(Japanese Text sold separately.)

## Data format

The Word2Vec main class expects a plain text file with one sentence per line.
 
The LDA main class expects a plain text file that has one document per line.

## Running

Run `sbt assembly`.

Download Spark version 1.5.2 (or later) from [http://spark.apache.org/downloads.html](http://spark.apache.org/downloads.html).

Finally you should be able to run the following (Assuming spark/bin is in your PATH):

    spark-submit --master local --class com.yumusoft.spark.Main target/scala-2.10/spark-text-1.0.0-SNAPSHOT.jar
    spark-submit --master local --class com.yumusoft.kuromoji.Main target/scala-2.10/spark-text-1.0.0-SNAPSHORT.jar text.txt
    spark-submit --master local --class com.yumusoft.word2vec.Main target/scala-2.10/spark-text-1.0.0-SNAPSHOT.jar sentences.txt
    spark-submit --master local --class com.yumusoft.lda.Main.Main target/scala-2.10/spark-text-1.0.0-SNAPSHOT.jar textfile.txt 

## Disclaimer

This is sample code for my Scala Matsuri presentation.  It might not cache properly and might have bugs.

Programmer discretion is advised.

Pull-requests welcome.

## License

Feel free to use this code under the terms of the Apache License.
