package iudx.onboarding.server.catalogue;

import io.vertx.core.Future;
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
  public Future<JsonObject> createItem(JsonObject request, CatalogueType catalogueType) {
    if(catalogueType.equals(CatalogueType.CENTRAL))
      centralCat.createItem(request);
    else if(catalogueType.equals(CatalogueType.LOCAL))
      localCat.createItem(request);
    return null;
  }

  @Override
  public Future<JsonObject> updateItem(JsonObject request, CatalogueType catalogueType) {
    if(catalogueType.equals(CatalogueType.CENTRAL))
      centralCat.updateItem(request);
    else if(catalogueType.equals(CatalogueType.LOCAL))
      localCat.updateItem(request);
    return null;
  }

  @Override
  public Future<JsonObject> deleteItem(JsonObject request, CatalogueType catalogueType) {
    if(catalogueType.equals(CatalogueType.CENTRAL))
      centralCat.deleteItem(request);
    else if(catalogueType.equals(CatalogueType.LOCAL))
      localCat.deleteItem(request);
    return null;
  }
}
