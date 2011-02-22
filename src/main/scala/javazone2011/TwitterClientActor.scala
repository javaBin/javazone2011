package javazone2011

import java.net.URI
import net.liftweb.common.Logger
import org.apache.abdera.model._
import scala.actors.{TIMEOUT, Actor}
import scala.collection.JavaConversions._
import org.apache.abdera.protocol.client.{RequestOptions, AbderaClient}
import org.joda.time.format.PeriodFormatterBuilder
import org.joda.time.{Period, Minutes, DateTime => DT}

case class JzTweet(handle: String, name: String, text: String, htmlLink: URI, timeAgo: String)

trait TwitterSearch {
  var currentResults: List[JzTweet]
}

class TwitterClientActor(logger: Logger, timeout: Minutes, uri: URI) extends Actor with TwitterSearch {

  private val abderaClient = new AbderaClient()

  var currentResults: List[JzTweet] = Nil

  def act() {
    loop {
      try {
        reactWithin(timeout.getMinutes * 60 * 1000) {
          case TIMEOUT =>
            update()
          case TwitterClient.Update =>
            update()
          case x =>
            logger.warn("Unknown message")
        }
      } catch {
        case e: Exception =>
          logger.error("Unable to update twitter search: " + e.getMessage, e)
      }
    }
  }

  def update() {
    logger.info("Searching twitter...")
    val options: RequestOptions = new RequestOptions
    options.setHeader("User-Agent", "JavaZone")
    val clientResponse = abderaClient.get(uri.toString, options)

    clientResponse.getStatus match {
      case 200 =>
        val contentType = clientResponse.getContentType

        contentType.getPrimaryType + "/" + contentType.getSubType match {
          case "application/atom+xml" =>
            val root = clientResponse.getDocument[Feed].getRoot
            logger.info("Got " + root.getEntries.size + " entries in feed")
            val x = TwitterClient.handleFeed(root)
            logger.info("Got " + x.length + " tweets from feed")
            currentResults = x
          case _ =>
            logger.warn("Unknown content type: " + contentType)
        }
      case x =>
        logger.warn("Can't handle response code: " + x + ". Status: " + clientResponse.getStatusText)
    }
  }
}

object TwitterClient {

  object Update

  def handleFeed(feed: Feed): List[JzTweet] = {
    asScalaIterable(feed.getEntries).flatMap(entryToJzTweet).toList
  }

  def entryToJzTweet(entry: Entry): Option[JzTweet] = for {
    published <- Option(entry.getPublished)
    htmlLink <- asScalaIterable(entry.getLinks).find(_.getMimeType.toString.equals("text/html"))
    handleName <- Option(entry.getAuthor).map(_.getName) if handleName.endsWith(")")
    i = handleName.indexOf(' ') if i > 0 && i < handleName.length - 2
    handle = handleName.substring(0, i)
    name = handleName.substring(i + 2, handleName.length - 1) if name.trim().length() > 1
    text = entry.getTitle.toString if name.trim().length() > 1
    period = new Period(new DT(published.getTime), new DT)
  } yield {
    JzTweet(handle, name, text, htmlLink.getHref.toURI, timeAgoFormatter.print(period))
  }

  val timeAgoFormatter = new PeriodFormatterBuilder().
      appendSeconds().appendSuffix(" seconds ago\n").
      appendMinutes().appendSuffix(" minutes ago\n").
      appendHours().appendSuffix(" hours ago\n").
      appendDays().appendSuffix(" days ago\n").
      appendMonths().appendSuffix(" months ago\n").
      appendYears().appendSuffix(" years ago\n").
      printZeroNever().
      toFormatter();
}
