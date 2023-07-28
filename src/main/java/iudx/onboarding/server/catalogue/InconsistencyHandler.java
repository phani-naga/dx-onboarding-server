package iudx.onboarding.server.catalogue;

import io.vertx.core.Future;
import iudx.onboarding.server.catalogue.service.LocalCatImpl;
import iudx.onboarding.server.token.TokenService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InconsistencyHandler {

  private static Logger LOGGER = LogManager.getLogger(InconsistencyHandler.class);

  TokenService tokenService;

  LocalCatImpl localCat;

  InconsistencyHandler(TokenService tokenService, LocalCatImpl localCat) {
    this.tokenService = tokenService;
    this.localCat = localCat;
  }

  /**
   * This method is meant to delete item on local when upload to central fails
   *
   * @return Future which of the type void
   */
  Future<Void> handleDeleteOnLocal(final LocalCatImpl localCat, final String id, final String token) {

    localCat.deleteItem(id, token)
        .onSuccess(successHandler -> {
          LOGGER.info("Item deleted from local after upload to central failed");
        }).onFailure(failureHandler -> {
          LOGGER.error("ITEM NOT DELETED FROM LOCAL - INCONSISTENT");
        });

    return Future.succeededFuture();
  }
}
