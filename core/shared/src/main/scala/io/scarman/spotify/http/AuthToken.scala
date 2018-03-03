package io.scarman.spotify.http

import java.util.Base64
import scala.concurrent._
import scala.util._

import fr.hmil.roshttp.body._
import fr.hmil.roshttp.{Method, HttpRequest => HR}
import io.circe.generic.auto._
import io.circe.parser._
import monix.execution.Scheduler.Implicits.global
import scribe.{Level, Logger, Logging}

import io.scarman.spotify.request.Endpoints
import io.scarman.spotify.response.AccessToken

private[spotify] trait AuthToken extends Logging {
  Logger.update(getClass.getName)(_.clearHandlers().withHandler(minimumLevel = Level.Debug))
  private val baseRequest = Endpoints.Token
    .withMethod(Method.POST)
    .withHeader("Content-Type", "application/x-www-form-urlencoded")

  private val baseBody = ("grant_type", "client_credentials")

  private def base64(id: String, secret: String): String = {
    Base64.getEncoder.encodeToString(s"$id:$secret".getBytes)
  }

  private def tokenRequest(req: HR): Future[String] = {
    req.send().map { r =>
      logger.info(s"Code: ${r.statusCode}")
      r.body
    }
  }

  protected def getToken(id: String, secret: String): Future[AccessToken] = {
    val authHeader: String = base64(id, secret)
    val request = baseRequest
      .withHeader("Authorization", s"Basic $authHeader")
      .withBody(URLEncodedBody(baseBody))

    logger.info(s"Requesting token for $authHeader")
    val req = tokenRequest(request).recover {
      case t: Throwable => logger.info(s"${t.getMessage}"); throw t
    }
    logger.info("Got req back?")

    req.map(s => decode[AccessToken](s)).map {
      case Right(v) => logger.info(s"Decoded $v"); v
      case Left(e)  => throw e
    }
  }

  protected[spotify] def refreshToken(id: String, secret: String, token: Future[AccessToken]): Future[AccessToken] = {
    token.flatMap { t =>
      if (!t.isExpired) {
        Future.successful(t)
      } else {
        getToken(id, secret)
      }
    }
  }
}
