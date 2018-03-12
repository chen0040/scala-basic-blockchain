package com.github.chen0040.blockchain

import java.util;

class MineResult(var message: String, var index: Long, var transactions: util.List[Transaction], var proof: Long, var prevHash: String) {
  def this() = this(message=null, index=0L, transactions=new util.ArrayList[Transaction](), proof=0L, prevHash=null)
}