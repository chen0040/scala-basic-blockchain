package com.github.chen0040.blockchain.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net._
import java.util
import java.util.Random

/**
  * Created by xschen on 1/27/16.
  */

class IpTools {

}

object IpTools {
  private val logger = LoggerFactory.getLogger(classOf[IpTools])

  def getIpAddress: String = {
    var result: String = null
    var interfaces: util.Enumeration[NetworkInterface] = null
    try interfaces = NetworkInterface.getNetworkInterfaces
    catch {
      case e: SocketException =>

      // handle error
    }
    if (interfaces != null) while ( {
      interfaces.hasMoreElements && (result == null || result == "")
    }) {
      val i = interfaces.nextElement
      val addresses = i.getInetAddresses
      while ( {
        addresses.hasMoreElements && (result == null || result.isEmpty)
      }) {
        val address = addresses.nextElement
        if (!address.isLoopbackAddress && address.isSiteLocalAddress) result = address.getHostAddress
      }
    }
    result
  }

  def findAvailablePort: Int = {
    var socket: ServerSocket = null
    try {
      socket = new ServerSocket(0)
      socket.setReuseAddress(true)
      val port = socket.getLocalPort
      logger.info("{} is available", port)
      try socket.close()
      catch {
        case e: IOException =>
          logger.error("Check port exception occurred: ", e)
      }
      return port
    } catch {
      case e: IOException =>
    } finally if (socket != null) try socket.close()
    catch {
      case e: IOException =>
        logger.error("Check port exception occurred on close ", e)
    }
    -1
  }

  def isPortAvailable(port: Int): Boolean = {
    var ss: ServerSocket = null
    var ds: DatagramSocket = null
    try {
      ss = new ServerSocket
      val endPoint = new InetSocketAddress(getIpAddress, port)
      ss.bind(endPoint)
      ss.setReuseAddress(true)
      ds = new DatagramSocket(port)
      ds.setReuseAddress(true)
      logger.info("port available: {}", port)
      return true
    } catch {
      case e: IOException =>
        logger.error("Failed to bind port: " + port, e)
    } finally {
      if (ds != null) ds.close()
      if (ss != null) try ss.close()
      catch {
        case e: IOException =>

        /* should not be thrown */
      }
    }
    false
  }

  def main(args: Array[String]): Unit = logger.info(getIpAddress)

  def getNextAvailablePort(port: Int): Int = {
    val nextPort = port + 1
    if (isPortAvailable(nextPort)) nextPort
    else getNextAvailablePort(nextPort)
  }

  def getAvailablePort(port: Int): Int = if (isPortAvailable(port)) port
  else getNextAvailablePort(port)

  def getNextAvailablePortWithRandomDelay(port: Int, random: Random): Int = {
    val nextPort = port + 1
    if (isPortAvailable(nextPort)) nextPort
    else {
      try Thread.sleep(Math.abs(random.nextLong % 10000L))
      catch {
        case e: InterruptedException =>
          logger.error("sleep interrupted")
      }
      getNextAvailablePortWithRandomDelay(nextPort, random)
    }
  }
}