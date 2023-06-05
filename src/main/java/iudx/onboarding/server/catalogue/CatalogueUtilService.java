package iudx.onboarding.server.catalogue;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import iudx.onboarding.server.common.CatalogueType;

@VertxGen
@ProxyGen
public interface CatalogueUtilService {
  Future<JsonObject> createItem(final JsonObject request, CatalogueType catalogueType);
  Future<JsonObject> updateItem(final JsonObject request, CatalogueType catalogueType);
  Future<JsonObject> deleteItem(final JsonObject request, CatalogueType catalogueType);
  @GenIgnore
  static CatalogueUtilService createProxy(Vertx vertx, String address) {
    return new CatalogueUtilServiceVertxEBProxy(vertx, address);
  }
}
