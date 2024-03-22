package iudx.onboarding.server.catalogue.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface CatalogueService {
  Future<JsonObject> createItem(final JsonObject request, String token);

  Future<JsonObject> updateItem(final JsonObject request, String token);

  Future<JsonObject> deleteItem(final String id, String token);

  Future<JsonObject> getItem(final String id);
  Future<JsonObject> createInstance(final JsonObject request, final String path, String token);
  Future<JsonObject> deleteInstance(final String id, final String path, String token);
  Future<JsonObject> getInstance(final String id, final String path);
  Future<JsonObject> updateInstance(String id, final JsonObject request, String token);

  Future<JsonObject> createDomain(final JsonObject request, String token);

  Future<JsonObject> deleteDomain(final String id, String token);

  Future<JsonObject> getDomain(final String id);

  Future<JsonObject> updateDomain(String id, final JsonObject request, String token);

}
