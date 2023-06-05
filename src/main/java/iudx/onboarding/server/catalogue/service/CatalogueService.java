package iudx.onboarding.server.catalogue.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import iudx.onboarding.server.common.CatalogueType;

@VertxGen
@ProxyGen
public interface CatalogueService {
  Future<JsonObject> createItem(final JsonObject request);
  Future<JsonObject> updateItem(final JsonObject request);
  Future<JsonObject> deleteItem(final JsonObject request);

}
