package iudx.onboarding.server.catalogue.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class KeyCloakImpl implements  KeyCloakClient{
  @Override
  public Future<JsonObject> getToken(String clientId, String clientSecret) {
    return null;
  }
}
