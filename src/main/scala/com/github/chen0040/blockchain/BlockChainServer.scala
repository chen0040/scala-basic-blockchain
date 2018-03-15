package com.github.chen0040.blockchain

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import com.github.chen0040.blockchain.utils.HttpClient
import com.github.chen0040.blockchain.utils.IpTools
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spark.{Request, Response, Spark}
import java.util

import spark.Spark.port
import spark.Spark.get
import spark.Spark.post

class BlockChainServer {

}

object BlockChainServer {
  private val logger: Logger = LoggerFactory.getLogger(classOf[BlockChainServer])
  private var chain: BlockChain = null
  private val executor: ListeningExecutorService = MoreExecutors.listeningDecorator(util.concurrent.Executors.newFixedThreadPool(10))

  def main(args: Array[String]): Unit = {
    for (arg <- args) {
      logger.info("arg: {}", arg)
    }
    var seed = "http://localhost:3088"
    if (args.length >= 1) seed = args(0)
    logger.info("block chain seed node: {}", seed)
    val chainPort = IpTools.getAvailablePort(3088)
    chain = new BlockChain(chainPort)
    port(chainPort)
    logger.info("Starting block chain node at {}", chain.id)
    val seedIp = seed
    executor.submit(new Runnable {
      override def run(): Unit = {
        try Thread.sleep(1000L)
        catch {
          case e: InterruptedException =>
            e.printStackTrace()
        }
        logger.info("broad this node {} ...", chain.id)
        chain.broadCast(seedIp)
      }
    })

    get("/kill", (req: Request, res: Response) => {
      new Thread(() => {

        try Thread.sleep(100)
        catch {
          case e: InterruptedException =>
            e.printStackTrace()
        }
        System.exit(0)
      }).start()
      "block-chain node will be killed in 100 milliseconds"
    })
    get("/ping", (req: Request, res: Response) => "block-chain-node")
    get("/chain", (req: Request, res: Response) => {
      res.header("Content-Type", "application/json")
      res.status(200)
      JSON.toJSONString(chain.chain, SerializerFeature.BrowserCompatible)
    })
    post("/nodes/register", (req: Request, res: Response) => {
      logger.info("nodes registration invoked at {}", chain.id)
      val nodes = JSON.parseArray(req.body, classOf[String])
      val total_nodes: java.lang.Integer = chain.register(nodes)
      res.status(201)
      val result = new util.HashMap[String, java.lang.Object]
      result.put("message", "New nodes have been added")
      result.put("total_nodes", total_nodes)
      res.header("Content-Type", "application/json")
      JSON.toJSONString(result, SerializerFeature.BrowserCompatible)
    })
    post("/nodes/de-register", (req: Request, res: Response) => {
      logger.info("nodes de-registration invoked at {}", chain.id)
      val nodes = JSON.parseArray(req.body, classOf[String])
      val total_nodes: java.lang.Integer = chain.deRegister(nodes)
      res.status(201)
      val result = new util.HashMap[String, java.lang.Object]
      result.put("message", "Ndes have been removed")
      result.put("total_nodes", total_nodes)
      res.header("Content-Type", "application/json")
      JSON.toJSONString(result, SerializerFeature.BrowserCompatible)
    })
    post("/nodes/broadcast_ip", (req: Request, res: Response) => {
      logger.info("broadcast api invoked at {}", chain.id)
      val nodes = JSON.parseArray(req.body, classOf[String])
      val total_nodes: java.lang.Integer = chain.register(nodes)
      executor.submit(new Runnable {
        override def run(): Unit =  {
          import scala.collection.JavaConversions._
          for (node <- chain.nodes) {
            HttpClient.postArray(node + "/nodes/register", nodes)
          }
        }
      })

      val result = new util.HashMap[String, java.lang.Object]
      result.put("message", "New nodes have been added and broadcasted")
      result.put("total_nodes", total_nodes)
      res.status(201)
      res.header("Content-Type", "application/json")
      JSON.toJSONString(result, SerializerFeature.BrowserCompatible)
    })
    post("/nodes/broadcast_de_registration", (req: Request, res: Response) => {
      logger.info("broadcast de-registration invoked at {}", chain.id)
      val nodes = JSON.parseArray(req.body, classOf[String])
      val total_nodes: java.lang.Integer = chain.deRegister(nodes)
      executor.submit(new Runnable {
        override def run(): Unit = {
          import scala.collection.JavaConversions._
          for (node <- chain.nodes) {
            HttpClient.postArray(node + "/nodes/de-register", nodes)
          }
        }
      })
      val result = new util.HashMap[String, java.lang.Object]
      result.put("message", "Nodes have been removed and de-registration action broadcasted")
      result.put("total_nodes", total_nodes)
      res.status(201)
      res.header("Content-Type", "application/json")
      JSON.toJSONString(result, SerializerFeature.BrowserCompatible)
    })
    get("/nodes", (req: Request, res: Response) => {
      res.header("Content-Type", "application/json")
      JSON.toJSONString(chain.nodes, SerializerFeature.BrowserCompatible)
    })
    get("/nodes/resolve", (req: Request, res: Response) => {
      val replaced = chain.resolveConflicts
      val result = new util.HashMap[String, AnyRef]
      if (replaced) {
        result.put("message", "Our chain was replaced")
        result.put("new_chain", chain.chain)
      }
      else {
        result.put("message", "Our chain is authoritative")
        result.put("chain", chain.chain)
      }
      res.status(200)
      res.header("Content-Type", "application/json")
      JSON.toJSONString(result, SerializerFeature.BrowserCompatible)
    })
    get("/mine", (req: Request, res: Response) => {
      res.status(200)
      res.header("Content-Type", "application/json")
      JSON.toJSONString(chain.mine, SerializerFeature.BrowserCompatible)
    })
    post("/transactions/new", (req: Request, res: Response) => {
      try {
        val newTransaction = JSON.parseObject(req.body, classOf[Transaction])
        val index = chain.newTransaction(newTransaction.sender, newTransaction.recipient, newTransaction.amount)
        val result = new util.HashMap[String, String]
        result.put("message", "Transaction will be added to block " + index)
        res.status(201)
        res.header("Content-Type", "application/json")
        JSON.toJSONString(result, SerializerFeature.BrowserCompatible)
      } catch {
        case ex: Exception =>
          res.status(400)
          val result = new util.HashMap[String, String]
          result.put("message", ex.toString)
          res.header("Content-Type", "application/json")
          JSON.toJSONString(result, SerializerFeature.BrowserCompatible)
      }
    })
    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      logger.info("node shutting down ...")
      chain.deRegisterSelf(seedIp)
      Spark.stop()
    }))
  }
}