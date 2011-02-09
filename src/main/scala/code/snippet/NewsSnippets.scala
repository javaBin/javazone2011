package code.snippet

import code.snippet.JavaZonePagesSnippet._
import net.liftweb.common._
import net.liftweb.http.S
import net.liftweb.sitemap.Loc._
import no.arktekk._
import no.arktekk.cms.CmsUtil._
import no.arktekk.cms.{Logger => _, _}
import scala.xml._

class NewsSnippets(cmsClient: CmsClient, offset: Option[Int], limit: Positive) extends DispatchLocSnippets with Logger {
  def dispatch: PartialFunction[String, NodeSeq => NodeSeq] = {
    case "news-list" => _ =>
      val offset = this.offset.
          orElse(S.param("start").toOption.flatMap(parseInt)).
          getOrElse(0)

      val response = cmsClient.getEntriesForCategory("News", offset, limit)

      response.page.map(entryToHtml) ++ readMoreLink(response, offset)
  }

  // It might make sense to reuse this
  def entryToHtml(entry: CmsEntry) =
    <div class="newsframe">
      {entry.updatedOrPublished.map(date => <span class="timestamp">{postDateTimeFormatter.print(date)}</span>).getOrElse(NodeSeq.Empty)}
      <div class="expand_newsframe">
          <a href={ "/news/" + entry.slug + ".html" }>
            <img src="/permalink.png" alt="co" title="Contract"/>
          </a>
      </div>
      <h2>{entry.title}</h2>
      <div>{entry.content}</div>
    </div>

  def readMoreLink(response: OpenSearchResponse, offset: Int): NodeSeq = {
    val prev = response.prevStart.map(i => <a href={"/news.html?start=" + i}>Prev</a>)
    val next = response.nextStart.map(i => <a href={"/news.html?start=" + i}>Next</a>)

    prev.getOrElse(Text("Prev")) ++ Text(" - ") ++ next.getOrElse(Text("Next"))
  }
}
