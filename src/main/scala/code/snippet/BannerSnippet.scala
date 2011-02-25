package code.snippet

import net.liftweb.common._
import scala.xml._
import net.liftweb.http.{DispatchSnippet, S}

class BannerSnippet extends DispatchSnippet with Logger {
  import BannerSnippet._
  val dispatch: DispatchIt = {
    case _ => _ => banner(S.uri, S.getRequestHeader("User-Agent").openOr("Default"))
  }
}

object BannerSnippet {
  def banner(uri:String, userAgent:String):NodeSeq = {
    val pageName = getSlug(uri)
    
    if (hasBanner(pageName) && isNotMobile(userAgent))
      <div id="banner">
        <div id="banner-content">
          <ol class="images">
            <li class="prev">
                <img src={"/images/banner/" + pageName + "/1.jpg"} alt=""/>
            </li>
            <li class="curr">
                <img src={"/images/banner/" + pageName + "/2.jpg"} alt=""/>
            </li>
            <li class="next">
                <img src={"/images/banner/" + pageName + "/3.jpg"} alt=""/>
            </li>
          </ol>
        </div>
        <div id="prev" class="btn-nav">
            <img src="/images/banner/nav-left.png" alt=""/>
        </div>
        <div id="next" class="btn-nav">
            <img src="/images/banner/nav-right.png" alt=""/>
        </div>
        <img class="message" src={"/images/banner/" + pageName + "/message.png"} alt=""/>
      </div>
    else NodeSeq.Empty
  }

  private def isNotMobile(userAgent:String) = {
    val s = userAgent.toLowerCase
    s.contains("ipad") ||  s.contains("iphone") || s.contains("android")
  }

  private def hasBanner(slug:String) = List("news", "journeyzone", "clubzone").contains(slug)
  private def getSlug(path:String) = path.replace("/", "").replace(".html", "")
}

