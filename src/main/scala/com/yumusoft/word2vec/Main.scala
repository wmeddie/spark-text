package com.yumusoft.word2vec

import breeze.linalg.DenseVector
import org.apache.spark.mllib.feature.{ Word2Vec, Word2VecModel }
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.{ SparkConf, SparkContext }
import org.atilika.kuromoji.Tokenizer

import scala.util.Try

object Main extends App {
  import scala.collection.JavaConverters.asScalaBufferConverter

  val vectorLength = 300

  val sc = new SparkContext(new SparkConf())
  val sqlContext = new SQLContext(sc)

  val rawText: RDD[String] = sc.textFile(args(0))
  val text = rawText.filter(line => line.indexOf(',') == -1)

  lazy val tokenizer = Tokenizer.builder().build()

  val documentWords: RDD[Seq[String]] =
    text.map(line => tokenizer.tokenize(line).asScala.map(_.getSurfaceForm).toSeq)

  documentWords.cache()

  val model = new Word2Vec().setVectorSize(vectorLength).fit(documentWords)

  println("==========")
  println("日本 findSynonyms")
  model.findSynonyms("日本", 10).foreach(println)

  val embeds = Map(
    "ITEM_001_01" -> "営業部門の情報共有と活用をサポートし",
    "ITEM_001_02" -> "組織的な営業力･売れる仕組みを構築します",
    "ITEM_001_03" -> "営業情報のコミュニケーション基盤を構築する",
    "ITEM_002_01" -> "一般的なサーバ、ネットワーク機器やOSレベルの監視に加え",
    "ITEM_002_02" -> "またモニタリングポータルでは、アラームの発生状況",
    "ITEM_002_03" -> "監視システムにより取得されたパフォーマンス情報が逐次ダッシュボード形式",
    "ITEM_003_01" -> "IPネットワークインフラストラクチャを構築します",
    "ITEM_003_02" -> "導入にとどまらず、アプリケーションやOAシステムとの融合を図ったユニファイドコミュニケーション環境を構築",
    "ITEM_003_03" -> "企業内および企業外へのコンテンツの効果的な配信環境、閲覧環境をご提供します"
  )

  def stringToVector(s: String): Array[Double] = {
    val words = tokenizer.tokenize(s).asScala.map(_.getSurfaceForm).toSeq
    val vectors = words.map(word => Try(model.transform(word)).getOrElse(model.transform("は")))
    val breezeVectors: Seq[DenseVector[Double]] = vectors.map(v => new DenseVector(v.toArray))
    val concat = breezeVectors.foldLeft(DenseVector.zeros[Double](vectorLength))((a, b) => a :+ b)

    concat.toArray
  }

  val embedVectors: Map[String, Array[Float]] = embeds.map {
    case (key, value) => (key, stringToVector(value).map(_.toFloat))
  }

  val embedModel = new Word2VecModel(embedVectors ++ model.getVectors)

  println("==========")
  println("ITEM_001_01 findSynonyms")
  embedModel.findSynonyms("ITEM_001_01", 10).foreach(println)

  val newSentence = stringToVector("会計・受発注及び生産管理を中心としたシステム")

  println("==========")
  println("newSentence findSynonyms")
  embedModel.findSynonyms(Vectors.dense(newSentence), 10).foreach(println)

  println("==========")
  println("日本 findSynonyms")
  embedModel.findSynonyms("日本", 10).foreach(println)
}
