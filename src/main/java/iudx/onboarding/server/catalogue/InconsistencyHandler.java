package iudx.onboarding.server.catalogue;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import dev.failsafe.RetryPolicyBuilder;
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
  RetryPolicyBuilder<Object> retryPolicyBuilder;

  InconsistencyHandler(TokenService tokenService, LocalCatImpl localCat, CentralCatImpl centralCat, RetryPolicyBuilder<Object> retryPolicyBuilder) {
    this.tokenService = tokenService;
    this.localCat = localCat;
    this.centralCat = centralCat;
    this.retryPolicyBuilder = retryPolicyBuilder;
  }

  /**
   * This method is meant to delete item on local when upload to central fails
   *
   * @return Future which of the type void
   */
  Future<Void> handleDeleteOnLocal(final LocalCatImpl localCat, final String id, final String token) {

    RetryPolicy<Object> retryPolicy = retryPolicyBuilder
        .onSuccess(listener -> LOGGER.info("Item deleted from local after upload to central failed"))
        .onFailure(failureListener -> {
          LOGGER.error("INCONSISTENCY DETECTED : ITEM NOT DELETED FROM LOCAL - INCONSISTENT");
        })
        .build();

    Failsafe.with(retryPolicy)
        .getAsyncExecution(asyncExecution -> {
          localCat.deleteItem(id, token)
              .onSuccess(successHandler -> {
                asyncExecution.complete();
              }).onFailure(asyncExecution::recordException);
        });

    return Future.succeededFuture();
  }

  /**
   * This method is meant to restore item on local when update on central fails
   *
   * @return Future which of the type void
   */
  Future<Void> handleUpdateOnLocal(final LocalCatImpl localCat, final CentralCatImpl centralCat, final String id, final String token) {

    RetryPolicy<Object> retryPolicy = retryPolicyBuilder
        .onSuccess(listener -> LOGGER.info("Update on local reverted after failure on central"))
        .onFailure(failureListener -> {
          LOGGER.error("INCONSISTENCY DETECTED : ITEM NOT RESTORED ON LOCAL");
        })
        .build();

    Failsafe.with(retryPolicy)
        .getAsyncExecution(asyncExecution -> {
          centralCat.getItem(id)
              .onSuccess(oldItem -> {
                localCat.updateItem(oldItem, token)
                    .onSuccess(successHandler -> {
                      asyncExecution.complete();
                    }).onFailure(asyncExecution::recordException);
              })
              .onFailure(asyncExecution::recordException);
        });

    return Future.succeededFuture();
  }

  /**
   * This method is meant to restore item on local when update on central fails
   *
   * @return Future which of the type void
   */
  Future<Void> handleUploadToLocal(final LocalCatImpl localCat, final CentralCatImpl centralCat, final String id, final String token) {

    RetryPolicy<Object> retryPolicy = retryPolicyBuilder
        .onSuccess(listener -> LOGGER.info("Update on local reverted after failure on central"))
        .onFailure(failureListener -> {
          LOGGER.error("INCONSISTENCY DETECTED : ITEM NOT RESTORED ON LOCAL");
        })
        .build();

    Failsafe.with(retryPolicy)
        .getAsyncExecution(asyncExecution -> {
          centralCat.getItem(id)
              .onSuccess(item -> {
                localCat.createItem(item, token)
                    .onSuccess(successHandler -> {
                      asyncExecution.complete();
                    }).onFailure(asyncExecution::recordException);

              }).onFailure(asyncExecution::recordException);
        });

    return Future.succeededFuture();
  }
}
