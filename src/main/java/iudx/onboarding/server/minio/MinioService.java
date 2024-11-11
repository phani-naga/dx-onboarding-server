package iudx.onboarding.server.minio;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface MinioService {

  @GenIgnore
  static MinioService createProxy(Vertx vertx, String address) {
    return new MinioServiceVertxEBProxy(vertx, address);
  }

  Future<String> createBucket(String username);
}
