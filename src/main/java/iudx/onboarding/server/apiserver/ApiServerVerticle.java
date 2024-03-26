package iudx.onboarding.server.apiserver;

import static iudx.onboarding.server.apiserver.util.Constants.*;
import static iudx.onboarding.server.apiserver.util.Util.errorResponse;
import static iudx.onboarding.server.common.Constants.*;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import iudx.onboarding.server.apiserver.util.ExceptionHandler;
import iudx.onboarding.server.apiserver.util.RespBuilder;
import iudx.onboarding.server.catalogue.CatalogueUtilService;
import iudx.onboarding.server.catalogue.service.LocalCatImpl;
import iudx.onboarding.server.common.Api;
import iudx.onboarding.server.common.CatalogueType;
import iudx.onboarding.server.common.HttpStatusCode;
import iudx.onboarding.server.resourceserver.ResourceServerService;
import iudx.onboarding.server.token.TokenService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
  private boolean isUacAvailable;
  private String dxApiBasePath;
  private TokenService tokenService;
  private CatalogueUtilService catalogueService;
  private ResourceServerService resourceServerService;

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

    isUacAvailable = config().getBoolean("isUacAvailable");

    router = Router.router(vertx);

    /* Get base paths from config */
    dxApiBasePath = config().getString("dxApiBasePath");
    Api api = Api.getInstance(dxApiBasePath);

    /* Define the APIs, methods, endpoints and associated methods. */

    ExceptionHandler exceptionHandler = new ExceptionHandler();
    router = Router.router(vertx);
    configureCorsHandler(router);

    putCommonResponseHeaders();

    // attach custom http error responses to router
    configureErrorHandlers(router);

    router.route().handler(BodyHandler.create());
    router.route().handler(TimeoutHandler.create(28000, 408));

    // item API
    router.post(api.getOnboardingUrl()).failureHandler(exceptionHandler).handler(this::createItem);
    router.get(api.getOnboardingUrl()).failureHandler(exceptionHandler).handler(this::getItem);
    router.put(api.getOnboardingUrl()).failureHandler(exceptionHandler).handler(this::updateItem);
    router.delete(api.getOnboardingUrl()).failureHandler(exceptionHandler).handler(this::deleteItem);

    // instance API
    router.post(api.getInstanceApi()).handler(this::createInstance).failureHandler(exceptionHandler);
    router.get(api.getInstanceApi()).handler(this::getAllInstance).failureHandler(exceptionHandler);
    router.delete(api.getInstanceApi()).handler(this::deleteInstance).failureHandler(exceptionHandler);

    // mlayer - instance API
    router.post(api.getMlayerInstanceApi()).failureHandler(exceptionHandler).handler(this::createInstance);
    router.delete(api.getMlayerInstanceApi()).failureHandler(exceptionHandler).handler(this::deleteInstance);
    router.put(api.getMlayerInstanceApi()).failureHandler(exceptionHandler).handler(this::updateInstance);
    router.get(api.getMlayerInstanceApi()).failureHandler(exceptionHandler).handler(this::getAllInstance);

    // mlayer - domain API
    router.post(api.getMlayerDomainApi()).failureHandler(exceptionHandler).handler(this::createDomain);
    router.delete(api.getMlayerDomainApi()).failureHandler(exceptionHandler).handler(this::deleteDomain);
    router.put(api.getMlayerDomainApi()).failureHandler(exceptionHandler).handler(this::updateDomain);
    router.get(api.getMlayerDomainApi()).failureHandler(exceptionHandler).handler(this::getDomain);

    // documentation apis
    router.get("/apis/spec")
        .produces("application/json")
        .handler(routingContext -> {
          HttpServerResponse response = routingContext.response();
          response.sendFile("docs/openapi.yaml");
        });
    router.get("/apis")
        .produces("text/html")
        .handler(routingContext -> {
          HttpServerResponse response = routingContext.response();
          response.sendFile("docs/apidoc.html");
        });

    /* Read ssl configuration. */
    HttpServerOptions serverOptions = new HttpServerOptions();
    setServerOptions(serverOptions);
    serverOptions.setCompressionSupported(true).setCompressionLevel(5);
    server = vertx.createHttpServer(serverOptions);
    server.requestHandler(router).listen(port);

    tokenService = TokenService.createProxy(vertx, TOKEN_ADDRESS);
    catalogueService = CatalogueUtilService.createProxy(vertx, CATALOGUE_ADDRESS);
    resourceServerService = ResourceServerService.createProxy(vertx, RS_SERVICE_ADDRESS);
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

  private void createItem(RoutingContext routingContext) {
    MultiMap tokenHeadersMap = routingContext.request().headers();
    HttpServerResponse response = routingContext.response();
    JsonObject requestBody;
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
    requestBody = routingContext.body().asJsonObject();
    ResultContainer resultContainer = new ResultContainer();

    catalogueService
        .createItem(requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.LOCAL)
        .compose(firstHandler -> {
          JsonObject itemBodyWithId = firstHandler.getJsonObject(RESULTS);
          if (isUacAvailable) {
            return catalogueService
                .createItem(itemBodyWithId, tokenHeadersMap.get(TOKEN), CatalogueType.CENTRAL);
          } else {
            return createAdapterForResourceGroup(tokenHeadersMap, resultContainer, itemBodyWithId);
          }
        })
        .compose(nextHandler -> {
          if (isUacAvailable) {
            return createAdapterForResourceGroup(tokenHeadersMap, resultContainer, nextHandler);
          } else {
            if (resultContainer.result.containsKey("item_details")) {
              resultContainer.result.put("adapter_details", nextHandler);
            }
            return Future.succeededFuture();
          }
        })
        .compose(lastHandler -> {
          if (isUacAvailable) {
            if (resultContainer.result.containsKey("item_details")) {
              resultContainer.result.put("adapter_details", lastHandler);
            }
          }
          return Future.succeededFuture();
        })
        .onComplete(completeHandler -> {
          if (completeHandler.succeeded()) {
            RespBuilder respBuilder = new RespBuilder()
                .withType("urn:dx:cat:Success")
                .withTitle("Success")
                .withDetail("Success: Item has been created successfully")
                .withResult(resultContainer.result);
            response
                .setStatusCode(201)
                .end(
                    respBuilder.getResponse());
          } else {
            handleResponse(response, completeHandler.cause());
          }
        });
  }

  private Future<JsonObject> createAdapterForResourceGroup(MultiMap tokenHeadersMap, ResultContainer resultContainer, JsonObject item) {
    String itemType = dxItemType(item.getJsonArray("type"));
    LOGGER.debug(item);
    if (itemType.equalsIgnoreCase("iudx:ResourceGroup")) {
      resultContainer.result = new JsonObject().put("item_details", item);
      String itemId = item.getString("id");
      return resourceServerService.createAdapter(itemId, tokenHeadersMap.get(TOKEN));
    } else {
      resultContainer.result = item;
      return Future.succeededFuture();
    }
  }

  private String dxItemType(JsonArray type) {
    ArrayList<String> ITEM_TYPES =
        new ArrayList<String>(Arrays.asList("iudx:Resource", "iudx:ResourceGroup",
            "iudx:ResourceServer", "iudx:Provider", "iudx:COS"));

    Set<String> types =
        new HashSet<String>(type.getList());
    types.retainAll(ITEM_TYPES);

    return types.toString().replaceAll("\\[", "").replaceAll("\\]", "");
  }

  private void updateItem(RoutingContext routingContext) {
    MultiMap tokenHeadersMap = routingContext.request().headers();
    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = routingContext.body().asJsonObject();
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
    catalogueService
        .updateItem(requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.LOCAL)
        .onSuccess(
            localItem -> {
              JsonObject itemBodyWithId = localItem.getJsonObject(RESULTS);

              if (isUacAvailable) {
                catalogueService
                    .updateItem(itemBodyWithId, tokenHeadersMap.get(TOKEN), CatalogueType.CENTRAL)
                    .onSuccess(
                        centralItem -> {
                          response
                              .setStatusCode(200)
                              .end(
                                  centralItem
                                      .toString());
                        })
                    .onFailure(centralCatItemFailure -> {
                      // This is after 3 retries and delete of item from local
                      // TODO: notify user to try again
                      response.setStatusCode(500).end(centralCatItemFailure.getMessage());
                    });
              } else {
                response.setStatusCode(200)
                    .end(localItem.toString());
              }
            })
        .onFailure(
            updateLocalItemFailureHandler -> {
              LOGGER.info("Local Handler Failed {}", updateLocalItemFailureHandler.getLocalizedMessage());
              handleResponse(response, updateLocalItemFailureHandler);
            });
  }

  private Future<JsonObject> deleteAdapterForResourceGroup(String id, MultiMap tokenHeaderMap) {
    Promise<JsonObject> promise = Promise.promise();
    LOGGER.debug("delete adapter started");
    catalogueService.getItem(id, CatalogueType.LOCAL)
        .compose(localCatResult -> {
          LOGGER.debug("debugging localCat :{}", localCatResult);
          String itemType = dxItemType(localCatResult.getJsonArray("results").getJsonObject(0).getJsonArray("type"));
          LOGGER.debug("debugging itemTpe :{}", itemType);
          if (itemType.equalsIgnoreCase("iudx:ResourceGroup")) {
            return resourceServerService.deleteAdapter(id, tokenHeaderMap.get(TOKEN));
          } else {
            return Future.succeededFuture();
          }
        }).onComplete(completeHandler -> {
          if (completeHandler.succeeded()) {
            promise.complete(completeHandler.result());
          } else {
            LOGGER.error("fail to delete adapter: {}", completeHandler.cause().getMessage());
            promise.fail(completeHandler.cause());
          }
        });
    return promise.future();
  }

  private void deleteItem(RoutingContext routingContext) {
    String itemId = routingContext.request().getParam("id");
    MultiMap tokenHeadersMap = routingContext.request().headers();
    HttpServerRequest request = routingContext.request();
    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = new JsonObject()
        .put(ID, request.getParam(ID));
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
    LOGGER.debug("debugging itemid:{}", itemId);
    deleteAdapterForResourceGroup(itemId, tokenHeadersMap)
        .compose(adapterDel -> {
          return catalogueService.deleteItem(requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.LOCAL);

        })
        .compose(localItem -> {
          if (isUacAvailable) {
            return catalogueService
                .deleteItem(requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.CENTRAL);
          } else {
            return Future.succeededFuture(localItem);
          }
        }).onComplete(completeHandler -> {
          if (completeHandler.succeeded()) {
            response.setStatusCode(200).end(completeHandler.result().toString());
          } else {
            handleResponse(response, completeHandler.cause());
          }
        });

  }


  private void getItem(RoutingContext routingContext) {
    HttpServerRequest request = routingContext.request();
    HttpServerResponse response = routingContext.response();
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
    catalogueService.getItem(request.getParam(ID), CatalogueType.LOCAL)
        .onSuccess(
            getLocalItemSuccessHandler -> {
              response
                  .setStatusCode(200)
                  .end(
                      getLocalItemSuccessHandler.toString());

            })
        .onFailure(
            getLocalItemFailureHandler -> {

              LOGGER.info("Handler Failed {}", getLocalItemFailureHandler.getLocalizedMessage());
              handleResponse(response, getLocalItemFailureHandler);
            });
  }

  private void createInstance(RoutingContext routingContext) {
    MultiMap tokenHeadersMap = routingContext.request().headers();
    HttpServerRequest request = routingContext.request();
    HttpServerResponse response = routingContext.response();
    String path = request.path().contains("/internal/ui") ? "/internal/ui" : "";
    JsonObject requestBody
        = path.isEmpty() ? new JsonObject().put(ID, routingContext.queryParams().get(ID)) : routingContext.body().asJsonObject();
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
    catalogueService
        .createInstance(path, requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.LOCAL)
        .onSuccess(
            localInstance -> {
              LOGGER.info("results after local cat{}", localInstance);
              if (!path.isEmpty()) {
                String instanceId =
                    localInstance.getJsonArray(RESULTS).getJsonObject(0).getString("id");
                localInstance.put(DETAIL, "Success: Instance has been created successfully");
                requestBody.put("instanceId", instanceId);
              }
              LOGGER.info("request body" + requestBody);
              if (isUacAvailable) {
                catalogueService
                    .createInstance(path, requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.CENTRAL)
                    .onSuccess(
                        centralInstance -> {
                          centralInstance.put(
                              DETAIL, "Success: Instance has been created successfully");
                          response.setStatusCode(201).end(centralInstance.toString());
                        })
                    .onFailure(
                        centralinstance -> {
                          // This is after 3 retries and delete of item from local
                          // TODO: notify user to try again
                          handleResponse(response, centralinstance);
                        });
              } else {

                response.setStatusCode(201).end(localInstance.toString());
              }
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
    String path = request.path().contains("/internal/ui") ? "/internal/ui" : "";
    catalogueService
        .getInstance(request.getParam(ID), path, CatalogueType.LOCAL)
        .onSuccess(
            getLocalItemSuccessHandler -> {
              LOGGER.info("Response {}", getLocalItemSuccessHandler);
              LOGGER.info(
                  "item taken from local cat {}", getLocalItemSuccessHandler.getJsonArray(RESULTS));
              // call only if response is 200 -success
              getLocalItemSuccessHandler.put(DETAIL, "Success: Instance fetched successfully");
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
              updateLocalItemSuccessHandler.put(DETAIL, "Instance Updated Successfully");
              JsonObject localUpdateResponse = updateLocalItemSuccessHandler;
              LOGGER.debug(
                  "item updated in local cat {}", localUpdateResponse.getJsonArray(RESULTS));

              if (isUacAvailable) {
                catalogueService
                    .updateInstance(
                        id, requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.CENTRAL)
                    .onSuccess(
                        updateCentralCatItemSuccess -> {
                          updateCentralCatItemSuccess.put(DETAIL, "Instance Updated Successfully");
                          response.setStatusCode(200).end(updateCentralCatItemSuccess.toString());
                        })
                    .onFailure(
                        centralCatItemFailure -> {
                          // This is after 3 retries and delete of item from local
                          // TODO: notify user to try again
                          response.setStatusCode(500).end(centralCatItemFailure.getMessage());
                        });
              } else {

                response.setStatusCode(200).end(updateLocalItemSuccessHandler.toString());
              }
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
    String path = request.path().contains("/internal/ui") ? "/internal/ui" : "";
    catalogueService
        .deleteInstance(path, requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.LOCAL)
        .onSuccess(
            localInstance -> {
              localInstance.put(DETAIL, "Instance deleted Successfully");
              LOGGER.debug(
                  "item deleted in local cat {}", localInstance.getJsonArray(RESULTS));

              if (isUacAvailable) {
                catalogueService
                    .deleteInstance(path, requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.CENTRAL)
                    .onSuccess(
                        deleteCentralCatItemSuccess -> {
                          deleteCentralCatItemSuccess.put(DETAIL, "Instance deleted Successfully");
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
              } else {

                response.setStatusCode(200).end(localInstance.toString());
              }
            })
        .onFailure(
            deleteLocalItemFailureHandler -> {
              LOGGER.info(
                  "Local Handler Failed {}", deleteLocalItemFailureHandler.getLocalizedMessage());
              handleResponse(response, deleteLocalItemFailureHandler);
            });
  }

  private void createDomain(RoutingContext routingContext) {
    MultiMap tokenHeadersMap = routingContext.request().headers();
    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = routingContext.body().asJsonObject();
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
    catalogueService
        .createDomain(requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.LOCAL)
        .onSuccess(
            localDomain -> {
              localDomain.put(DETAIL, "domain Created Successfully");
              LOGGER.info("results after local cat{}", localDomain);
              String domainId = localDomain.getJsonArray(RESULTS).getJsonObject(0).getString("id");
              requestBody.put("domainId", domainId);
              LOGGER.info("request body" + requestBody);
              if (isUacAvailable) {
                catalogueService
                    .createDomain(requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.CENTRAL)
                    .onSuccess(
                        centralInstance -> {
                          centralInstance.put(DETAIL, "domain Created Successfully");
                          response.setStatusCode(201).end(centralInstance.toString());
                        })
                    .onFailure(
                        centralInstance -> {
                          // This is after 3 retries and delete of item from local
                          // TODO: notify user to try again
                          response.setStatusCode(500).end(centralInstance.getMessage());
                        });
              } else {

                response.setStatusCode(201).end(localDomain.toString());
              }
            })
        .onFailure(
            localDomainFailure -> {
              LOGGER.info("Local Handler Failed {}", localDomainFailure.getLocalizedMessage());
              handleResponse(response, localDomainFailure);
            });
  }

  private void deleteDomain(RoutingContext routingContext) {

    MultiMap tokenHeadersMap = routingContext.request().headers();
    HttpServerRequest request = routingContext.request();
    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = new JsonObject().put(ID, request.getParam(ID));
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
    catalogueService
        .deleteDomain(requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.LOCAL)
        .onSuccess(
            deleteLocalDomain -> {
              deleteLocalDomain.put(DETAIL, "Domain deleted Successfully");
              LOGGER.debug("item deleted in local cat {}", deleteLocalDomain.getJsonArray(RESULTS));

              if (isUacAvailable) {
                catalogueService
                    .deleteDomain(requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.CENTRAL)
                    .onSuccess(
                        deleteCentralDomain -> {
                          deleteCentralDomain.put(DETAIL, "Domain deleted Successfully");
                          LOGGER.debug(
                              "item deleted in central cat {}",
                              deleteCentralDomain.getJsonArray(RESULTS));
                          response.setStatusCode(200).end(deleteCentralDomain.toString());
                        })
                    .onFailure(
                        deleteCentralDomainFailure -> {
                          // This is after 3 retries and delete of item from local
                          // TODO: notify user to try again
                          response.setStatusCode(500).end("Upload failed, try again later");
                        });
              } else {
                response.setStatusCode(200).end(deleteLocalDomain.toString());

              }
            })
        .onFailure(
            deleteLocalDomainFailure -> {
              LOGGER.info(
                  "Local Handler Failed {}", deleteLocalDomainFailure.getLocalizedMessage());
              handleResponse(response, deleteLocalDomainFailure);
            });
  }

  private void updateDomain(RoutingContext routingContext) {

    MultiMap tokenHeadersMap = routingContext.request().headers();
    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = routingContext.body().asJsonObject();
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

    String id = routingContext.request().getParam("id");
    catalogueService
        .updateDomain(id, requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.LOCAL)
        .onSuccess(
            updateLocalDomain -> {
              updateLocalDomain.put(DETAIL, "Domain Updated Successfully");
              LOGGER.debug(
                  "domain updated in local cat {}", updateLocalDomain.getJsonArray(RESULTS));

              if (isUacAvailable) {
                catalogueService
                    .updateDomain(id, requestBody, tokenHeadersMap.get(TOKEN), CatalogueType.CENTRAL)
                    .onSuccess(
                        updateCentralDomainSuccess -> {
                          updateCentralDomainSuccess.put(DETAIL, "Domain Updated Successfully");
                          response.setStatusCode(200).end(updateCentralDomainSuccess.toString());
                        })
                    .onFailure(
                        centralCatDomainFailure -> {
                          // This is after 3 retries and delete of item from local
                          // TODO: notify user to try again
                          response.setStatusCode(500).end(centralCatDomainFailure.getMessage());
                        });
              } else {
                response.setStatusCode(200).end(updateLocalDomain.toString());

              }
            })
        .onFailure(
            updateLocalDomainFailure -> {
              LOGGER.info(
                  "Local Handler Failed {}", updateLocalDomainFailure.getLocalizedMessage());
              handleResponse(response, updateLocalDomainFailure);
            });
  }

  private void getDomain(RoutingContext routingContext) {
    HttpServerRequest request = routingContext.request();
    HttpServerResponse response = routingContext.response();
    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);
    catalogueService
        .getDomain(request.getParam(ID), CatalogueType.LOCAL)
        .onSuccess(
            getLocalDomainSuccess -> {
              getLocalDomainSuccess.put(DETAIL, "Domain fetched Successfully");
              LOGGER.info("Response {}", getLocalDomainSuccess);
              LOGGER.info(
                  "item taken from local cat {}", getLocalDomainSuccess.getJsonArray(RESULTS));
              // call only if response is 200 -success
              response.setStatusCode(200).end(getLocalDomainSuccess.toString());
            })
        .onFailure(
            getLocalDomainFailure -> {
              LOGGER.info("Handler Failed {}", getLocalDomainFailure.getLocalizedMessage());
              handleResponse(response, getLocalDomainFailure);
            });
  }

  private void handleResponse(HttpServerResponse response, Throwable localInstance) {
    String errorMessage = localInstance.getMessage();

    LOGGER.debug(errorMessage);

    if (errorMessage.contains(":InvalidSchema")) {
      response.setStatusCode(400).end(errorMessage);
    } else if (errorMessage.contains(":InvalidAuthorizationToken") || errorMessage.contains(":invalidAuthorizationToken")) {
      response.setStatusCode(401).end(errorMessage);
    } else if (errorMessage.contains(":ItemNotFound")) {
      response.setStatusCode(404).end(errorMessage);
    } else if (errorMessage.contains(":InvalidSyntax")) {
      response.setStatusCode(400).end(errorMessage);
    } else if (errorMessage.contains(":OperationNotAllowed")) {
      response.setStatusCode(400).end(errorMessage);
    } else if (errorMessage.contains(":InvalidUUID")) {
      response.setStatusCode(400).end(errorMessage);
    } else if (errorMessage.contains(":LinkValidationFailed")) {
      response.setStatusCode(400).end(errorMessage);
    } else if (errorMessage.contains(":urn:dx:cat:InvalidUUID")) {
      response.setStatusCode(400).end(errorMessage);
    } else {
      response.setStatusCode(500).end(errorMessage);
    }
  }

  final class ResultContainer {
    JsonObject result;
  }

}
