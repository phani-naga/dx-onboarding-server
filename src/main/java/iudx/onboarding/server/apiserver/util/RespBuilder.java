package iudx.onboarding.server.apiserver.util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class RespBuilder {
  private JsonObject response = new JsonObject();

  public RespBuilder withType(String type) {
    response.put("type", type);
    return this;
  }

  public RespBuilder withTitle(String title) {
    response.put("title", title);
    return this;
  }

  public RespBuilder withDetail(String detail) {
    response.put("detail", detail);
    return this;
  }

  public RespBuilder withResult(JsonObject result) {
    response.put("results", result);
    return this;
  }

  public JsonObject getJsonResponse() {
    return response;
  }

  public String getResponse() {
    return response.toString();
  }
}
