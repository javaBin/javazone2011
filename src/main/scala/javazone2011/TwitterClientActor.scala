package javazone2011

import net.liftweb.common.Logger
import org.apache.abdera.model._
import scala.actors.{TIMEOUT, Actor}
import scala.collection.JavaConversions._
import org.apache.abdera.protocol.client.{RequestOptions, AbderaClient}
import org.joda.time.{Period, Minutes, DateTime => DT}
import java.net.{URL, URI}

case class JzTweet(handle: String,
                   handleUrl: URL,
                   handleImage: URL,
                   name: String,
                   text: String,
                   htmlLink: URL,
                   timeAgo: String)

trait TwitterSearch {
  var searchUrlHtml: Option[URL]

  var currentResults: List[JzTweet]
}

class TwitterClientActor(logger: Logger, timeout: Minutes, uri: URI) extends Actor with TwitterSearch {

  private val NUMBER_OF_TWEETS_TO_SHOW = 7

  private val abderaClient = new AbderaClient()

  var currentResults: List[JzTweet] = Nil

  var searchUrlHtml: Option[URL] = None

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
//            logger.info("Got " + root.getEntries.size + " entries in feed")
            val x = TwitterClient.handleFeed(new DT(), root).take(NUMBER_OF_TWEETS_TO_SHOW)
//            logger.info("Got " + x.length + " tweets from feed")
            currentResults = x
            searchUrlHtml = TwitterClient.findLinkByRel(asScalaIterable(root.getLinks), "alternate")
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

  def handleFeed(now: DT, feed: Feed): List[JzTweet] = {
    asScalaIterable(feed.getEntries).flatMap(entryToJzTweet(now)).toList
  }

  def entryToJzTweet(now: DT)(entry: Entry): Option[JzTweet] = for {
    published <- Option(entry.getPublished)
    links = asScalaIterable(entry.getLinks).toList
    htmlLink <- findLinkByMimeType(links, "text/html")
    author <- Option(entry.getAuthor)
    handleName <- Option(author.getName) if handleName.endsWith(")")
    handleUri <- Option(author.getUri)
    // Those without an image get a stock image from Twitter
    handleImage <- findLinkByRel(links, "image")
    i = handleName.indexOf(' ') if i > 0 && i < handleName.length - 2
    handle = handleName.substring(0, i)
    name = handleName.substring(i + 2, handleName.length - 1) if name.trim().length() > 1
    text = entry.getTitle.toString if name.trim().length() > 1
    period = new Period(new DT(published.getTime), now)
  } yield {
    JzTweet(handle, handleUri.toURL, handleImage,
      name, text, htmlLink, formatToTimeAgo(period))
  }

  def findLinkByMimeType(links: Iterable[Link], mimeType: String): Option[URL] = for {
    link <- links.find(link => mimeType.equals(String.valueOf(link.getMimeType)))
    href <- Option(link.getHref)
  } yield href.toURL

  def findLinkByRel(links: Iterable[Link], rel: String): Option[URL] = for {
    link <- links.find(link => rel.equals(String.valueOf(link.getRel)))
    href <- Option(link.getHref)
  } yield href.toURL

  def formatToTimeAgo(period: Period): String =
    if (period.getYears > 0) period.getYears + " years ago"
    else if (period.getMonths > 0) period.getMonths + " months ago"
    else if (period.getDays > 0) period.getDays + " days ago"
    else if (period.getHours > 0) period.getHours + " hours ago"
    else if (period.getMinutes > 0) period.getMinutes + " minutes ago"
    else if (period.getSeconds > 0) period.getSeconds + " seconds ago"
    else "just now"
}
