package iudx.onboarding.server.catalogue.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static iudx.onboarding.server.common.Constants.TOKEN;

public class LocalCatImpl implements CatalogueService {

  private static final Logger LOGGER = LogManager.getLogger(LocalCatImpl.class);
  static WebClient catWebClient;
  private String catHost;
  private int catPort;
  private String catBasePath;
  private Vertx vertx;

  public LocalCatImpl(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.catHost = config.getString("localCatServerHost");
    this.catPort = config.getInteger("localCatServerPort");
    this.catBasePath = config.getString("dxCatalogueBasePath");

    WebClientOptions options =
        new WebClientOptions().setTrustAll(true).setVerifyHost(false).setSsl(true);
    if (catWebClient == null) {
      catWebClient = WebClient.create(vertx, options);
    }

  }

  @Override
  public Future<JsonObject> createItem(JsonObject request, String token) {
    Promise<JsonObject> promise = Promise.promise();
    request.remove(TOKEN);
    catWebClient
        .post(catPort, catHost, catBasePath.concat("/item"))
        .putHeader("token", token)
        .putHeader("Content-Type", "application/json")
        .sendJsonObject(request, httpResponseAsyncResult -> {
          if (httpResponseAsyncResult.succeeded() && httpResponseAsyncResult.result().statusCode() == 201) {
            LOGGER.info("request successful");
            JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
            promise.complete(response);
          } else {
            LOGGER.info("Failure {}", httpResponseAsyncResult.result().body().toString());
            Throwable cause = httpResponseAsyncResult.cause();
            if (cause != null) {
              promise.fail(cause);
            } else {
              promise.fail(httpResponseAsyncResult.result().bodyAsString());
            }
            ; // Fail the promise with the failure cause
          }
        });
    return promise.future();
  }

  @Override
  public Future<JsonObject> updateItem(JsonObject request, String token) {
    Promise<JsonObject> promise = Promise.promise();
    request.remove(TOKEN);
    catWebClient
        .put(catPort, catHost, catBasePath.concat("/item"))
        .putHeader("token", token)
        .putHeader("Content-Type", "application/json")
        .sendJsonObject(request, httpResponseAsyncResult -> {
          if (httpResponseAsyncResult.succeeded() && httpResponseAsyncResult.result().statusCode() == 200) {
            LOGGER.info("request successful");
            JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
            promise.complete(response);
          } else {
            LOGGER.info("Failure {}", httpResponseAsyncResult.result().body().toString());
            Throwable cause = httpResponseAsyncResult.cause();
            if (cause != null) {
              promise.fail(cause);
            } else {
              promise.fail(httpResponseAsyncResult.result().bodyAsString());
            }
            ; // Fail the promise with the failure cause
          }
        });
    return promise.future();
  }

  @Override
  public Future<JsonObject> deleteItem(String id, String token) {
    Promise<JsonObject> promise = Promise.promise();
    catWebClient
        .delete(catPort, catHost, catBasePath.concat("/item"))
        .putHeader("token", token)
        .putHeader("Content-Type", "application/json")
        .addQueryParam("id", id)
        .send(httpResponseAsyncResult -> {
          if (httpResponseAsyncResult.succeeded() && httpResponseAsyncResult.result().statusCode() == 200) {
            LOGGER.info("local request successful" + httpResponseAsyncResult.result().bodyAsJsonObject());
            JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
            promise.complete(response);
          } else {
            LOGGER.info("Failure {}", httpResponseAsyncResult.result().body().toString());
            Throwable cause = httpResponseAsyncResult.cause();
            if (cause != null) {
              promise.fail(cause);
            } else {
              promise.fail(httpResponseAsyncResult.result().bodyAsString());
            }
            ; // Fail the promise with the failure cause
          }
        });
    return promise.future(); // Return the future outside the callback function
  }

  @Override
  public Future<JsonObject> getItem(String id) {
    Promise<JsonObject> promise = Promise.promise();
    catWebClient
        .get(catPort, catHost, catBasePath.concat("/item"))
        .addQueryParam("id", id)
        .send(httpResponseAsyncResult -> {
          if (httpResponseAsyncResult.succeeded() && httpResponseAsyncResult.result().statusCode() == 200) {
            JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
            promise.complete(response);
          } else {
            LOGGER.info("Failure {}", httpResponseAsyncResult.result().body().toString());
            Throwable cause = httpResponseAsyncResult.cause();
            if (cause != null) {
              promise.fail(cause);
            } else {
              promise.fail(httpResponseAsyncResult.result().bodyAsString());
            }
            ; // Fail the promise with the failure cause
          }
        });
    return promise.future();
  }
}
