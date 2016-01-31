package com.yumusoft.kuromoji

import org.atilika.kuromoji.Tokenizer

object Main extends App {
  import scala.collection.JavaConverters.asScalaBufferConverter

  lazy val tokenizer = Tokenizer.builder().build()

  val ex1 = "リストのような構造の物から条件を満たす物を探す"
  val res1 = tokenizer.tokenize(ex1).asScala

  val ex2 = "厚生年金基金脱退に伴う手続きについてのリマインドです"
  val res2 = tokenizer.tokenize(ex2).asScala

  for (token <- res1.union(res2)) {
    println(s"${token.getBaseForm}\t${token.getPartOfSpeech}")
  }
}
