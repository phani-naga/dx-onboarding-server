package iudx.onboarding.server.catalogue.service;

import static iudx.onboarding.server.common.Constants.TOKEN;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static iudx.onboarding.server.common.Constants.ID;
import static iudx.onboarding.server.common.Constants.TOKEN;

public class LocalCatImpl implements CatalogueService {

  private static final Logger LOGGER = LogManager.getLogger(LocalCatImpl.class);
  public static WebClient catWebClient;
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
            Throwable cause = httpResponseAsyncResult.cause();
            if (cause != null) {
              LOGGER.info("Failure {}", httpResponseAsyncResult.cause());
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
            Throwable cause = httpResponseAsyncResult.cause();
            if (cause != null) {
              LOGGER.info("Failure {}", httpResponseAsyncResult.cause());
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
            LOGGER.info("local request successful" + httpResponseAsyncResult.result().bodyAsJsonObject());
            JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
            promise.complete(response);
          } else {
            Throwable cause = httpResponseAsyncResult.cause();
            if (cause != null) {
              LOGGER.info("Failure {}", httpResponseAsyncResult.cause());
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
    LOGGER.debug("here xx");
    catWebClient
        .get(catPort, catHost, catBasePath.concat("/item"))
        .addQueryParam("id", id)
        .send(httpResponseAsyncResult -> {
          if (httpResponseAsyncResult.succeeded() && httpResponseAsyncResult.result().statusCode() == 200) {
            JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
            LOGGER.debug("getItem id :{}", response);
            promise.complete(response);
          } else {
            Throwable cause = httpResponseAsyncResult.cause();
            if (cause != null) {
              LOGGER.info("Failure {}", httpResponseAsyncResult.cause());
              promise.fail(cause);
            } else {
              LOGGER.debug("get item fail :{}", httpResponseAsyncResult.result().bodyAsString());
              promise.fail(httpResponseAsyncResult.result().bodyAsString());
            }
            // Fail the promise with the failure cause
          }
        });
    return promise.future();
  }

  public Future<JsonObject> getRelatedEntity(String id, String rel, JsonArray filter) {
    Promise<JsonObject> promise = Promise.promise();

    LOGGER.debug(filter.toString().replace("\"", ""));
    catWebClient
        .get(catPort, catHost, catBasePath.concat("/relationship"))
        .addQueryParam("id", id)
        .addQueryParam("rel", rel)
        .addQueryParam("filter", filter.toString().replace("\"", ""))
        .send(relatedEntityHandler -> {
          LOGGER.debug(id);
          LOGGER.debug(relatedEntityHandler.result().body().toJsonObject());
          if (relatedEntityHandler.succeeded() && relatedEntityHandler.result().statusCode() == 200) {
            LOGGER.debug(relatedEntityHandler.result().body());
            promise.complete(relatedEntityHandler.result().body().toJsonObject());
          } else {
            Throwable cause = relatedEntityHandler.cause();
            if (cause != null) {
              LOGGER.info("Failure {}", relatedEntityHandler.cause());
              promise.fail(cause);
            } else {
              promise.fail(relatedEntityHandler.result().bodyAsString());
            }
          }
        });
    return promise.future();
  }

  @Override
  public Future<JsonObject> createInstance(JsonObject request, String path, String token) {
    Promise<JsonObject> promise = Promise.promise();
    request.remove(TOKEN);

    LOGGER.error(token);
    LOGGER.error(path);
    LOGGER.error(request);
    LOGGER.error(request.getString(ID));
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
                LOGGER.info(
                    "request successful" + httpResponseAsyncResult.result().body().toJsonObject());
                JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
                promise.complete(response);
              } else {
                Throwable cause = httpResponseAsyncResult.cause();
                if (cause != null) {
                  LOGGER.info("Failure {}", httpResponseAsyncResult.cause());
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
  public Future<JsonObject> getInstance(String id, String path) {
    LOGGER.info("id" + id);
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
                  LOGGER.info("Failure {}", httpResponseAsyncResult.cause());
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
  public Future<JsonObject> updateInstance(String id, JsonObject request, String token) {
    Promise<JsonObject> promise = Promise.promise();
    request.remove(TOKEN);
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
                LOGGER.info("request successful");
                JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
                promise.complete(response);
              } else {
                Throwable cause = httpResponseAsyncResult.cause();
                if (cause != null) {
                  LOGGER.info("Failure {}", httpResponseAsyncResult.cause());
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
                    "local request successful"
                        + httpResponseAsyncResult.result().bodyAsJsonObject());
                JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
                promise.complete(response);
              } else {
                Throwable cause = httpResponseAsyncResult.cause();
                if (cause != null) {
                  LOGGER.info("Failure {}", httpResponseAsyncResult.cause());
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
  public Future<JsonObject> createDomain(JsonObject request, String token) {
    Promise<JsonObject> promise = Promise.promise();
    request.remove(TOKEN);
    catWebClient
        .post(catPort, catHost, catBasePath.concat("/internal/ui/domain"))
        .putHeader("token", token)
        .putHeader("Content-Type", "application/json")
        .sendJsonObject(
            request,
            httpResponseAsyncResult -> {
              if (httpResponseAsyncResult.succeeded()
                  && httpResponseAsyncResult.result().statusCode() == 201) {
                LOGGER.info(
                    "request successful" + httpResponseAsyncResult.result().body().toJsonObject());
                JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
                promise.complete(response);
              } else {
                Throwable cause = httpResponseAsyncResult.cause();
                if (cause != null) {
                  LOGGER.info("Failure {}", httpResponseAsyncResult.cause());
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
  public Future<JsonObject> getDomain(String id) {
    LOGGER.info("id" + id);
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
                  LOGGER.info("Failure {}", httpResponseAsyncResult.cause());
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
  public Future<JsonObject> updateDomain(String id, JsonObject request, String token) {
    Promise<JsonObject> promise = Promise.promise();
    request.remove(TOKEN);
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
                LOGGER.info("request successful");
                JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
                promise.complete(response);
              } else {
                Throwable cause = httpResponseAsyncResult.cause();
                if (cause != null) {
                  LOGGER.info("Failure {}", httpResponseAsyncResult.cause());
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
                    "local request successful"
                        + httpResponseAsyncResult.result().bodyAsJsonObject());
                JsonObject response = httpResponseAsyncResult.result().body().toJsonObject();
                promise.complete(response);
              } else {
                Throwable cause = httpResponseAsyncResult.cause();
                if (cause != null) {
                  LOGGER.info("Failure {}", httpResponseAsyncResult.cause());
                  promise.fail(cause);
                } else {
                  promise.fail(httpResponseAsyncResult.result().bodyAsString());
                }
                // Fail the promise with the failure cause
              }
            });
    return promise.future(); // Return the future outside the callback function
  }
}
