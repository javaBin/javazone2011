import sbt._

class JavaZone2011Project(info: ProjectInfo) extends DefaultWebProject(info) with IdeaProject
{
  val liftVersion = "2.1"
  val logbackVersion = "0.9.18"
  val abderaVersion = "1.1"

  override def managedStyle = ManagedStyle.Maven

  val arktekkRepo = "arktekk" at "http://dev.eventsystems.no/nexus/content/repositories/arktekk-public-snapshot"
  val javaBinRepo = "javabin" at "http://smia.java.no/maven/repo/snapshot"
  Credentials(Path.userHome / ".ivy2" / "javabin.properties", log)
  /**
Your ~/.ivy2/javabin.properties must contain:
host=smia.java.no
user=<your ldap username>
password=<your ldap password>
  */

  override def scanDirectories = Nil
  override def jettyWebappPath = webappPath

  override def libraryDependencies = Set(
    "net.liftweb" %% "lift-common" % liftVersion % "compile->default" withSources(),
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default" withSources(),
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default" withSources(),
    "net.liftweb" %% "lift-util" % liftVersion % "compile->default" withSources(),

    "no.arktekk.atom-client" %% "atom-client-lift" % "1.0-SNAPSHOT" /*withSources()*/,

    "org.slf4j" % "slf4j-log4j12" % "1.6.1",

    "org.scala-tools.testing" %% "specs" % "1.6.5" % "test" withSources(),
    "org.mortbay.jetty" % "jetty" % "6.1.22" % "test->default" withSources(),
    "junit" % "junit" % "4.5" % "test->default",
    "org.scala-tools.testing" %% "specs" % "1.6.5" % "test->default" withSources(),
    "com.h2database" % "h2" % "1.2.138"
  ) ++ super.libraryDependencies
}
