package com.github.chen0040.blockchain

import java.util

class Block(var index: Long, var timestamp: Long, var proof: Long, var transactions: util.List[Transaction], var prevHash: String) {
  def this() =
    this(index=0L, timestamp=0L, proof=0L, transactions=new util.ArrayList[Transaction](), prevHash="")

}