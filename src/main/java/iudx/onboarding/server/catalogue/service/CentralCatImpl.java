package iudx.onboarding.server.catalogue.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import iudx.onboarding.server.apiserver.exceptions.DxRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CentralCatImpl implements CatalogueService {

  private static final Logger LOGGER = LogManager.getLogger(CentralCatImpl.class);
  static WebClient catWebClient;
  private String catHost;
  private int catPort;
  private String catBasePath;
  private Vertx vertx;

  public CentralCatImpl(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.catHost = config.getString("centralCatServerHost");
    this.catPort = config.getInteger("centralCatServerPort");
    this.catBasePath = config.getString("dxCatalogueBasePath");
    this.catWebClient = client;

    WebClientOptions options =
        new WebClientOptions().setTrustAll(true).setVerifyHost(false).setSsl(true);
    if (catWebClient == null) {
      catWebClient = WebClient.create(vertx, options);
    }

  }

  @Override
  public Future<JsonObject> createItem(JsonObject request, String token) {
    Promise<JsonObject> promise = Promise.promise();
    catWebClient
        .post(catPort, catHost, catBasePath.concat("/item"))
        .putHeader("token", token)
        .putHeader("Content-Type", "application/json")
        .sendJsonObject(request, httpResponseAsyncResult -> {
          if (httpResponseAsyncResult.succeeded() && httpResponseAsyncResult.result().statusCode() == 201) {
            LOGGER.info("central request successful");
            JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
            promise.complete(response);
          } else {
            Throwable cause = httpResponseAsyncResult.cause();
            if (cause != null) {
              LOGGER.debug(cause.getMessage());
              promise.fail(cause);
            } else {
              promise.fail(new DxRuntimeException(400,httpResponseAsyncResult.result().bodyAsString()));
            }
            ; // Fail the promise with the failure cause
          }
        });
    return promise.future();
  }


  @Override
  public Future<JsonObject> updateItem(JsonObject request, String token) {
    Promise<JsonObject> promise = Promise.promise();
    catWebClient
        .put(catPort, catHost, catBasePath.concat("/item"))
        .putHeader("token", token)
        .putHeader("Content-Type", "application/json")
        .sendJsonObject(request, httpResponseAsyncResult -> {
          if (httpResponseAsyncResult.succeeded() && httpResponseAsyncResult.result().statusCode() == 200) {
            LOGGER.info("central request successful");
            JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
            promise.complete(response);
          } else {
            LOGGER.info("Failure {}", httpResponseAsyncResult.result().body().toString());
            Throwable cause = httpResponseAsyncResult.cause();
            if (cause != null) {
              promise.fail(cause);
            } else {
              promise.fail(new DxRuntimeException(400,httpResponseAsyncResult.result().bodyAsString()));
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
            LOGGER.info("central request successful" + httpResponseAsyncResult.result().bodyAsJsonObject());
            JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
            promise.complete(response);
          } else {
            Throwable cause = httpResponseAsyncResult.cause();
            if (cause != null) {
              LOGGER.debug(cause.getMessage());
              promise.fail(cause);
            } else {
              promise.fail(new DxRuntimeException(400,httpResponseAsyncResult.result().bodyAsString()));
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
