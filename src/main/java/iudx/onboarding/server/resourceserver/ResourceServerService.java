package iudx.onboarding.server.resourceserver;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface ResourceServerService {

  Future<JsonObject> createAdapter(String id, String token);

  Future<JsonObject> deleteAdapter(String id, String token);

  @GenIgnore
  static ResourceServerService createProxy(Vertx vertx, String address) {
    return new ResourceServerServiceVertxEBProxy(vertx, address);
  }



}
