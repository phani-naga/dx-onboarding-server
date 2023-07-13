package iudx.onboarding.server.catalogue.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.core.json.JsonObject;
import iudx.onboarding.server.token.TokenServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocalCatImpl implements CatalogueService {

  private static final Logger LOGGER = LogManager.getLogger(LocalCatImpl.class);
  private String catHost;
  private int catPort;
  private String catBasePath;
  static WebClient catWebClient;
  private Vertx vertx;
  TokenServiceImpl tokenService;

  public LocalCatImpl(Vertx vertx, JsonObject config) {
    LOGGER.debug("config : {}", config);
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
    catWebClient
            .post(catPort, catHost, "/iudx/cat/v1/item")
            .putHeader("Token",token)
            .putHeader("Content-Type","application/json")
            .sendJsonObject(request, httpResponseAsyncResult -> {
              if (httpResponseAsyncResult.succeeded()) {
                LOGGER.info("request successful");
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
            .putHeader("Token",token)
            .putHeader("Content-Type","application/json")
            .sendJsonObject(request, httpResponseAsyncResult -> {
              if (httpResponseAsyncResult.succeeded()) {
                LOGGER.info("request successful");
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
  public Future<JsonObject> deleteItem(JsonObject request) {
    Promise<JsonObject> promise = Promise.promise();
    Future<String> typeFuture = typeOfItem(request.getString("id"));
    typeFuture.onComplete( handler -> {
      String type = handler.result();
      String token ="";
      if(type.equals("iudx:ResourceServer")) {
        token = request.getString("Admin-Token");
      } else {
        token = request.getString("Provider-Token");
      }
      catWebClient
              .delete(catPort, catHost, "/iudx/cat/v1/item")
              .putHeader("Token", token)
              .putHeader("Content-Type", "application/json")
              .addQueryParam("id", request.getString("id"))
              .send(httpResponseAsyncResult -> {
                if (httpResponseAsyncResult.succeeded()) {
                  LOGGER.info("request successful");
                  JsonObject response = new JsonObject().put("statusCode", httpResponseAsyncResult.result().statusCode())
                          .put("results", httpResponseAsyncResult.result().body().toJsonObject());
                  promise.complete(response);
                } else {
                  LOGGER.info("Failure" + httpResponseAsyncResult);
                  Throwable cause = httpResponseAsyncResult.cause();
                  promise.fail(cause); // Fail the promise with the failure cause
                }
              });

    }).onFailure(handler -> {
      Throwable cause = handler.getCause();
      promise.fail(cause);
    });
    return promise.future(); // Return the future outside the callback function
  }

  private Future<String> typeOfItem(String id) {
    Promise<String> promise = Promise.promise();
    catWebClient
            .get(catPort, catHost, "/iudx/cat/v1/item")
            .addQueryParam("id", id)
            .send(httpResponseAsyncResult -> {
              if (httpResponseAsyncResult.succeeded()) {
                LOGGER.info("request successful");
                String type = httpResponseAsyncResult.result().body()
                        .toJsonObject().getJsonArray("results")
                        .getJsonObject(0)
                        .getJsonArray("type")
                        .getString(0);
                promise.complete(type);
              } else {
                LOGGER.info("Failure" + httpResponseAsyncResult);
                Throwable cause = httpResponseAsyncResult.cause();
                promise.fail(cause); // Fail the promise with the failure cause
              }
            });
    return promise.future();
  }


}
