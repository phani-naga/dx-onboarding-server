package iudx.onboarding.server.catalogue.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
@VertxGen
@ProxyGen
public interface KeyCloakClient {

  Future<JsonObject> getToken(String clientId, String clientSecret);
}
