package iudx.onboarding.server.catalogue;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
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
  public CentralCatImpl centralCat;
  public LocalCatImpl localCat;

  CatalogueServiceImpl(Vertx vertx, TokenService tokenService, JsonObject config) {
    this.tokenService = tokenService;
    this.centralCat = new CentralCatImpl(vertx, config);
    this.localCat = new LocalCatImpl(vertx, config);
  }

  @Override
  public Future<JsonObject> createItem(JsonObject request, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    String token = request.getString(TOKEN);
    request.remove(TOKEN);
    if (catalogueType.equals(CatalogueType.CENTRAL)) {
      tokenService.createToken().compose(adminToken -> {
        return centralCat.createItem(request, adminToken.getString(TOKEN));
      }).onComplete(completeHandler -> {
        if (completeHandler.succeeded()) {
          promise.complete(completeHandler.result());
        } else {
          promise.fail(completeHandler.cause());
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
  public Future<JsonObject> updateItem(JsonObject request, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    String token = request.getString(TOKEN);
    request.remove(TOKEN);
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
  public Future<JsonObject> deleteItem(JsonObject request, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    Future<JsonObject> deleteFuture;
    String token = request.getString(TOKEN);
    request.remove(TOKEN);
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
