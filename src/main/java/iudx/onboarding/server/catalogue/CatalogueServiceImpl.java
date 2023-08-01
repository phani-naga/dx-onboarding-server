package iudx.onboarding.server.catalogue;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import iudx.onboarding.server.catalogue.service.CentralCatImpl;
import iudx.onboarding.server.catalogue.service.LocalCatImpl;
import iudx.onboarding.server.common.CatalogueType;
import iudx.onboarding.server.token.TokenService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static iudx.onboarding.server.common.Constants.ID;
import static iudx.onboarding.server.common.Constants.TOKEN;

public class CatalogueServiceImpl implements CatalogueUtilService {

  private static final Logger LOGGER = LogManager.getLogger(CatalogueServiceImpl.class);
  private final TokenService tokenService;
  private final CircuitBreaker circuitBreaker;
  public CentralCatImpl centralCat;
  public LocalCatImpl localCat;
  private InconsistencyHandler inconsistencyHandler;

  CatalogueServiceImpl(Vertx vertx, TokenService tokenService, CircuitBreaker circuitBreaker, JsonObject config) {
    this.tokenService = tokenService;
    this.circuitBreaker = circuitBreaker;
    this.centralCat = new CentralCatImpl(vertx, config);
    this.localCat = new LocalCatImpl(vertx, config);
    this.inconsistencyHandler = new InconsistencyHandler(tokenService, localCat);
  }

  @Override
  public Future<JsonObject> createItem(JsonObject request, String token, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    if (catalogueType.equals(CatalogueType.CENTRAL)) {
      circuitBreaker.<JsonObject>execute(circuitBreakerHandler -> {
        tokenService.createToken().compose(adminToken -> {
          return centralCat.createItem(request, adminToken.getString(TOKEN));
        }).onComplete(circuitBreakerHandler);
      }).onComplete(ar -> {
        if (ar.succeeded()) {
          promise.complete(ar.result());
        } else {
          LOGGER.warn("Failed to upload item to central");
          LOGGER.debug(token);
          // TODO: delete item from local
          String id = request.getString(ID);
          Future.future(f -> inconsistencyHandler.handleDeleteOnLocal(localCat, id, token));
          promise.fail(ar.cause().getMessage());
        }
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
      tokenService.createToken().compose(adminToken -> {
        return centralCat.updateItem(request, adminToken.getString(TOKEN));
      }).onComplete(completeHandler -> {
        if (completeHandler.succeeded()) {
          promise.complete(completeHandler.result());
        } else {
          promise.fail(completeHandler.cause());
        }
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
      tokenService.createToken().compose(adminToken -> {
        return centralCat.deleteItem(id, adminToken.getString(TOKEN));
      }).onComplete(completeHandler -> {
        if (completeHandler.succeeded()) {
          promise.complete(completeHandler.result());
        } else {
          promise.fail(completeHandler.cause());
        }
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
}
