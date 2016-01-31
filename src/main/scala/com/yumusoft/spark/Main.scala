package com.yumusoft.spark

import org.apache.spark.{ SparkConf, SparkContext }

object Main extends App {
  val sc = new SparkContext(new SparkConf())

  val text = sc.textFile(args(0))

  val counts = text.flatMap(line => line.split(" "))
    .map(word => (word, 1))
    .reduceByKey(_ + _)

  counts.collect().foreach(println)
}
