package controllers

import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents}
import repo.ApplicationRepo

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

class InstallController @Inject() (
    cc: ControllerComponents,
    appRepo: ApplicationRepo,
    config: Configuration
) extends AbstractController(cc) {

  private val stableBaseUrlO = configUrl("service.stableBaseUrl")

  private val betaBaseUrlO = configUrl("service.betaBaseUrl")

  def install(beta: Boolean, rcUpdate: Option[Boolean]) = Action.async { _ =>
    appRepo.findApplication().map { maybeApp =>
      val response = for {
        stableBaseUrl <- stableBaseUrlO
        betaBaseUrl   <- betaBaseUrlO
        app           <- maybeApp
        stableVersion = app.stableCliVersion
        betaVersion   = app.betaCliVersion
      } yield
        if (beta) {
          Ok(
            views.txt.install_beta(
              cliVersion = betaVersion,
              baseUrl = betaBaseUrl,
              rcUpdate = rcUpdate.getOrElse(true)
            )
          )
        } else {
          Ok(
            views.txt.install_stable(
              cliVersion = stableVersion,
              baseUrl = stableBaseUrl,
              rcUpdate = rcUpdate.getOrElse(true)
            )
          )
        }
      response getOrElse ServiceUnavailable
    }
  }

  private def configUrl(url: String): Option[String] = config.getOptional[String](url)
}
