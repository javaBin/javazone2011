package code.snippet

import net.liftweb.http.DispatchSnippet
import no.arktekk.cms._
import org.joda.time.format._
import scala.xml.NodeSeq

class JavaZonePagesSnippet(val cmsClient: CmsClient) extends DispatchSnippet {

  import JavaZonePagesSnippet._

  val dispatch: DispatchIt = {
    case "topPages" => topPages _
  }

  def topPages(body: NodeSeq): NodeSeq = {
    <ul>
      <li>
        <a href="/news.html">News</a>
      </li>
      {NodeSeq.fromSeq(cmsClient.getTopPages.map(pageToLi))}
    </ul>
  }
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
