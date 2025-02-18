package iudx.onboarding.server.token;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface TokenService {

  @GenIgnore
  static TokenService createProxy(Vertx vertx, String address) {
    return new TokenServiceVertxEBProxy(vertx, address);
  }

  Future<JsonObject> createToken();

  Future<JsonObject> decodeToken(String token);
}
