package iudx.onboarding.server.catalogue;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import iudx.onboarding.server.catalogue.service.CentralCatImpl;
import iudx.onboarding.server.catalogue.service.KeyCloakClient;
import iudx.onboarding.server.catalogue.service.KeyCloakImpl;
import iudx.onboarding.server.catalogue.service.LocalCatImpl;
import iudx.onboarding.server.common.CatalogueType;

public class CatalogueServiceImpl implements CatalogueUtilService{

  public CentralCatImpl centralCat;
  public LocalCatImpl localCat;

  private KeyCloakClient keyCloakClient;

  CatalogueServiceImpl(Vertx vertx,JsonObject config){
    this.keyCloakClient = new KeyCloakImpl();
    this.centralCat = new CentralCatImpl(vertx,config, keyCloakClient);
    this.localCat = new LocalCatImpl(vertx,config);
  }
  @Override
  public Future<JsonObject> createItem(JsonObject request, CatalogueType catalogueType, String token) {
    Promise<JsonObject> promise = Promise.promise();
    Future<JsonObject> createFuture;
    if (catalogueType.equals(CatalogueType.CENTRAL)) {
      createFuture = centralCat.createItem(request, token);
    } else if (catalogueType.equals(CatalogueType.LOCAL)) {
      createFuture = localCat.createItem(request, token);
    } else {
      promise.fail("Invalid catalogue type");
      return promise.future();
    }

    createFuture.onComplete(handler -> {
      if (handler.succeeded()) {
        promise.complete(handler.result());
      } else {
        promise.fail("Request failed");
      }
    });

    return promise.future();
  }

  @Override
  public Future<JsonObject> updateItem(JsonObject request, CatalogueType catalogueType, String token) {
    Promise<JsonObject> promise = Promise.promise();
    Future<JsonObject> updateFuture;
    if(catalogueType.equals(CatalogueType.CENTRAL))
      updateFuture = centralCat.updateItem(request, token);
    else if(catalogueType.equals(CatalogueType.LOCAL))
      updateFuture = localCat.updateItem(request, token);
    else {
      promise.fail("Invalid catalogue type");
      return promise.future();
    }
    updateFuture.onComplete(handler -> {
      if (handler.succeeded()) {
        promise.complete(handler.result());
      } else {
        promise.fail("Request failed");
      }
    });

    return promise.future();
  }

  @Override
  public Future<JsonObject> deleteItem(String id, CatalogueType catalogueType, String token) {
    Promise<JsonObject> promise = Promise.promise();
    Future<JsonObject> deleteFuture;
    if(catalogueType.equals(CatalogueType.CENTRAL))
      deleteFuture = centralCat.deleteItem(id, token);
    else if(catalogueType.equals(CatalogueType.LOCAL)) {
      deleteFuture = localCat.deleteItem(id, token);
    } else {
      promise.fail("Invalid catalogue type");
      return promise.future();
    }
    deleteFuture.onComplete(handler -> {
      if (handler.succeeded()) {
        promise.complete(handler.result());
      } else {
        promise.fail("Request failed");
      }
    });

    return promise.future();
  }

  @Override
  public Future<JsonObject> getItem(String id, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    Future<JsonObject> getFuture;
    if(catalogueType.equals(CatalogueType.CENTRAL))
      getFuture = centralCat.getItem(id);
    else if(catalogueType.equals(CatalogueType.LOCAL)) {
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
