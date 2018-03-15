package com.github.chen0040.blockchain.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import org.apache.http.NameValuePair
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods._
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.Serializable
import java.util

/**
  * Created by xschen on 3/10/16.
  */
@SerialVersionUID(4661645115933875389L)
object HttpClient {
  private val DATA_ENCODING = "UTF-8"
  private val logger = LoggerFactory.getLogger(classOf[HttpClient])

  private def buildClient = { //HttpClientBuilder builder = HttpClientBuilder.create();
    val timeout = 60
    val config = RequestConfig.custom.setSocketTimeout(timeout * 1000).setConnectionRequestTimeout(timeout * 1000).setConnectTimeout(timeout * 1000).build
    HttpClients.custom.setDefaultRequestConfig(config).build //builder.build();
  }

  @throws[IOException]
  def formPost(uri: String, parameters: util.Map[String, String]): Unit = {
    logger.info("form post to {}", uri)
    val httpPost = new HttpPost(uri)
    val client = buildClient
    val params = new util.ArrayList[NameValuePair]
    import scala.collection.JavaConversions._
    for (entry <- parameters.entrySet) {
      params.add(new BasicNameValuePair(entry.getKey, entry.getValue))
    }
    httpPost.setEntity(new UrlEncodedFormEntity(params))
    val response = client.execute(httpPost)
    val statusCode = response.getStatusLine.getStatusCode
    logger.info("status code: {}", statusCode)
  }

  def jsonPost(url: String, parameters: util.Map[String, String]): String = {
    val httpClient = buildClient
    var json = ""
    val body = JSON.toJSONString(parameters, SerializerFeature.BrowserCompatible)
    try {
      val request = new HttpPost(url)
      val params = new StringEntity(body)
      request.addHeader("content-type", "application/json")
      request.setEntity(params)
      val result = httpClient.execute(request)
      if (result.getEntity != null) json = EntityUtils.toString(result.getEntity, DATA_ENCODING)
      result.close()
      httpClient.close()
    } catch {
      case ex: IOException =>
        json = ex.getMessage
    }
    json
  }

  def postArray[T](url: String, a: util.List[T]): String = {
    val json = JSON.toJSONString(a, SerializerFeature.BrowserCompatible)
    val headers = new util.HashMap[String, String]
    headers.put("Content-Type", "application/json")
    post(url, json, headers)
  }

  def post(url: String, body: String, headers: util.Map[String, String]): String = {
    val httpClient = buildClient
    var json = ""
    try {
      val request = new HttpPost(url)
      val params = new StringEntity(body)
      import scala.collection.JavaConversions._
      for (entry <- headers.entrySet) {
        request.addHeader(entry.getKey, entry.getValue)
      }
      request.setEntity(params)
      val result = httpClient.execute(request)
      if (result.getEntity != null) json = EntityUtils.toString(result.getEntity, DATA_ENCODING)
      result.close()
      httpClient.close()
    } catch {
      case ex: IOException =>
        json = ex.getMessage
    }
    json
  }

  def put(url: String, body: String, headers: util.Map[String, String]): String = {
    val httpClient = buildClient
    var json = ""
    try {
      val request = new HttpPut(url)
      val params = new StringEntity(body)
      import scala.collection.JavaConversions._
      for (entry <- headers.entrySet) {
        request.addHeader(entry.getKey, entry.getValue)
      }
      request.setEntity(params)
      val result = httpClient.execute(request)
      if (result.getEntity != null) json = EntityUtils.toString(result.getEntity, DATA_ENCODING)
      result.close()
      httpClient.close()
    } catch {
      case ex: IOException =>
        json = ex.getMessage
    }
    json
  }

  def delete(url: String, headers: util.Map[String, String]): String = {
    val httpClient = buildClient
    var json = ""
    try {
      val request = new HttpDelete(url)
      import scala.collection.JavaConversions._
      for (entry <- headers.entrySet) {
        request.addHeader(entry.getKey, entry.getValue)
      }
      val response = httpClient.execute(request)
      if (response.getEntity != null) json = EntityUtils.toString(response.getEntity, DATA_ENCODING)
      response.close()
      httpClient.close()
    } catch {
      case ex: IOException =>
        json = ex.getMessage
    }
    json
  }

  def delete(url: String): String = {
    val httpClient = buildClient
    var json = ""
    try {
      val request = new HttpDelete(url)
      request.addHeader("content-type", "application/json")
      val response = httpClient.execute(request)
      if (response.getEntity != null) json = EntityUtils.toString(response.getEntity, DATA_ENCODING)
      response.close()
      httpClient.close()
    } catch {
      case ex: IOException =>
        json = ex.getMessage
    }
    json
  }

  def getJson(url: String): String = {
    var json = ""
    try {
      val httpClient = buildClient
      val request = new HttpGet(url)
      request.addHeader("content-type", "application/json")
      val response = httpClient.execute(request)
      if (response.getEntity != null) json = EntityUtils.toString(response.getEntity, DATA_ENCODING)
      //logger.info("spark[tryReadAlgorithmModuleStatus]: "+json);
    } catch {
      case ex2: Exception =>
        json = ex2.getMessage
    }
    json
  }

  def getObject[T >: Null](url: String, clazz: Class[T]): T = {
    val json = getJson(url)
    try JSON.parseObject(json, clazz)
    catch {
      case ex: Exception =>
        logger.error("Failed to parse json " + json, ex)
        null
    }
  }

  def getArray[T](url: String, clazz: Class[T]): util.List[T] = {
    val json = getJson(url)
    try JSON.parseArray(json, clazz)
    catch {
      case ex: Exception =>
        logger.error("Failed to parse json " + json, ex)
        null
    }
  }

  def get(url: String, headers: util.Map[String, String]): String = {
    var json = ""
    try {
      val httpClient = buildClient
      val request = new HttpGet(url)
      import scala.collection.JavaConversions._
      for (entry <- headers.entrySet) {
        request.addHeader(entry.getKey, entry.getValue)
      }
      val response = httpClient.execute(request)
      if (response.getEntity != null) json = EntityUtils.toString(response.getEntity, DATA_ENCODING)
    } catch {
      case ex2: Exception =>
        json = ex2.getMessage
    }
    json
  }
}

@SerialVersionUID(4661645115933875389L)
class HttpClient extends Serializable {}