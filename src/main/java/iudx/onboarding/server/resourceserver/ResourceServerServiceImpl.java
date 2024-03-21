package iudx.onboarding.server.resourceserver;

import static iudx.onboarding.server.apiserver.util.Constants.RESULTS;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import dev.failsafe.RetryPolicyBuilder;
import io.netty.channel.ConnectTimeoutException;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import iudx.onboarding.server.apiserver.exceptions.DxRuntimeException;
import iudx.onboarding.server.catalogue.service.CentralCatImpl;
import iudx.onboarding.server.catalogue.service.LocalCatImpl;
import iudx.onboarding.server.common.InconsistencyHandler;
import iudx.onboarding.server.ingestion.IngestionService;
import iudx.onboarding.server.token.TokenService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResourceServerServiceImpl implements ResourceServerService {

  private static final Logger LOGGER = LogManager.getLogger(ResourceServerServiceImpl.class);
  private final RetryPolicyBuilder<Object> retryPolicyBuilder;
  private CentralCatImpl centralCat;
  private LocalCatImpl localCat;
  private InconsistencyHandler inconsistencyHandler;
  private IngestionService ingestionService;

  ResourceServerServiceImpl(Vertx vertx, TokenService tokenService, RetryPolicyBuilder<Object> retryPolicyBuilder, IngestionService ingestionService, JsonObject config) {
    this.retryPolicyBuilder = retryPolicyBuilder;
    this.centralCat = new CentralCatImpl(vertx, config);
    this.localCat = new LocalCatImpl(vertx, config);
    this.inconsistencyHandler = new InconsistencyHandler(tokenService, localCat, centralCat, retryPolicyBuilder);
    this.ingestionService = ingestionService;

  }

  @Override
  public Future<JsonObject> createAdapter(String id, String token) {
    Promise<JsonObject> promise = Promise.promise();

    RetryPolicy<Object> retryPolicy = retryPolicyBuilder
            .onSuccess(successListener -> {
              promise.complete((JsonObject) successListener.getResult());
            })
            .onFailure(listener -> {
              LOGGER.warn("Failed to create adapter for resource group");
              LOGGER.debug(listener.getException());
              LOGGER.debug(listener.getResult());
              LOGGER.debug(listener.getException().getMessage());
              Future.future(f -> inconsistencyHandler.handleDeleteOfResourceGroup(id, token));
              promise.fail(listener.getException().getMessage());
            })
            .build();

    Failsafe.with(retryPolicy)
            .getAsyncExecution(asyncExecution -> {
              localCat.getRelatedEntity(id, "resourceServer", new JsonArray().add("resourceServerRegURL"))
                      .compose(rsUrlResult -> {
                        String resourceServerUrl = rsUrlResult.getJsonArray(RESULTS).getJsonObject(0).getString("resourceServerRegURL");
                        return Future.succeededFuture(resourceServerUrl);
                      }).compose(rsUrl -> {
                        return ingestionService.registerAdapter(rsUrl, id, token);
                      }).onComplete(ar -> {
                        if (ar.succeeded()) {
                          asyncExecution.recordResult(ar.result());
                        } else {
                          LOGGER.debug(ar.cause().getMessage());
                          if (ar.cause() instanceof ConnectTimeoutException) {
                            asyncExecution.recordException(ar.cause());
                          } else {
                            asyncExecution.recordException(new DxRuntimeException(400, ar.cause().getMessage()));
                          }
                        }
                      });
            });
    return promise.future();
  }

  //delete adapter added


  @Override
  public Future<JsonObject> deleteAdapter(String id, String token) {
    Promise<JsonObject> promise = Promise.promise();

    RetryPolicy<Object> retryPolicy = retryPolicyBuilder
            .onSuccess(successListener -> {
              promise.complete();
            })
            .onFailure(listener -> {
              LOGGER.warn("Failed to delete adapter for resource group");
              LOGGER.debug(listener.getException());
              LOGGER.debug(listener.getResult());
              LOGGER.debug(listener.getException().getMessage());
              //Future.future(f -> inconsistencyHandler.handleDeleteOfResourceGroup(id, token));
              promise.fail(listener.getException().getMessage());
            })
            .build();

    Failsafe.with(retryPolicy)
            .getAsyncExecution(asyncExecution -> {
              localCat.getRelatedEntity(id, "resourceServer", new JsonArray().add("resourceServerRegURL"))
                      .compose(rsUrlResult -> {
                        String resourceServerUrl = rsUrlResult.getJsonArray(RESULTS).getJsonObject(0).getString("resourceServerRegURL");
                        return Future.succeededFuture(resourceServerUrl);
                      }).compose(rsUrl -> {
                        return ingestionService.unregisteredAdapter(rsUrl, id, token);
                      })
                      .onComplete(ar -> {
                        if (ar.succeeded()) {
                          asyncExecution.recordResult(ar.result());
                        } else {
                          LOGGER.debug(ar.cause().getMessage());
                          if (ar.cause() instanceof ConnectTimeoutException) {
                            asyncExecution.recordException(ar.cause());
                          } else {
                            asyncExecution.recordException(new DxRuntimeException(400, ar.cause().getMessage()));
                          }
                        }
                      });
            });
    return promise.future();
  }

}
