package iudx.onboarding.server.catalogue;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import dev.failsafe.RetryPolicyBuilder;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import iudx.onboarding.server.apiserver.exceptions.DxRuntimeException;
import iudx.onboarding.server.apiserver.util.RespBuilder;
import iudx.onboarding.server.catalogue.service.CentralCatImpl;
import iudx.onboarding.server.catalogue.service.LocalCatImpl;
import iudx.onboarding.server.common.CatalogueType;
import iudx.onboarding.server.token.TokenService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.UnknownHostException;

import static iudx.onboarding.server.common.Constants.ID;
import static iudx.onboarding.server.common.Constants.TOKEN;

public class CatalogueServiceImpl implements CatalogueUtilService {

  private static final Logger LOGGER = LogManager.getLogger(CatalogueServiceImpl.class);
  private final TokenService tokenService;
  private final RetryPolicyBuilder<Object> retryPolicyBuilder;
  public CentralCatImpl centralCat;
  public LocalCatImpl localCat;
  private InconsistencyHandler inconsistencyHandler;

  CatalogueServiceImpl(Vertx vertx, TokenService tokenService, RetryPolicyBuilder<Object> retryPolicyBuilder, JsonObject config) {
    this.tokenService = tokenService;
    this.retryPolicyBuilder = retryPolicyBuilder;
    this.centralCat = new CentralCatImpl(vertx, config);
    this.localCat = new LocalCatImpl(vertx, config);
    this.inconsistencyHandler = new InconsistencyHandler(tokenService, localCat, centralCat, retryPolicyBuilder);
  }

  @Override
  public Future<JsonObject> createItem(JsonObject request, String token, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    if (catalogueType.equals(CatalogueType.CENTRAL)) {
      RetryPolicy<Object> retryPolicy = retryPolicyBuilder
          .onSuccess(successListener -> {
            promise.complete((JsonObject) successListener.getResult());
          })
          .onFailure(listener -> {
            LOGGER.warn("Failed to upload item to central");
            String id = request.getString(ID);
            Future.future(f -> inconsistencyHandler.handleDeleteOnLocal(id, token));
            promise.fail(handleFailure(listener.getException()));
          })
          .build();

      Failsafe.with(retryPolicy)
          .getAsyncExecution(asyncExecution -> {
            tokenService.createToken().compose(adminToken -> {
              return centralCat.createItem(request, adminToken.getString(TOKEN));
            }).onComplete(ar -> {
              if (ar.succeeded()) {
                asyncExecution.recordResult(ar.result());
              } else {
                asyncExecution.recordException(ar.cause());
              }
            });
          });
    } else if (catalogueType.equals(CatalogueType.LOCAL)) {
      localCat.createItem(request, token).onComplete(completeHandler -> {
        if (completeHandler.succeeded()) {
          promise.complete(completeHandler.result());
        } else {
          promise.fail(completeHandler.cause());
        }
      });
    } else {
      promise.fail("Invalid catalogue type");
    }

    return promise.future();
  }

  @Override
  public Future<JsonObject> updateItem(JsonObject request, String token, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    if (catalogueType.equals(CatalogueType.CENTRAL)) {
      RetryPolicy<Object> retryPolicy = retryPolicyBuilder
          .onSuccess(successListener -> {
            promise.complete((JsonObject) successListener.getResult());
          })
          .onFailure(listener -> {
            LOGGER.warn("Failed to update item to central");
            String id = request.getString(ID);
            Future.future(f -> inconsistencyHandler.handleUpdateOnLocal(id, token));
            promise.fail(handleFailure(listener.getException()));
//            promise.fail(new DxRuntimeException(500, listener.getException().getMessage()));
          })
          .build();

      Failsafe.with(retryPolicy)
          .getAsyncExecution(asyncExecution -> {
            tokenService.createToken().compose(adminToken -> {
              return centralCat.updateItem(request, adminToken.getString(TOKEN));
            }).onComplete(ar -> {
              if (ar.succeeded()) {
                asyncExecution.recordResult(ar.result());
              } else {
                asyncExecution.recordException(ar.cause());
              }
            });
          });
    } else if (catalogueType.equals(CatalogueType.LOCAL)) {
      localCat.updateItem(request, token).onComplete(completeHandler -> {
        if (completeHandler.succeeded()) {
          promise.complete(completeHandler.result());
        } else {
          promise.fail(completeHandler.cause());
        }
      });
    } else {
      promise.fail("Invalid catalogue type");
    }

    return promise.future();
  }

  @Override
  public Future<JsonObject> deleteItem(JsonObject request, String token, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    String id = request.getString(ID);
    if (catalogueType.equals(CatalogueType.CENTRAL)) {
      RetryPolicy<Object> retryPolicy = retryPolicyBuilder
          .onSuccess(successListener -> {
            LOGGER.debug("hereh");
            promise.complete((JsonObject) successListener.getResult());
          })
          .onFailure(listener -> {
            LOGGER.warn("Failed to delete item from central");
            Future.future(f -> inconsistencyHandler.handleUploadToLocal(id, token));
            promise.fail(handleFailure(listener.getException()));
          })
          .build();

      Failsafe.with(retryPolicy)
          .getAsyncExecution(asyncExecution -> {
            tokenService.createToken().compose(adminToken -> {
              return centralCat.deleteItem(id, adminToken.getString(TOKEN));
            }).onComplete(ar -> {
              if (ar.succeeded()) {
                asyncExecution.recordResult(ar.result());
              } else {
                asyncExecution.recordException(ar.cause());
              }
            });
          });
    } else if (catalogueType.equals(CatalogueType.LOCAL)) {
      localCat.deleteItem(id, token).onComplete(completeHandler -> {
        if (completeHandler.succeeded()) {
          promise.complete(completeHandler.result());
        } else {
          promise.fail(completeHandler.cause());
        }
      });
    } else {
      promise.fail("Invalid catalogue type");
    }

    return promise.future();
  }

  @Override
  public Future<JsonObject> getItem(String id, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    Future<JsonObject> getFuture;
    if (catalogueType.equals(CatalogueType.CENTRAL)) getFuture = centralCat.getItem(id);
    else if (catalogueType.equals(CatalogueType.LOCAL)) {
      getFuture = localCat.getItem(id);
    } else {
      promise.fail("Invalid catalogue type");
      return promise.future();
    }
    getFuture.onComplete(handler -> {
      if (handler.succeeded()) {
        promise.complete(handler.result());
      } else {
        promise.fail("Request failed");
      }
    });

    return promise.future();
  }

  private String handleFailure(Throwable cause) {

    RespBuilder respBuilder;
    if (cause instanceof DxRuntimeException) {
      respBuilder = new RespBuilder()
          .withType("urn:dx:cat:RuntimeException")
          .withTitle("Dx Runtime Exception")
          .withDetail(cause.getMessage());
    } else if (cause instanceof UnknownHostException) {
      respBuilder = new RespBuilder()
          .withType("urn:dx:cat:InternalServerError")
          .withTitle("Internal Server Error")
          .withDetail(cause.getMessage());
    } else {
      respBuilder = new RespBuilder()
          .withType("urn:dx:cat:InternalServerError")
          .withTitle("Internal Server Error")
          .withDetail(cause.getMessage());
    }
    return respBuilder.getResponse();
  }
}
