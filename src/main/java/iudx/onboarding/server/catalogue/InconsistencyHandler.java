package iudx.onboarding.server.catalogue;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import dev.failsafe.RetryPolicyBuilder;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
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
  Future<Void> handleDeleteOnLocal(final String id, final String token) {

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
  Future<Void> handleUpdateOnLocal(final String id, final String token) {

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
  Future<Void> handleUploadToLocal(final String id, final String token) {

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

    /**
     * This method is meant to delete instance on local when upload to central fails
     *
     * @return Future which is of the type void
     */
  Future<Void> handleDeleteInstanceOnLocal(final String id, final String token) {

    RetryPolicy<Object> retryPolicy =
        retryPolicyBuilder
            .onSuccess(
                listener ->
                    LOGGER.info("Instance deleted from local after upload to central failed"))
            .onFailure(
                failureListener -> {
                  LOGGER.error(
                      "INCONSISTENCY DETECTED : INSTANCE NOT DELETED FROM LOCAL - INCONSISTENT");
                })
            .build();

    Failsafe.with(retryPolicy)
        .getAsyncExecution(
            asyncExecution -> {
              localCat
                  .deleteInstance(id, token)
                  .onSuccess(
                      successHandler -> {
                        asyncExecution.complete();
                      })
                  .onFailure(asyncExecution::recordException);
            });

    return Future.succeededFuture();
  }

    /**
     * This method is meant to restore instance on local when update on central fails
     *
     * @return Future which is of the type void
     */
  Future<Void> handleUploadInstanceToLocal(final String id, final String token) {

    RetryPolicy<Object> retryPolicy =
        retryPolicyBuilder
            .onSuccess(listener -> LOGGER.info("Update on local reverted after failure on central"))
            .onFailure(
                failureListener -> {
                  LOGGER.error("INCONSISTENCY DETECTED : INSTANCE NOT RESTORED ON LOCAL");
                })
            .build();

    Failsafe.with(retryPolicy)
        .getAsyncExecution(
            asyncExecution -> {
              centralCat
                  .getInstance(id)
                  .onSuccess(
                      item -> {
                        JsonObject deletedItem = item.getJsonArray("results").getJsonObject(0);
                        deletedItem.put("instanceId", id);
                        localCat
                            .createInstance(deletedItem, token)
                            .onSuccess(
                                successHandler -> {
                                  asyncExecution.complete();
                                })
                            .onFailure(asyncExecution::recordException);
                      })
                  .onFailure(asyncExecution::recordException);
            });

    return Future.succeededFuture();
  }

    /**
     * This method is meant to restore item on local when update on central fails
     *
     * @return Future which is of the type void
     */
  Future<Void> handleUpdateInstanceOnLocal(final String id, final String token) {

    RetryPolicy<Object> retryPolicy =
        retryPolicyBuilder
            .onSuccess(listener -> LOGGER.info("Update on local reverted after failure on central"))
            .onFailure(
                failureListener -> {
                  LOGGER.error("INCONSISTENCY DETECTED : INSTANCE NOT RESTORED ON LOCAL");
                })
            .build();

    Failsafe.with(retryPolicy)
        .getAsyncExecution(
            asyncExecution -> {
              centralCat
                  .getInstance(id)
                  .onSuccess(
                      oldItem -> {
                        JsonObject item = oldItem.getJsonArray("results").getJsonObject(0);
                        localCat
                            .updateInstance(id, item, token)
                            .onSuccess(
                                successHandler -> {
                                  asyncExecution.complete();
                                })
                            .onFailure(asyncExecution::recordException);
                      })
                  .onFailure(asyncExecution::recordException);
            });

    return Future.succeededFuture();
  }

  /**
   * This method is meant to delete domain on local when upload to central fails
   *
   * @return Future which is of the type void
   */
  Future<Void> handleDeleteDomainOnLocal(final String id, final String token) {

    RetryPolicy<Object> retryPolicy =
        retryPolicyBuilder
            .onSuccess(
                listener -> LOGGER.info("Domain deleted from local after upload to central failed"))
            .onFailure(
                failureListener -> {
                  LOGGER.error(
                      "INCONSISTENCY DETECTED : DOMAIN NOT DELETED FROM LOCAL - INCONSISTENT");
                })
            .build();

    Failsafe.with(retryPolicy)
        .getAsyncExecution(
            asyncExecution -> {
              localCat
                  .deleteDomain(id, token)
                  .onSuccess(
                      successHandler -> {
                        asyncExecution.complete();
                      })
                  .onFailure(asyncExecution::recordException);
            });

    return Future.succeededFuture();
  }

  /**
   * This method is meant to restore domain on local when update on central fails
   *
   * @return Future which is of the type void
   */
  Future<Void> handleUploadDomainToLocal(final String id, final String token) {

    RetryPolicy<Object> retryPolicy =
        retryPolicyBuilder
            .onSuccess(listener -> LOGGER.info("Update on local reverted after failure on central"))
            .onFailure(
                failureListener -> {
                  LOGGER.error("INCONSISTENCY DETECTED : DOMAIN NOT RESTORED ON LOCAL");
                })
            .build();

    Failsafe.with(retryPolicy)
        .getAsyncExecution(
            asyncExecution -> {
              centralCat
                  .getDomain(id)
                  .onSuccess(
                      item -> {
                        JsonObject deletedDomain = item.getJsonArray("results").getJsonObject(0);
                        deletedDomain.put("domainId", id);
                        localCat
                            .createDomain(deletedDomain, token)
                            .onSuccess(
                                successHandler -> {
                                  asyncExecution.complete();
                                })
                            .onFailure(asyncExecution::recordException);
                      })
                  .onFailure(asyncExecution::recordException);
            });

    return Future.succeededFuture();
  }

  /**
   * This method is meant to restore domain on local when update on central fails
   *
   * @return Future which is of the type void
   */
  Future<Void> handleUpdateDomainOnLocal(final String id, final String token) {

    RetryPolicy<Object> retryPolicy =
        retryPolicyBuilder
            .onSuccess(listener -> LOGGER.info("Update on local reverted after failure on central"))
            .onFailure(
                failureListener -> {
                  LOGGER.error("INCONSISTENCY DETECTED : DOMAIN NOT RESTORED ON LOCAL");
                })
            .build();

    Failsafe.with(retryPolicy)
        .getAsyncExecution(
            asyncExecution -> {
              centralCat
                  .getDomain(id)
                  .onSuccess(
                      oldItem -> {
                        JsonObject item = oldItem.getJsonArray("results").getJsonObject(0);
                        localCat
                            .updateDomain(id, item, token)
                            .onSuccess(
                                successHandler -> {
                                  asyncExecution.complete();
                                })
                            .onFailure(asyncExecution::recordException);
                      })
                  .onFailure(asyncExecution::recordException);
            });

    return Future.succeededFuture();
  }
}
