package bootstrap.liftweb

import code.snippet._
import java.net.URL
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.util._
import no.arktekk.cms.{Logger => CmsLogger, _}
import no.arktekk.push._
import scala.util.Random
import net.liftweb.sitemap.Loc._
import no.javabin.atom2twitterpublish.Atom2TwitterSync
import java.util.concurrent.ScheduledFuture
import java.io.{FileInputStream, File}

class Boot {
  def boot {
    LiftRules.configureLogging = LoggingAutoConfigurer()

    val localFile = () => {
        val file = new File("/opt/jb/jz11/etc/jz11.props")
      if (file.exists) Full(new FileInputStream(file)) else Empty
    }
    Props.whereToLook = () => (("local", localFile) :: Nil)

    CmsUtil.skipEhcacheUpdateCheck

    // PubsubhubbubSubscriber
    val subscriptionUrl = new URL("http://localhost:8080/pubsubhubbub")
    val pubsubhubbub: PubsubhubbubSubscriber = new DefaultPubsubhubbubSubscriber(new Random(), subscriptionUrl)

    def hubCallback(hub: URL, topic: URL) {
      pubsubhubbub.addTopicToHub(hub, topic)
    }

    // CMS Integration
    val cmsLogger = new CmsLogger {
      private val logger = net.liftweb.common.Logger("CMS")

      def info(message: String) = logger.info(message)

      def warn(message: String) = logger.warn(message)
    }

    val cmsClient = CmsClient(cmsLogger, "CMS", new File(System.getProperty("user.home"), ".cms"), hubCallback)
    LiftRules.unloadHooks.append({
      cmsClient.close
    })

    LiftRules.snippetDispatch.append({
      case "jz" => new JavaZonePagesSnippet(cmsClient)
    })

    LiftRules.statelessRewrite.append(pageRewriter(cmsClient))

    // Hack to work around a bug in lift
    SiteMap.enforceUniqueLinks = false

    val pageSize = Positive.fromInt(2)

    def menu = List(
      Menu("default") / "index" >>
          Hidden >> EarlyResponse(() => Full(RedirectResponse("news.html"))),
      Menu("News") / "news" >>
          new NewsSnippets(cmsClient, None, pageSize),
      Menu("Static Page") / "static-page" >>
          Loc.If(() => S.param("slug").isDefined, () => NotFoundResponse("Go away")) >>
          new StaticPageSnippets(cmsClient),
      Menu("Static Post") / "static-post" >>
          Loc.If(() => S.param("slug").isDefined, () => NotFoundResponse("You don't exist")) >>
          new StaticPostSnippets(cmsClient))

    LiftRules.setSiteMapFunc(() => SiteMap(menu:_*))

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    //    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    //    LiftRules.dispatch.prepend {
    //      case Req("presentation" :: Nil, _, _) => () => Full(RedirectResponse("/presentation/benefits"))
    //    }

    val pshbLift = new PubsubhubbubSubscriberLift(cmsLogger, pubsubhubbub)
    LiftRules.statelessDispatchTable.append(pshbLift.dispatch)

    if (Props.get("atomtwitterfeed_enabled").map(_.toBoolean).openOr(true)) {
      def mandatoryProperty(propertyName: String): String = Props.get(propertyName).openOr(error("Property not found " + propertyName))
      val logger = Logger("atom2twittersync")
      val atom2TwitterSync = new Atom2TwitterSync(
        mandatoryProperty("atomtwitterfeed_atom_uri"),
        mandatoryProperty("atomtwitterfeed_application_id"),
        mandatoryProperty("atomtwitterfeed_consumer_key"),
        mandatoryProperty("atomtwitterfeed_consumer_secret"),
        mandatoryProperty("atomtwitterfeed_access_token"),
        mandatoryProperty("atomtwitterfeed_access_secret"),
        mandatoryProperty("atomtwitterfeed_twitter_handle"),
        (msg, maybExc) => maybExc.map(exc => logger.error(msg, exc)).getOrElse(logger.error(msg)), logger.info(_))
      import Helpers._
      val timeSpan = TimeSpan(Props.get("atomtwitterfeed_update_ms").map(_.toLong).openOr(300 seconds))
      var future: Option[ScheduledFuture[Unit]] = None
      def start: ScheduledFuture[Unit] = ActorPing.schedule({
        () =>
          atom2TwitterSync ! Atom2TwitterSync.Check
          future = Some(start)
      }, timeSpan)
      future = Some(start)
      LiftRules.unloadHooks.prepend(() => future.foreach(_.cancel(false)))
    }
  }

  val news = ParsePath(List("news"), "", true, false);
  val staticPage = ParsePath(List("static-page"), "", true, false);
  val staticPost = ParsePath(List("static-post"), "", true, false);

  def pageRewriter(cmsClient: CmsClient): LiftRules.RewritePF = {
    case RewriteRequest(ParsePath("news.html" :: Nil, _, _, false), GetRequest, _) =>
      RewriteResponse(news, Map.empty, true)
    case RewriteRequest(ParsePath(slug :: Nil, _, _, false), GetRequest, _)
      if cmsClient.getPageBySlug(CmsSlug.fromString(slug)).isDefined =>
      RewriteResponse(staticPage, Map("slug" -> slug), true)
    case RewriteRequest(ParsePath("news" :: slug :: Nil, _, _, false), GetRequest, _) =>
      RewriteResponse(staticPost, Map("slug" -> slug), true)
  }
}
