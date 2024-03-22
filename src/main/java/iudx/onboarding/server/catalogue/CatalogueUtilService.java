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
  Future<JsonObject> createItem(final JsonObject request, final String token, CatalogueType catalogueType);

  Future<JsonObject> updateItem(final JsonObject request, final String token, CatalogueType catalogueType);

  Future<JsonObject> deleteItem(final JsonObject request, final String token, CatalogueType catalogueType);

  Future<JsonObject> getItem(final String request, CatalogueType catalogueType);
  Future<JsonObject> createInstance(final String path, final JsonObject request, final String token, CatalogueType catalogueType);
  Future<JsonObject> deleteInstance(final String path, final JsonObject request, final String token, CatalogueType catalogueType);
  Future<JsonObject> updateInstance(String id, final JsonObject request, final String token, CatalogueType catalogueType);
  Future<JsonObject> getInstance(final String request, final String path, CatalogueType catalogueType);
  Future<JsonObject> createDomain(final JsonObject request, final String token, CatalogueType catalogueType);

  Future<JsonObject> deleteDomain(final JsonObject request, final String token, CatalogueType catalogueType);

  Future<JsonObject> updateDomain(String id, final JsonObject request, final String token, CatalogueType catalogueType);

  Future<JsonObject> getDomain(final String request, CatalogueType catalogueType);

  @GenIgnore
  static CatalogueUtilService createProxy(Vertx vertx, String address) {
    return new CatalogueUtilServiceVertxEBProxy(vertx, address);
  }
}
