package iudx.onboarding.server.catalogue.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static iudx.onboarding.server.common.Constants.ID;

public class CentralCatImpl implements CatalogueService {

  private static final Logger LOGGER = LogManager.getLogger(CentralCatImpl.class);
  public static WebClient catWebClient;
  private String catHost;
  private int catPort;
  private String catBasePath;
  private Vertx vertx;

  public CentralCatImpl(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
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
              promise.fail(httpResponseAsyncResult.result().bodyAsString());
            }
            // Fail the promise with the failure cause
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
              promise.fail(httpResponseAsyncResult.result().bodyAsString());
            }
            // Fail the promise with the failure cause
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
              promise.fail(httpResponseAsyncResult.result().bodyAsString());
            }
            // Fail the promise with the failure cause
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
            Throwable cause = httpResponseAsyncResult.cause();
            if (cause != null) {
              promise.fail(cause);
            } else {
              promise.fail(httpResponseAsyncResult.result().bodyAsString());
            }
            // Fail the promise with the failure cause
          }
        });
    return promise.future();
  }

  @Override
  public Future<JsonObject> createInstance(JsonObject request, String path, String token) {
    Promise<JsonObject> promise = Promise.promise();
    catWebClient
        .post(catPort, catHost, catBasePath.concat(path.concat("/instance")))
        .setQueryParam(ID, request.getString(ID))
        .putHeader("token", token)
        .putHeader("Content-Type", "application/json")
        .sendJsonObject(
            request,
            httpResponseAsyncResult -> {
              if (httpResponseAsyncResult.succeeded()
                  && httpResponseAsyncResult.result().statusCode() == 201) {
                LOGGER.info("central request successful");
                JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
                promise.complete(response);
              } else {
                Throwable cause = httpResponseAsyncResult.cause();
                if (cause != null) {
                  LOGGER.debug(cause.getMessage());
                  promise.fail(cause);
                } else {
                  LOGGER.error(httpResponseAsyncResult.result().body());
                  promise.fail(httpResponseAsyncResult.result().bodyAsString());
                }
              }
            });
    return promise.future();
  }


  @Override
  public Future<JsonObject> getInstance(String id, String path) {
    Promise<JsonObject> promise = Promise.promise();
    catWebClient
        .get(catPort, catHost, catBasePath.concat(path.concat("/instance")))
        .addQueryParam(ID, id)
        .send(
            httpResponseAsyncResult -> {
              if (httpResponseAsyncResult.succeeded()
                  && httpResponseAsyncResult.result().statusCode() == 200) {
                JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
                promise.complete(response);
              } else {
                Throwable cause = httpResponseAsyncResult.cause();
                if (cause != null) {
                  promise.fail(cause);
                } else {
                  promise.fail(httpResponseAsyncResult.result().bodyAsString());
                }
              }
            });
    return promise.future();
  }

  @Override
  public Future<JsonObject> updateInstance(String id, JsonObject request, String token) {
    Promise<JsonObject> promise = Promise.promise();
    catWebClient
        .put(catPort, catHost, catBasePath.concat("/internal/ui/instance"))
        .putHeader("token", token)
        .putHeader("Content-Type", "application/json")
        .addQueryParam(ID, id)
        .sendJsonObject(
            request,
            httpResponseAsyncResult -> {
              if (httpResponseAsyncResult.succeeded()
                  && httpResponseAsyncResult.result().statusCode() == 200) {
                LOGGER.info("central request successful");
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
              }
            });
    return promise.future();
  }

  @Override
  public Future<JsonObject> deleteInstance(String id, String path, String token) {
    Promise<JsonObject> promise = Promise.promise();
    catWebClient
        .delete(catPort, catHost, catBasePath.concat(path.concat("/instance")))
        .putHeader("token", token)
        .putHeader("Content-Type", "application/json")
        .addQueryParam(ID, id)
        .send(
            httpResponseAsyncResult -> {
              if (httpResponseAsyncResult.succeeded()
                  && httpResponseAsyncResult.result().statusCode() == 200) {
                LOGGER.info(
                    "central request successful"
                        + httpResponseAsyncResult.result().bodyAsJsonObject());
                JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
                promise.complete(response);
              } else {
                Throwable cause = httpResponseAsyncResult.cause();
                if (cause != null) {
                  LOGGER.debug(cause.getMessage());
                  promise.fail(cause);
                } else {
                  promise.fail(httpResponseAsyncResult.result().bodyAsString());
                }
              }
            });
    return promise.future(); // Return the future outside the callback function
  }

  @Override
  public Future<JsonObject> createDomain(JsonObject request, String token) {
    Promise<JsonObject> promise = Promise.promise();
    catWebClient
        .post(catPort, catHost, catBasePath.concat("/internal/ui/domain"))
        .putHeader("token", token)
        .putHeader("Content-Type", "application/json")
        .sendJsonObject(
            request,
            httpResponseAsyncResult -> {
              if (httpResponseAsyncResult.succeeded()
                  && httpResponseAsyncResult.result().statusCode() == 201) {
                LOGGER.info("central request successful");
                JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
                promise.complete(response);
              } else {
                Throwable cause = httpResponseAsyncResult.cause();
                if (cause != null) {
                  LOGGER.debug(cause.getMessage());
                  promise.fail(cause);
                } else {
                  promise.fail(httpResponseAsyncResult.result().bodyAsString());
                }
              }
            });
    return promise.future();
  }

  @Override
  public Future<JsonObject> getDomain(String id) {
    Promise<JsonObject> promise = Promise.promise();
    catWebClient
        .get(catPort, catHost, catBasePath.concat("/internal/ui/domain"))
        .addQueryParam("id", id)
        .send(
            httpResponseAsyncResult -> {
              if (httpResponseAsyncResult.succeeded()
                  && httpResponseAsyncResult.result().statusCode() == 200) {
                JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
                promise.complete(response);
              } else {
                Throwable cause = httpResponseAsyncResult.cause();
                if (cause != null) {
                  promise.fail(cause);
                } else {
                  promise.fail(httpResponseAsyncResult.result().bodyAsString());
                }
              }
            });
    return promise.future();
  }

  @Override
  public Future<JsonObject> updateDomain(String id, JsonObject request, String token) {
    Promise<JsonObject> promise = Promise.promise();
    catWebClient
        .put(catPort, catHost, catBasePath.concat("/internal/ui/domain"))
        .putHeader("token", token)
        .putHeader("Content-Type", "application/json")
        .addQueryParam("id", id)
        .sendJsonObject(
            request,
            httpResponseAsyncResult -> {
              if (httpResponseAsyncResult.succeeded()
                  && httpResponseAsyncResult.result().statusCode() == 200) {
                LOGGER.info("central request successful");
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
              }
            });
    return promise.future();
  }

  @Override
  public Future<JsonObject> deleteDomain(String id, String token) {
    Promise<JsonObject> promise = Promise.promise();
    catWebClient
        .delete(catPort, catHost, catBasePath.concat("/internal/ui/domain"))
        .putHeader("token", token)
        .putHeader("Content-Type", "application/json")
        .addQueryParam("id", id)
        .send(
            httpResponseAsyncResult -> {
              if (httpResponseAsyncResult.succeeded()
                  && httpResponseAsyncResult.result().statusCode() == 200) {
                LOGGER.info(
                    "central request successful"
                        + httpResponseAsyncResult.result().bodyAsJsonObject());
                JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
                promise.complete(response);
              } else {
                Throwable cause = httpResponseAsyncResult.cause();
                if (cause != null) {
                  LOGGER.debug(cause.getMessage());
                  promise.fail(cause);
                } else {
                  promise.fail(httpResponseAsyncResult.result().bodyAsString());
                }
              }
            });
    return promise.future(); // Return the future outside the callback function
  }


}
