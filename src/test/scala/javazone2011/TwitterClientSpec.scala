package javazone2011

import java.net.URL
import org.apache.abdera.Abdera
import org.apache.abdera.model._
import org.specs._
import org.joda.time.{Period, DateTime => DT}

class TwitterClientSpec extends Specification {
  import TwitterClient._

  val abdera = new Abdera
  val url = new URL("http://twitter.com/javazone")
  val handleImage = new URL("http://twitter.com/javazone.png")
  val now = new DT(2011, 02, 11, 18, 34, 0, 0)
  val entryTime = new DT(2011, 02, 11, 15, 44, 0, 0)

  "Twitter parser" should {
    "work" in {
      entryToJzTweet(now)(generateEntry("javazone (JavaZone)", "tjoho!")) must
          beSome(JzTweet("javazone", url, handleImage, "JavaZone", "tjoho!", new URL("http://yo"), "2 hours ago"))

      entryToJzTweet(now)(generateEntry("javazone (Java Zone)", "tjoho!")) must
          beSome(JzTweet("javazone", url, handleImage, "Java Zone", "tjoho!", new URL("http://yo"), "2 hours ago"))

      entryToJzTweet(now)(generateEntry("javazone (jeg er ) vanskelig)", "tjoho!")) must
          beSome(JzTweet("javazone", url, handleImage, "jeg er ) vanskelig", "tjoho!", new URL("http://yo"), "2 hours ago"))

      entryToJzTweet(now)(generateEntry(" (jeg er ) vanskelig)", "tjoho!")) must beNone

      entryToJzTweet(now)(generateEntry("javazone (", "tjoho!")) must beNone

      entryToJzTweet(now)(generateEntry("javazone (foo", "tjoho!")) must beNone

      entryToJzTweet(now)(generateEntry("j ()", "tjoho!")) must beNone
    }

    "format stuff properly" in {
      val p = new Period(entryTime, now)
      formatToTimeAgo(p) mustEqual "2 hours ago"
    }
  }

  def generateEntry(author: String, title: String) = {
    val entry = abdera.newEntry
    entry.setPublished(entryTime.toDate)
    val a = abdera.getFactory.newAuthor()
    a.setName(author)
    a.setUri(url.toExternalForm)
    entry.addAuthor(a)
    entry.addLink({
      val link = abdera.getFactory.newLink
      link.setHref("http://yo")
      link.setMimeType("text/html")
      link
    })
    entry.addLink({
      val image = abdera.getFactory.newLink
      image.setHref(handleImage.toExternalForm)
      image.setRel("image")
      image
    })
    entry.setTitle(title, Text.Type.TEXT)
    entry
  }
}
