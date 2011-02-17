package code.snippet

import net.liftweb.common._
import net.liftweb.http.S
import net.liftweb.sitemap.Loc._
import no.arktekk.cms.{CmsClient, CmsSlug}
import scala.xml._

class BannerSnippet(val cmsClient: CmsClient) extends DispatchLocSnippets with Logger {
  import BannerSnippet._
  def dispatch: PartialFunction[String, NodeSeq => NodeSeq] = {
    case "banner" => _ =>
      (for {
        slug <- S.param("slug").map(CmsSlug.fromString)
        //TODO: guard on slug && userAgent != handheld
      } yield createBannerFor("news")).openOr(NodeSeq.Empty)
  }
}

object BannerSnippet {
  //TODO: generify this to 'discover' banners from the images folder...
  def createBannerFor(name:String) =
    <div id="banner">
      <div id="banner-content">
        <ol class="images">
          <li>
              <img src="/images/banner/{name}/1.jpg" alt=""/>
          </li>
          <li>
              <img src="/images/banner/{name}/2.jpg" alt=""/>
          </li>
          <li>
              <img src="/images/banner/{name}/3.jpg" alt=""/>
          </li>
        </ol>
      </div>
      <div id="prev" class="btn-nav">
          <img src="/images/banner/nav-left.png" alt=""/>
      </div>
      <div id="next" class="btn-nav">
          <img src="/images/banner/nav-right.png" alt=""/>
      </div>
        <img class="message" src="/images/banner/{name}/message.png" alt=""/>
    </div>
}
