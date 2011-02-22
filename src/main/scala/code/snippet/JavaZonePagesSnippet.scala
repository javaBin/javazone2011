package code.snippet

import net.liftweb.http.DispatchSnippet
import no.arktekk.cms._
import org.joda.time.format._
import scala.xml.NodeSeq
import javazone2011.{JzTweet, TwitterSearch}

class JavaZonePagesSnippet(val cmsClient: CmsClient, val twitterSearch: TwitterSearch) extends DispatchSnippet {

  import JavaZonePagesSnippet._

  val dispatch: DispatchIt = {
    case "topPages" => topPages _
    case "tweets" => tweets _
    case "moreTweets" => moreTweets _
  }

  def topPages(body: NodeSeq): NodeSeq =
    <ul>
      <li>
        <a href="/news.html">News</a>
      </li>
      {NodeSeq.fromSeq(cmsClient.getTopPages.map(pageToLi))}
    </ul>

  def tweets(body: NodeSeq): NodeSeq =
    <ul id="twitter_update_list">
      {twitterSearch.currentResults.map(tweetToLi(_))}
    </ul>

  private def tweetToLi(tweet: JzTweet): NodeSeq =
    <li>
      <span class="handle"><a href={tweet.handleUrl.toExternalForm}>{tweet.handle}</a></span>:
      <span class="text">{tweet.text}</span><br/>
      <span class="meta">{tweet.timeAgo} &#183; <a class="tweet_link" href={tweet.htmlLink.toString}>View Tweet</a></span>
    </li>

  def moreTweets(body: NodeSeq): NodeSeq =
    twitterSearch.searchUrlHtml.map(url => <a href={url.toExternalForm}>More...</a>).getOrElse(NodeSeq.Empty)
}

object JavaZonePagesSnippet {
  def pageToLi(page: CmsEntry) = <li>{pageToA(page)}</li>

  def pageToA(page: CmsEntry) = <a href={"/" + page.slug + ".html"}>{page.title}</a>

  val postDateTimeFormatter: DateTimeFormatter = new DateTimeFormatterBuilder().
    appendDayOfMonth(2).
    appendLiteral(' ').
    appendMonthOfYearText().
    appendLiteral(' ').
    appendYear(4, 4).
    toFormatter()
}
