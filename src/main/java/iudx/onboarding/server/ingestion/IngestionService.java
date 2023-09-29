package iudx.onboarding.server.ingestion;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface IngestionService {

  Future<JsonObject> registerAdapter(final String resourceServerUrl, final String id, final String token);

  @GenIgnore
  static IngestionService createProxy(Vertx vertx, String address) {
    return new IngestionServiceVertxEBProxy(vertx, address);
  }
}
