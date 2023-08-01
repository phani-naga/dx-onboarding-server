package iudx.onboarding.server.catalogue;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.Future;
import iudx.onboarding.server.catalogue.service.CentralCatImpl;
import iudx.onboarding.server.catalogue.service.LocalCatImpl;
import iudx.onboarding.server.token.TokenService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InconsistencyHandler {

  private static Logger LOGGER = LogManager.getLogger(InconsistencyHandler.class);

  TokenService tokenService;

  LocalCatImpl localCat;
  CentralCatImpl centralCat;
  CircuitBreaker circuitBreaker;

  InconsistencyHandler(TokenService tokenService, LocalCatImpl localCat, CentralCatImpl centralCat, CircuitBreaker circuitBreaker) {
    this.tokenService = tokenService;
    this.localCat = localCat;
    this.centralCat = centralCat;
    this.circuitBreaker = circuitBreaker;
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
          LOGGER.error("INCONSISTENCY DETECTED : ITEM NOT DELETED FROM LOCAL - INCONSISTENT");
        });

    return Future.succeededFuture();
  }

  /**
   * This method is meant to restore item on local when update on central fails
   *
   * @return Future which of the type void
   */
  Future<Void> handleUpdateOnLocal(final LocalCatImpl localCat, final CentralCatImpl centralCat, final String id, final String token) {

    centralCat.getItem(id)
        .onSuccess(oldItem -> {

          localCat.updateItem(oldItem, token)
              .onSuccess(successHandler -> {
                LOGGER.info("Update on local reverted after failure on central");
              }).onFailure(failureHandler -> {
                LOGGER.error("INCONSISTENCY DETECTED : ITEM NOT RESTORED ON LOCAL");
              });
        })
        .onFailure(failureHandler -> {
          LOGGER.error("INCONSISTENCY DETECTED : ITEM NOT RESTORED ON LOCAL");
        });

    return Future.succeededFuture();
  }

  /**
   * This method is meant to restore item on local when update on central fails
   *
   * @return Future which of the type void
   */
  Future<Void> handleUploadToLocal(final LocalCatImpl localCat, final CentralCatImpl centralCat, final String id, final String token) {

    centralCat.getItem(id)
        .onSuccess(item -> {

          localCat.createItem(item, token)
              .onSuccess(successHandler -> {
                LOGGER.info("Delete on local reverted after failure on central");
              }).onFailure(failureHandler -> {
                LOGGER.error("INCONSISTENCY DETECTED : ITEM NOT RESTORED ON LOCAL");
              });
        }).onFailure(failureHandler -> {
          LOGGER.error("INCONSISTENCY DETECTED : ITEM NOT RESTORED ON LOCAL");
        });

    return Future.succeededFuture();
  }
}
