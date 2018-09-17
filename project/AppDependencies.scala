import play.sbt.PlayImport.filters
import sbt._

object AppDependencies {

  import play.core.PlayVersion

  val compile = Seq(
    filters,
    "uk.gov.hmrc"           %% "crypto"              % "4.4.0",
    "uk.gov.hmrc"           %% "http-verbs"          % "7.3.0",
    "uk.gov.hmrc"           %% "http-verbs-play-25"  % "0.12.0",
    "uk.gov.hmrc"           %% "play-auditing"       % "3.9.0-play-25",
    "uk.gov.hmrc"           %% "auth-client"         % "2.9.0-play-25",
    "uk.gov.hmrc"           %% "play-health"         % "3.5.0-play-25",
    "uk.gov.hmrc"           %% "play-config"         % "7.0.0",
    "uk.gov.hmrc"           %% "logback-json-logger" % "3.1.0",
    "com.typesafe.play"     %% "play"                % "2.5.12",
    "io.dropwizard.metrics" % "metrics-graphite"     % "3.2.5",
    "de.threedimensions"    %% "metrics-play"        % "2.5.13",
    "com.ning"              % "async-http-client"    % "1.8.15",
    "ch.qos.logback"        % "logback-core"         % "1.1.7"
  )

  val test = Seq(
    "com.typesafe.play"      %% "play-test"          % PlayVersion.current % "test",
    "org.scalacheck"         % "scalacheck_2.11"     % "1.12.5"            % "test",
    "org.mockito"            % "mockito-all"         % "1.9.5"             % "test",
    "org.pegdown"            % "pegdown"             % "1.5.0"             % "test",
    "com.github.tomakehurst" % "wiremock"            % "2.7.1"             % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1"             % "test",
    "uk.gov.hmrc"            %% "hmrctest"           % "2.4.0"             % "test"
  )
}
