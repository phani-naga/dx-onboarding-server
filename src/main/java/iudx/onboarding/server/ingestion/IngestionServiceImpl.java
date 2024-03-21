package iudx.onboarding.server.ingestion;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IngestionServiceImpl implements IngestionService {

  private static final Logger LOGGER = LogManager.getLogger(IngestionServiceImpl.class);
  public static WebClient rsWebClient;
  private int rsPort;
  private String rsBasePath;
  private Vertx vertx;

  public IngestionServiceImpl(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.rsPort = config.getInteger("resourceServerPort"); // If above true, port info??
    this.rsBasePath = config.getString("resourceServerBasePath"); // will this always be `/ngsi-ld/v1` ?

    WebClientOptions options =
            new WebClientOptions().setTrustAll(true).setVerifyHost(false).setSsl(true);
    if (rsWebClient == null) {
      rsWebClient = WebClient.create(vertx, options);
    }
  }

  @Override
  public Future<JsonObject> registerAdapter(String resourceServerUrl, String id, String token) {
    Promise<JsonObject> promise = Promise.promise();

    JsonObject ingestionRequestBody = new JsonObject()
            .put("entities", new JsonArray().add(id));

    rsWebClient
            .post(rsPort, resourceServerUrl, rsBasePath.concat("/ingestion"))
            .putHeader("token", token)
            .putHeader("Content-Type", "application/json")
            .sendJsonObject(ingestionRequestBody, responseHandler -> {
              if (responseHandler.succeeded() && responseHandler.result().statusCode() == 201) {
                JsonObject result = responseHandler.result().body().toJsonObject().getJsonArray("results").getJsonObject(0);
                promise.complete(result);
              } else {
                Throwable cause = responseHandler.cause();
                if (cause != null) {
                  LOGGER.debug(cause.getClass());
                  promise.fail(cause);
                } else {
                  promise.fail(responseHandler.result().bodyAsString());
                }
              }
            });
    return promise.future();
  }

  @Override
  public Future<JsonObject> unregisteredAdapter(String resourceServerUrl, String id, String token) {
    Promise<JsonObject> promise = Promise.promise();

    rsWebClient
      .delete(rsPort, resourceServerUrl, rsBasePath.concat("/ingestion/" + id))
      .putHeader("token", token)
      .send()
      .onSuccess(response -> {
        if (response.statusCode() == 200) {
          promise.complete(new JsonObject());
        } else {
          promise.fail(response.bodyAsString());
        }
      })
            .onFailure(promise::fail);

    return promise.future();
  }

}
