package com.github.chen0040.blockchain

class Transaction(var sender: String, var recipient: String, var amount: Double) {
  def this() = this(sender=null, recipient=null, amount=0L)
}