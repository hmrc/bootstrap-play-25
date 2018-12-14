import play.sbt.PlayImport.filters
import sbt._

object AppDependencies {

  import play.core.PlayVersion

  val akkaVersion = "2.5.18"

  val compile = Seq(
    filters,
    "com.typesafe.akka"     %% "akka-actor"                 % akkaVersion,
    "com.typesafe.akka"     %% "akka-stream"                % akkaVersion,
    "com.typesafe.akka"     %% "akka-slf4j"                 % akkaVersion,
    "uk.gov.hmrc"           %% "crypto"                     % "5.2.0",
    "uk.gov.hmrc"           %% "http-verbs"                 % "9.0.0-play-25",
    "uk.gov.hmrc"           %% "play-auditing"              % "3.14.0-play-25",
    "uk.gov.hmrc"           %% "auth-client"                % "2.11.0-play-25",
    "uk.gov.hmrc"           %% "play-health"                % "3.9.0-play-25",
    "uk.gov.hmrc"           %% "play-config"                % "7.2.0",
    "uk.gov.hmrc"           %% "logback-json-logger"        % "4.1.0",
    "com.typesafe.play"     %% "play"                       % PlayVersion.current,
    "io.dropwizard.metrics" % "metrics-graphite"            % "3.2.5",
    "de.threedimensions"    %% "metrics-play"               % "2.5.13",
    "ch.qos.logback"        % "logback-classic"             % "1.2.3",
    "com.github.rishabh9"   %% "mdc-propagation-dispatcher" % "0.0.5",
    // force dependencies due to security flaws found in jackson-databind < 2.9.x using XRay
    "com.fasterxml.jackson.core"     % "jackson-core"            % "2.9.7",
    "com.fasterxml.jackson.core"     % "jackson-databind"        % "2.9.7",
    "com.fasterxml.jackson.core"     % "jackson-annotations"     % "2.9.7",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8"   % "2.9.7",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.9.7",
    // force dependencies due to security flaws found in xercesImpl 2.11.0
    "xerces" % "xercesImpl" % "2.12.0"
  )

  val test = Seq(
    "com.typesafe.play"      %% "play-test"          % PlayVersion.current % "test",
    "org.scalacheck"         % "scalacheck_2.11"     % "1.14.0"            % "test",
    "org.mockito"            % "mockito-all"         % "1.9.5"             % "test",
    "org.pegdown"            % "pegdown"             % "1.5.0"             % "test",
    "com.github.tomakehurst" % "wiremock"            % "2.7.1"             % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1"             % "test",
    "uk.gov.hmrc"            %% "hmrctest"           % "3.3.0"             % "test"
  )
}
