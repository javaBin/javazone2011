package javazone2011

import org.apache.abdera.Abdera
import org.apache.abdera.model._
import org.specs._
import java.net.URI

class TwitterClientSpec extends Specification {
  import TwitterClient._

  val abdera = new Abdera

  "Twitter parser" should {
    "work" in {
      entryToJzTweet(generateEntry("javazone (JavaZone)", "tjoho!")) must
          beSome(JzTweet("javazone", "JavaZone", "tjoho!", new URI("http://yo"), ""))

      entryToJzTweet(generateEntry("javazone (Java Zone)", "tjoho!")) must
          beSome(JzTweet("javazone", "Java Zone", "tjoho!", new URI("http://yo"), ""))

      entryToJzTweet(generateEntry("javazone (jeg er ) vanskelig)", "tjoho!")) must
          beSome(JzTweet("javazone", "jeg er ) vanskelig", "tjoho!", new URI("http://yo"), ""))

      entryToJzTweet(generateEntry(" (jeg er ) vanskelig)", "tjoho!")) must beNone

      entryToJzTweet(generateEntry("javazone (", "tjoho!")) must beNone

      entryToJzTweet(generateEntry("javazone (foo", "tjoho!")) must beNone

      entryToJzTweet(generateEntry("j ()", "tjoho!")) must beNone
    }
  }

  def generateEntry(author: String, title: String) = {
    val entry = abdera.newEntry
    val a = abdera.getFactory.newAuthor()
    a.setName(author)
    entry.addAuthor(a)
    val link = abdera.getFactory.newLink
    link.setHref("http://yo")
    link.setMimeType("text/html")
    entry.addLink(link)
    entry.setTitle(title, Text.Type.TEXT)
    entry
  }
}
