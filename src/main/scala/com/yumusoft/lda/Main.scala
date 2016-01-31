package com.yumusoft.lda

import org.apache.spark.mllib.clustering.{ DistributedLDAModel, LocalLDAModel, LDA }
import org.apache.spark.mllib.linalg.{ Vector, Vectors }
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.{ SparkConf, SparkContext }
import org.atilika.kuromoji.Tokenizer

import scala.collection.Map

object Main extends App {
  import scala.collection.JavaConverters.asScalaBufferConverter

  val sc = new SparkContext(new SparkConf())
  val sqlContext = new SQLContext(sc)

  val rawText: RDD[String] = sc.textFile(args(0))
  val text = rawText.filter(line => line.indexOf(',') == -1)

  text.take(100).foreach(println)
  println(s"==========")
  println("Text sample")

  Thread.sleep(10 * 1000)

  lazy val tokenizer = Tokenizer.builder().build()

  val words: RDD[String] = for {
    line <- text
    token <- tokenizer.tokenize(line).asScala if token.getBaseForm != null
  } yield token.getBaseForm

  words.cache()

  val wordCounts = words.map(w => (w, 1)).reduceByKey(_ + _).sortBy(-_._2)
  val popular = wordCounts.take(50).map(_._1).toSet
  val unpopular = wordCounts.filter { case (word, count) => count <= 2 }.map(_._1).collect().toSet

  val stopwords = popular.union(unpopular)

  stopwords.foreach(println)
  println(s"==========")
  println("stopwords")
  Thread.sleep(10 * 1000)

  val vocabIndicies = words.distinct().filter(w => !stopwords.contains(w)).zipWithIndex()
  val vocab: Map[String, Long] = vocabIndicies.collectAsMap()
  val vocabulary = vocabIndicies.collect().map(_._1)

  println(s"Vocabulary size: ${vocabulary.length}")

  val documentWords: RDD[Array[String]] =
    text.map(line => tokenizer.tokenize(line).asScala.filter(t => t.getBaseForm != null).map { t =>
      t.getBaseForm
    }.filter(w => !stopwords.contains(w)).toArray).filter(_.length > 0)

  documentWords.cache()

  println("document words")
  documentWords.take(10).foreach(a => {
    println(a.mkString(", "))
  })
  println(s"==========")

  Thread.sleep(10 * 1000)

  val documentCounts: RDD[Array[(String, Int)]] =
    documentWords.map(words => words.distinct.map(word => (word, words.count(_ == word))))

  val documentIndexAndCount: RDD[Seq[(Int, Double)]] =
    documentCounts.map(wordsAndCount => wordsAndCount.map { case (word, count) => (vocab(word).toInt, count.toDouble) })

  val corpus: RDD[(Long, Vector)] =
    documentIndexAndCount.map(Vectors.sparse(vocab.size, _)).zipWithIndex.map(_.swap)

  corpus.cache()

  // Cluster the documents into three topics using LDA
  val ldaModel = new LDA().setK(10).setMaxIterations(100).run(corpus)

  val topics = ldaModel.describeTopics(10).map {
    case (terms, weights) =>
      terms.map(vocabulary(_)).zip(weights)
  }

  topics.zipWithIndex.foreach {
    case (topic, i) =>
      println(s"TOPIC $i")
      topic.foreach { case (term, weight) => println(s"$term\t$weight") }
      println(s"==========")
  }

  val lda: LocalLDAModel = if (ldaModel.isInstanceOf[DistributedLDAModel]) {
    ldaModel.asInstanceOf[DistributedLDAModel].toLocal
  } else {
    ldaModel.asInstanceOf[LocalLDAModel]
  }

  val newDoc1 = sc.parallelize(Seq("添付の書類にご記入の上、ご提出ください。"))
  val newDoc2 = sc.parallelize(Seq("平素は当社サービスをご利用いただき、誠にありがとうございます。"))

  def stringToCountVector(strings: RDD[String]) = {
    val newDocumentWords: RDD[Array[String]] =
      strings.map(line => tokenizer.tokenize(line).asScala.filter(t => t.getBaseForm != null).map { t =>
        t.getBaseForm
      }.filter(w => !stopwords.contains(w)).toArray).filter(_.length > 0)

    val newDocumentCounts: RDD[Array[(String, Int)]] =
      newDocumentWords.map(words => words.distinct.map(word => (word, words.count(_ == word))))

    val newDocumentIndexAndCount: RDD[Seq[(Int, Double)]] =
      newDocumentCounts.map(wordsAndCount => wordsAndCount.map {
        case (word, count) => (vocab(word).toInt, count.toDouble)
      })

    val newCorpus: RDD[(Long, Vector)] =
      newDocumentIndexAndCount.map(Vectors.sparse(vocab.size, _)).zipWithIndex.map(_.swap)

    newCorpus
  }

  println(s"==========")
  val newTopics = lda.topicDistributions(stringToCountVector(newDoc1).union(stringToCountVector(newDoc2)))
  newTopics.foreach {
    case (docId, topicVector) =>
      println(s"newDocument $docId topics distribution: ${topicVector.toArray.mkString(",")}")
  }

  println(s"==========")

  val score1 = lda.logLikelihood(stringToCountVector(newDoc1))
  println(s"Log Likelihood newDoc1: $score1")

  val score2 = lda.logLikelihood(stringToCountVector(newDoc2))
  println(s"Log likelihood newDoc2: $score2")
}
