package iudx.onboarding.server.minio;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Service interface for managing MinIO operations such as bucket creation and policy attachment.
 */
@ProxyGen
@VertxGen
public interface MinioService {

  /**
   * Creates a proxy instance of MinioService for communication over the Vert.x event bus.
   *
   * @param vertx   the Vert.x instance
   * @param address the event bus address
   * @return a proxy instance of MinioService
   */
  @GenIgnore
  static MinioService createProxy(Vertx vertx, String address) {
    return new MinioServiceVertxEBProxy(vertx, address);
  }

  /**
   * Creates a new bucket for the specified username if it does not already exist.
   *
   * @param username the name of the user for whom the bucket is created
   * @return a Future containing the URL of the bucket
   */
  Future<String> createBucket(String username);

  /**
   * Attaches a bucket to a name policy using the MinIO policy API.
   *
   * @param policyRequest the policy request details in JSON format
   * @return a Future indicating success or failure
   */
  Future<Void> attachBucketToNamePolicy(JsonObject policyRequest);
}
