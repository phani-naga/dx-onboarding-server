package iudx.onboarding.server.apiserver;

import com.google.common.hash.Hashing;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import iudx.onboarding.server.catalogue.CatalogueUtilService;
import iudx.onboarding.server.common.Api;
import iudx.onboarding.server.common.CatalogueType;
import iudx.onboarding.server.common.HttpStatusCode;
import iudx.onboarding.server.token.TokenService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static iudx.onboarding.server.apiserver.util.Constants.*;
import static iudx.onboarding.server.apiserver.util.Util.errorResponse;
import static iudx.onboarding.server.common.Constants.*;

/**
 * The Onboarding Server API Verticle.
 *
 * <h1>Onboarding Server API Verticle</h1>
 *
 * <p>
 * The API Server verticle implements the IUDX Onboarding Server APIs. It handles the API requests
 * from the clients and interacts with the associated Service to respond.
 *
 * @version 1.0
 * @see io.vertx.core.Vertx
 * @see AbstractVerticle
 * @see HttpServer
 * @see Router
 * @see io.vertx.servicediscovery.ServiceDiscovery
 * @see io.vertx.servicediscovery.types.EventBusService
 * @see io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
 * @since 2023-07-27
 */
public class ApiServerVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LogManager.getLogger(ApiServerVerticle.class);

  private HttpServer server;
  private Router router;
  private int port;
  private boolean isSSL;
  private String dxApiBasePath;
  private TokenService tokenService;
  private CatalogueUtilService catalogueService;

  /**
   * This method is used to start the Verticle. It deploys a verticle in a cluster, reads the
   * configuration, obtains a proxy for the Event bus services exposed through service discovery,
   * start an HTTPs server at port 8443 or an HTTP server at port 8080.
   *
   * @throws Exception which is a startup exception TODO Need to add documentation for all the
   */
  @Override
  public void start() throws Exception {
    /* Create a reference to HazelcastClusterManager. */

    router = Router.router(vertx);

    /* Get base paths from config */
    dxApiBasePath = config().getString("dxApiBasePath");
    Api api = Api.getInstance(dxApiBasePath);

    /* Define the APIs, methods, endpoints and associated methods. */

    router = Router.router(vertx);
    configureCorsHandler(router);

    putCommonResponseHeaders();

    // attach custom http error responses to router
    configureErrorHandlers(router);

    router.route().handler(BodyHandler.create());
    router.route().handler(TimeoutHandler.create(28000, 408));

    /* NGSI-LD api endpoints */

    // item API
    router.post(api.getOnboardingUrl()).handler(this::createItem);
    router.get(api.getOnboardingUrl()).handler(this::getItem);
    router.patch(api.getOnboardingUrl()).handler(this::updateItem);
    router.delete(api.getOnboardingUrl()).handler(this::deleteItem);

    // instance API
    router.post(api.getInstanceUrl()).handler(this::createInstance);
    router.delete(api.getInstanceUrl()).handler(this::deleteInstance);
    router.patch(api.getInstanceUrl()).handler(this::updateInstance);
    router.get(api.getInstanceUrl()).handler(this::getAllInstance);

    // adapter API
    router.post(api.getIngestionUrl()).handler(this::registerAdapter);

    // TODO : test URL - will be deleted later
    router.post(api.getTokenUrl()).handler(this::handleTokenRequest);

    /* Read ssl configuration. */
    HttpServerOptions serverOptions = new HttpServerOptions();
    setServerOptions(serverOptions);
    serverOptions.setCompressionSupported(true).setCompressionLevel(5);
    server = vertx.createHttpServer(serverOptions);
    server.requestHandler(router).listen(port);

    tokenService = TokenService.createProxy(vertx, TOKEN_ADDRESS);
    catalogueService = CatalogueUtilService.createProxy(vertx, CATALOGUE_ADDRESS);
    /* Print the deployed endpoints */
    LOGGER.info("API server deployed on: " + port);
  }

  /**
   * Configures the CORS handler on the provided router.
   *
   * @param router The router instance to configure the CORS handler on.
   */
  private void configureCorsHandler(Router router) {
    router
        .route()
        .handler(CorsHandler
            .create("*")
            .allowedHeaders(ALLOWED_HEADERS)
            .allowedMethods(ALLOWED_METHODS));
  }

  /**
   * Configures error handlers for the specified status codes on the provided router.
   *
   * @param router The router instance to configure the error handlers on.
   */
  private void configureErrorHandlers(Router router) {
    HttpStatusCode[] statusCodes = HttpStatusCode.values();
    Stream.of(statusCodes).forEach(code -> {
      router.errorHandler(code.getValue(), errorHandler -> {
        HttpServerResponse response = errorHandler.response();
        if (response.headWritten()) {
          try {
            response.close();
          } catch (RuntimeException e) {
            LOGGER.error("Error: " + e);
          }
          return;
        }
        response
            .putHeader(CONTENT_TYPE, APPLICATION_JSON)
            .setStatusCode(code.getValue())
            .end(errorResponse(code));
      });
    });
  }

  /**
   * Sets common response headers to be included in HTTP responses.
   */
  private void putCommonResponseHeaders() {
    router.route().handler(requestHandler -> {
      requestHandler
          .response()
          .putHeader("Cache-Control", "no-cache, no-store,  must-revalidate,max-age=0")
          .putHeader("Pragma", "no-cache")
          .putHeader("Expires", "0")
          .putHeader("X-Content-Type-Options", "nosniff");
      requestHandler.next();
    });
  }

  /**
   * Sets the server options based on the configuration settings. If SSL is enabled, starts an HTTPS
   * server with the specified HTTP port. If SSL is disabled, starts an HTTP server with the
   * specified HTTP port. If the HTTP port is not specified in the configuration, default ports
   * (8080 for HTTP and 8443 for HTTPS) will be used.
   *
   * @param serverOptions The server options to be configured.
   */
  private void setServerOptions(HttpServerOptions serverOptions) {
    isSSL = config().getBoolean("ssl");
    if (isSSL) {
      LOGGER.debug("Info: Starting HTTPs server");
      port = config().getInteger("httpPort") == null ? 8443 : config().getInteger("httpPort");
    } else {
      LOGGER.debug("Info: Starting HTTP server");
      serverOptions.setSsl(false);
      port = config().getInteger("httpPort") == null ? 8080 : config().getInteger("httpPort");
    }
  }

  private void handleTokenRequest(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
    tokenService.createToken()
        .onSuccess(successHandler -> {
          response.setStatusCode(200)
              .end(successHandler.toString());
        })
        .onFailure(failureHandler -> {
          response.setStatusCode(400)
              .end(failureHandler.getMessage());
        });
  }

  private void createItem(RoutingContext routingContext) {
    MultiMap tokenHeadersMap = routingContext.request().headers();
    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = routingContext.body().asJsonObject();
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
    catalogueService
        .createItem(requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.LOCAL)
        .onSuccess(
            localItem -> {
              JsonObject itemBodyWithId = localItem.getJsonObject(RESULTS);
              LOGGER.debug(
                  "item uploaded in local cat {}", itemBodyWithId);

              catalogueService
                  .createItem(itemBodyWithId, tokenHeadersMap.get(TOKEN), CatalogueType.CENTRAL)
                  .onSuccess(
                      centralItem -> {
                        response
                            .setStatusCode(201)
                            .end(
                                centralItem
                                    .toString());
                      })
                  .onFailure(centralCatItemFailure -> {
                    // This is after 3 retries and delete of item from local
                    // TODO: notify user to try again
                    response.setStatusCode(500).end(centralCatItemFailure.getMessage());
                  });
            })
        .onFailure(
            createLocalItemFailureHandler -> {
              LOGGER.info("Local Handler Failed {}", createLocalItemFailureHandler.getLocalizedMessage());
              response.end(createLocalItemFailureHandler.getMessage());
            });
  }

  private void updateItem(RoutingContext routingContext) {
    MultiMap tokenHeadersMap = routingContext.request().headers();
    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = routingContext.body().asJsonObject();
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
    catalogueService
        .updateItem(requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.LOCAL)
        .onSuccess(
            updateLocalItemSuccessHandler -> {
              JsonObject localUpdateResponse = updateLocalItemSuccessHandler;
              LOGGER.debug(
                  "item updated in local cat {}", localUpdateResponse.getJsonArray(RESULTS));

              catalogueService
                  .updateItem(requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.CENTRAL)
                  .onSuccess(
                      updateCentralCatItemSuccess -> {
                        response
                            .setStatusCode(200)
                            .end(
                                updateCentralCatItemSuccess
                                    .toString());
                      })
                  .onFailure(centralCatItemFailure -> {
                    // This is after 3 retries and delete of item from local
                    // TODO: notify user to try again
                    response.setStatusCode(500).end(centralCatItemFailure.getMessage());
                  });
            })
        .onFailure(
            updateLocalItemFailureHandler -> {
              LOGGER.info("Local Handler Failed {}", updateLocalItemFailureHandler.getLocalizedMessage());
              response.end(updateLocalItemFailureHandler.getMessage());
            });
  }

  private void deleteItem(RoutingContext routingContext) {
    MultiMap tokenHeadersMap = routingContext.request().headers();
    HttpServerRequest request = routingContext.request();
    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = new JsonObject()
        .put(ID, request.getParam(ID));
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

    catalogueService
        .deleteItem(requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.LOCAL)
        .onSuccess(
            deleteLocalItemSuccessHandler -> {
              JsonObject localCreateResponse = deleteLocalItemSuccessHandler;
              LOGGER.debug(
                  "item deleted in local cat {}", localCreateResponse.getJsonArray(RESULTS));

              catalogueService
                  .deleteItem(requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.CENTRAL)
                  .onSuccess(
                      deleteCentralCatItemSuccess -> {
                        LOGGER.debug(
                            "item deleted in central cat {}",
                            deleteCentralCatItemSuccess.getJsonArray(RESULTS));
                        response
                            .setStatusCode(200)
                            .end(
                                deleteCentralCatItemSuccess
                                    .toString());
                      })
                  .onFailure(centralCatItemFailure -> {
                    // This is after 3 retries and delete of item from local
                    // TODO: notify user to try again
                    response.setStatusCode(500).end("Upload failed, try again later");
                  });
            })
        .onFailure(
            deleteLocalItemFailureHandler -> {
              LOGGER.info("Local Handler Failed {}", deleteLocalItemFailureHandler.getLocalizedMessage());
              response.end(deleteLocalItemFailureHandler.getMessage());
            });
  }

  private void getItem(RoutingContext routingContext) {
    HttpServerRequest request = routingContext.request();
    HttpServerResponse response = routingContext.response();
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
    catalogueService.getItem(request.getParam(ID), CatalogueType.LOCAL)
        .onSuccess(
            getLocalItemSuccessHandler -> {
              LOGGER.info("Response {}", getLocalItemSuccessHandler);
              LOGGER.info(
                  "item taken from local cat {}", getLocalItemSuccessHandler.getJsonArray(RESULTS));
              // call only if response is 200 -success
              response.setStatusCode(200).end(getLocalItemSuccessHandler.toString());
            })
        .onFailure(
            getLocalItemFailureHandler -> {
              LOGGER.info("Handler Failed {}", getLocalItemFailureHandler.getLocalizedMessage());
              response.end(getLocalItemFailureHandler.getMessage());
            });
  }

  private void createInstance(RoutingContext routingContext) {
    MultiMap tokenHeadersMap = routingContext.request().headers();
    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = routingContext.body().asJsonObject();
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
    catalogueService
        .createInstance(requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.LOCAL)
        .onSuccess(
            localInstance -> {
              LOGGER.info("results after local cat{}", localInstance);
              String instanceId =
                  localInstance.getJsonArray(RESULTS).getJsonObject(0).getString("id");
              requestBody.put("instanceId", instanceId);
              LOGGER.info("request body" + requestBody);
              catalogueService
                  .createInstance(requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.CENTRAL)
                  .onSuccess(
                      centralInstance -> {
                        response.setStatusCode(201).end(centralInstance.toString());
                      })
                  .onFailure(
                      centralinstance -> {
                        // This is after 3 retries and delete of item from local
                        // TODO: notify user to try again
                        response.setStatusCode(500).end(centralinstance.getMessage());
                      });
            })
        .onFailure(
            localInstance -> {
              LOGGER.info("Local Handler Failed {}", localInstance.getLocalizedMessage());
              handleResponse(response, localInstance);


            });
  }

  private void getAllInstance(RoutingContext routingContext) {
    HttpServerRequest request = routingContext.request();
    HttpServerResponse response = routingContext.response();
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
    catalogueService
        .getInstance(request.getParam(ID), CatalogueType.LOCAL)
        .onSuccess(
            getLocalItemSuccessHandler -> {
              LOGGER.info("Response {}", getLocalItemSuccessHandler);
              LOGGER.info(
                  "item taken from local cat {}", getLocalItemSuccessHandler.getJsonArray(RESULTS));
              // call only if response is 200 -success
              response.setStatusCode(200).end(getLocalItemSuccessHandler.toString());
            })
        .onFailure(
            getLocalItemFailureHandler -> {

              LOGGER.info("Handler Failed {}", getLocalItemFailureHandler.getLocalizedMessage());
                handleResponse(response, getLocalItemFailureHandler);
            });
  }

  private void updateInstance(RoutingContext routingContext) {

    MultiMap tokenHeadersMap = routingContext.request().headers();
    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = routingContext.body().asJsonObject();
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

    String id = routingContext.request().getParam("id");
    catalogueService
        .updateInstance(id, requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.LOCAL)
        .onSuccess(
            updateLocalItemSuccessHandler -> {
              JsonObject localUpdateResponse = updateLocalItemSuccessHandler;
              LOGGER.debug(
                  "item updated in local cat {}", localUpdateResponse.getJsonArray(RESULTS));

              catalogueService
                  .updateInstance(
                      id, requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.CENTRAL)
                  .onSuccess(
                      updateCentralCatItemSuccess -> {
                        response.setStatusCode(200).end(updateCentralCatItemSuccess.toString());
                      })
                  .onFailure(
                      centralCatItemFailure -> {
                        // This is after 3 retries and delete of item from local
                        // TODO: notify user to try again
                        response.setStatusCode(500).end(centralCatItemFailure.getMessage());
                      });
            })
        .onFailure(
            updateLocalItemFailureHandler -> {
              LOGGER.info(
                  "Local Handler Failed {}", updateLocalItemFailureHandler.getLocalizedMessage());
                handleResponse(response, updateLocalItemFailureHandler);
            });
  }

  private void deleteInstance(RoutingContext routingContext) {

    MultiMap tokenHeadersMap = routingContext.request().headers();
    HttpServerRequest request = routingContext.request();
    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = new JsonObject().put(ID, request.getParam(ID));
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
    catalogueService
        .deleteInstance(requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.LOCAL)
        .onSuccess(
            deleteLocalItemSuccessHandler -> {
              JsonObject localCreateResponse = deleteLocalItemSuccessHandler;
              LOGGER.debug(
                  "item deleted in local cat {}", localCreateResponse.getJsonArray(RESULTS));

              catalogueService
                  .deleteInstance(requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.CENTRAL)
                  .onSuccess(
                      deleteCentralCatItemSuccess -> {
                        LOGGER.debug(
                            "item deleted in central cat {}",
                            deleteCentralCatItemSuccess.getJsonArray(RESULTS));
                        response.setStatusCode(200).end(deleteCentralCatItemSuccess.toString());
                      })
                  .onFailure(
                      centralCatItemFailure -> {
                        // This is after 3 retries and delete of item from local
                        // TODO: notify user to try again
                        response.setStatusCode(500).end("Upload failed, try again later");
                      });
            })
        .onFailure(
            deleteLocalItemFailureHandler -> {
              LOGGER.info(
                  "Local Handler Failed {}", deleteLocalItemFailureHandler.getLocalizedMessage());
                handleResponse(response, deleteLocalItemFailureHandler);
            });
  }

    private void handleResponse(HttpServerResponse response, Throwable localInstance) {
        String errorMessage = localInstance.getMessage();

        if (errorMessage.contains("urn:dx:cat:InvalidSchema")) {
            response.setStatusCode(400).end(errorMessage);
        } else if (errorMessage.contains("urn:dx:cat:InvalidAuthorizationToken")) {
            response.setStatusCode(401).end(errorMessage);
        } else {
            response.setStatusCode(500).end(errorMessage);
        }
    }

  private void registerAdapter(RoutingContext routingContext) {}

  private void updateAdapter(RoutingContext routingContext) {}

  private void deleteAdapter(RoutingContext routingContext) {}

  private void getAdapter(RoutingContext routingContext) {}
}
