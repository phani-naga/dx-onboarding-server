package iudx.onboarding.server.catalogue.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CentralCatImpl implements CatalogueService {

  private static final Logger LOGGER = LogManager.getLogger(CentralCatImpl.class);
  private String catHost;
  private int catPort;
  private String catBasePath;
  static WebClient catWebClient;
  private Vertx vertx;
  private KeyCloakClient keyCloakClient;

  public CentralCatImpl(Vertx vertx, JsonObject config, KeyCloakClient keyCloakClient) {
    LOGGER.debug("config : {}", config);
    this.vertx = vertx;
    this.keyCloakClient = keyCloakClient;
    this.catHost = config.getString("centralCatServerHost");
    this.catPort = config.getInteger("centralCatServerPort");
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
    catWebClient
            .post(catPort, catHost, "/iudx/cat/v1/item")
            .putHeader("token", token)
            .sendJsonObject(request, httpResponseAsyncResult -> {
              if (httpResponseAsyncResult.succeeded()) {
                LOGGER.info("central request successful");
                JsonObject response = new JsonObject().put("statusCode",httpResponseAsyncResult.result().statusCode())
                        .put("results",httpResponseAsyncResult.result().body().toJsonObject());
                promise.complete(response);
              } else {
                LOGGER.info("Failure"+httpResponseAsyncResult);
                Throwable cause = httpResponseAsyncResult.cause();
                promise.fail(cause); // Fail the promise with the failure cause
              }
            });
    return promise.future();
  }



  @Override
  public Future<JsonObject> updateItem(JsonObject request, String token) {
    Promise<JsonObject> promise = Promise.promise();
    catWebClient
            .put(catPort, catHost, "/iudx/cat/v1/item")
            .putHeader("token", token)
            .putHeader("Content-Type","application/json")
            .sendJsonObject(request, httpResponseAsyncResult -> {
              if (httpResponseAsyncResult.succeeded()) {
                LOGGER.info("central request successful");
                JsonObject response = new JsonObject().put("statusCode",httpResponseAsyncResult.result().statusCode())
                        .put("results",httpResponseAsyncResult.result().body().toJsonObject());
                promise.complete(response);
              } else {
                LOGGER.info("Failure"+httpResponseAsyncResult);
                Throwable cause = httpResponseAsyncResult.cause();
                promise.fail(cause); // Fail the promise with the failure cause
              }
            });
    return promise.future();
  }

  @Override
  public Future<JsonObject> deleteItem(String id, String token) {
    Promise<JsonObject> promise = Promise.promise();
    catWebClient
            .delete(catPort, catHost, "/iudx/cat/v1/item")
            .putHeader("token", token)
            .putHeader("Content-Type","application/json")
            .addQueryParam("id", id)
            .send(httpResponseAsyncResult -> {
              if (httpResponseAsyncResult.succeeded()) {
                LOGGER.info("central request successful");
                JsonObject response = new JsonObject().put("statusCode",httpResponseAsyncResult.result().statusCode())
                        .put("results",httpResponseAsyncResult.result().body().toJsonObject());
                promise.complete(response);
              } else {
                LOGGER.info("Failure"+httpResponseAsyncResult);
                Throwable cause = httpResponseAsyncResult.cause();
                promise.fail(cause); // Fail the promise with the failure cause
              }
            });
    return promise.future();
  }

  @Override
  public Future<JsonObject> getItem(String id) {
    Promise<JsonObject> promise = Promise.promise();
    catWebClient
            .get(catPort, catHost, "/iudx/cat/v1/item")
            .addQueryParam("id", id)
            .send(httpResponseAsyncResult -> {
              if (httpResponseAsyncResult.succeeded()) {
                JsonObject response = new JsonObject().put("statusCode",httpResponseAsyncResult.result().statusCode())
                        .put("results",httpResponseAsyncResult.result().body().toJsonObject());
                promise.complete(response);
              } else {
                LOGGER.info("Failure"+httpResponseAsyncResult);
                Throwable cause = httpResponseAsyncResult.cause();
                promise.fail(cause); // Fail the promise with the failure cause
              }
            });
    return promise.future();
  }
}
