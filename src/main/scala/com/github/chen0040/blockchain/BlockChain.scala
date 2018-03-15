package com.github.chen0040.blockchain

import com.alibaba.fastjson.JSON
import com.github.chen0040.blockchain.utils.HttpClient
import com.github.chen0040.blockchain.utils.IpTools
import com.google.common.hash.Hashing
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.util

import com.alibaba.fastjson.serializer.SerializerFeature

object BlockChain {
  private val logger: Logger = LoggerFactory.getLogger(classOf[BlockChain])
}

class BlockChain(val port: Int) {
  val id: String = "http://" + IpTools.getIpAddress + ":" + port
  var chain: util.List[Block] = new util.ArrayList[Block]
  var currentTransactions: util.List[Transaction] = new util.ArrayList[Transaction]
  val nodes = new util.HashSet[String]

  def newBlock(proof: Long): Block = newBlock(proof, null)

  def newBlock(proof: Long, prevHash: String): Block = {
    var prev_hash: String = prevHash
    if (prev_hash == null) prev_hash = hash(chain.get(chain.size - 1))
    val block = new Block
    block.index = chain.size + 1
    block.timestamp = new util.Date().getTime
    block.transactions = currentTransactions
    block.prevHash = prev_hash
    block.proof = proof
    currentTransactions = new util.ArrayList[Transaction]
    chain.add(block)
    block
  }

  def newTransaction(sender: String, recipient: String, amount: Double): Long = {
    val transaction = new Transaction
    transaction.amount = amount
    transaction.sender = sender
    transaction.recipient = recipient
    lastBlock.index + 1 // return the index of the block that will hold this transaction
  }

  private def lastBlock: Block = {
    if (chain.isEmpty) return new Block
    chain.get(chain.size - 1)
  }

  private def hash(block: Block): String = {
    val json = JSON.toJSONString(block, SerializerFeature.BrowserCompatible)
    hash(json)
  }

  private def hash(text: String): String = Hashing.sha256.hashString(text, StandardCharsets.UTF_8).toString

  def validateProof(lastProof: java.lang.Long, proof: java.lang.Long): Boolean = {
    val text = lastProof + "" + proof
    val hashed = hash(text)
    hashed.startsWith("0000")
  }

  def proofOfWork(lastProof: java.lang.Long): java.lang.Long = {
    var proof = 0L
    while ( {
      !validateProof(lastProof, proof)
    }) proof += 1
    proof
  }

  def registerNode(url: String): Unit = {
    if (url == id) return
    nodes.add(url)
  }

  def validateChain(chain: util.List[Block]): Boolean = {
    var lastBlock = chain.get(0)
    var currentIndex = 1
    while ( {
      currentIndex < chain.size
    }) {
      val block = chain.get(currentIndex)
      if (!block.prevHash.equals(hash(lastBlock))) return false
      lastBlock = block
      currentIndex += 1
    }
    true
  }

  def resolveConflicts: Boolean = {
    val neighbors = new util.ArrayList[String](nodes)
    var newChain: util.List[Block] = null
    // only looking for chains longer than that available at this node.
    var max_length = chain.size
    import scala.collection.JavaConversions._
    for (neighbor <- neighbors) {
      val chain = queryChain(neighbor)
      if (chain != null && chain.size > max_length && validateChain(chain)) {
        max_length = chain.size
        newChain = chain
      }
    }
    if (newChain != null) {
      chain = newChain
      return true
    }
    false
  }

  private def queryChain(url: String) = HttpClient.getArray(url + "/chain", classOf[Block])

  def mine: MineResult = {
    val lastBlock: Block = this.lastBlock
    val lastProof = lastBlock.proof
    val proof = proofOfWork(lastProof)
    newTransaction("0", id, 1)
    val prevHash = hash(lastBlock)
    val newBlock: Block = this.newBlock(proof, prevHash)
    val result = new MineResult
    result.index = newBlock.index
    result.message = "New Block Forged"
    result.prevHash = prevHash
    result.proof = proof
    result.transactions = newBlock.transactions
    result
  }

  def register(nodes: util.List[String]): Int = {
    import scala.collection.JavaConversions._
    for (node <- nodes) {
      registerNode(node)
    }
    this.nodes.size
  }

  def broadCast(seedIp: String): Unit = {
    if (id == seedIp) return
    val a = new util.ArrayList[String]
    a.add(id)
    val url = seedIp + "/nodes/broadcast_ip"
    BlockChain.logger.info("broad cast this ip " + seedIp + " to " + url)
    HttpClient.postArray(url, a)
  }

  def deRegisterSelf(seedIp: String): Unit = {
    if (id == seedIp) return
    val a = new util.ArrayList[String]
    a.add(id)
    val url = seedIp + "/nodes/broadcast_de_registration"
    BlockChain.logger.info("broad cast the de-registration of this ip " + seedIp + " to " + url)
    HttpClient.postArray(url, a)
  }

  def deRegister(nodes: util.List[String]): Int = {
    import scala.collection.JavaConversions._
    for (node <- nodes) {
      this.nodes.remove(node)
    }
    this.nodes.size
  }
}