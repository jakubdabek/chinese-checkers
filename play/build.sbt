name := "play"
 
version := "1.0" 
      
lazy val `play` = (project in file(".")).enablePlugins(PlayJava)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
scalaVersion := "2.11.11"

libraryDependencies ++= Seq( javaJdbc , ehcache , javaWs , guice )
// https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib
libraryDependencies += "org.jetbrains.kotlin" % "kotlin-stdlib" % "1.3.11"


unmanagedClasspath in Compile += baseDirectory.value / "../out/production/classes"
unmanagedClasspath in Runtime += baseDirectory.value / "../out/production/classes"
unmanagedResourceDirectories in Test += { baseDirectory ( _ /"target/web/public/test" ).value }

      