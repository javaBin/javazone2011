package code.snippet

import net.liftweb.sitemap.Loc.DispatchLocSnippets
import xml.NodeSeq
import net.liftweb.http.S
import no.arktekk.cms.{CmsSlug, CmsClient}

/**
 * @author Thor Åge Eldby (thoraageeldby@gmail.com)
 */

class StaticPostSnippets(cmsClient: CmsClient) extends DispatchLocSnippets {
  def dispatch: PartialFunction[String, NodeSeq => NodeSeq] = {
    case "page" => _ =>
      S.param("slug").map {
        slug =>
          cmsClient.getPostBySlug(CmsSlug.fromString(slug)).map {
            entry =>
              <xml:group>
                <h2>{entry.title}</h2>
                <div>{entry.content}</div>
              </xml:group>
          }
      }.openOr(None).getOrElse(NodeSeq.Empty)
  }
}