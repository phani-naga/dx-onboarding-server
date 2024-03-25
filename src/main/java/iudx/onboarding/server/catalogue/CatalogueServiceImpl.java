package iudx.onboarding.server.catalogue;

import static iudx.onboarding.server.apiserver.util.Constants.RESULTS;
import static iudx.onboarding.server.common.Constants.ID;
import static iudx.onboarding.server.common.Constants.TOKEN;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import dev.failsafe.RetryPolicyBuilder;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import iudx.onboarding.server.apiserver.exceptions.DxRuntimeException;
import iudx.onboarding.server.apiserver.util.RespBuilder;
import iudx.onboarding.server.catalogue.service.CentralCatImpl;
import iudx.onboarding.server.catalogue.service.LocalCatImpl;
import iudx.onboarding.server.common.CatalogueType;
import iudx.onboarding.server.common.InconsistencyHandler;
import iudx.onboarding.server.token.TokenService;
import java.net.UnknownHostException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CatalogueServiceImpl implements CatalogueUtilService {

  private static final Logger LOGGER = LogManager.getLogger(CatalogueServiceImpl.class);
  private final TokenService tokenService;
  private final RetryPolicyBuilder<Object> retryPolicyBuilder;
  private CentralCatImpl centralCat;
  private LocalCatImpl localCat;
  private InconsistencyHandler inconsistencyHandler;

  CatalogueServiceImpl(Vertx vertx, TokenService tokenService, RetryPolicyBuilder<Object> retryPolicyBuilder, JsonObject config) {
    this.tokenService = tokenService;
    this.retryPolicyBuilder = retryPolicyBuilder;
    this.centralCat = new CentralCatImpl(vertx, config);
    this.localCat = new LocalCatImpl(vertx, config);
    this.inconsistencyHandler = new InconsistencyHandler(tokenService, localCat, centralCat, retryPolicyBuilder);
  }

  @Override
  public Future<JsonObject> createItem(JsonObject request, String token, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    if (catalogueType.equals(CatalogueType.CENTRAL)) {
      RetryPolicy<Object> retryPolicy = retryPolicyBuilder
          .onSuccess(successListener -> {
            promise.complete((JsonObject) successListener.getResult());
          })
          .onFailure(listener -> {
            LOGGER.warn("Failed to upload item to central");
            String id = request.getString(ID);
            Future.future(f -> inconsistencyHandler.handleDeleteOnLocal(id, token));
            promise.fail(listener.getException().getMessage());
          })
          .build();

      Failsafe.with(retryPolicy)
          .getAsyncExecution(asyncExecution -> {
            tokenService.createToken()
                .compose(adminToken -> {
                  return centralCat.createItem(request, adminToken.getString(TOKEN));
                })
                .onComplete(ar -> {
                  if (ar.succeeded()) {
                    asyncExecution.recordResult(ar.result().getJsonObject(RESULTS));
                  } else {
                    asyncExecution.recordException(new DxRuntimeException(400, ar.cause().getMessage()));
                  }
                });
          });
    } else if (catalogueType.equals(CatalogueType.LOCAL)) {
      localCat.createItem(request, token).onComplete(completeHandler -> {
        if (completeHandler.succeeded()) {
          promise.complete(completeHandler.result());
        } else {
          promise.fail(completeHandler.cause());
        }
      });
    } else {
      promise.fail("Invalid catalogue type");
    }

    return promise.future();
  }

  @Override
  public Future<JsonObject> updateItem(JsonObject request, String token, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    if (catalogueType.equals(CatalogueType.CENTRAL)) {
      RetryPolicy<Object> retryPolicy = retryPolicyBuilder
          .onSuccess(successListener -> {
            promise.complete((JsonObject) successListener.getResult());
          })
          .onFailure(listener -> {
            LOGGER.warn("Failed to update item to central");
            String id = request.getString(ID);
            Future.future(f -> inconsistencyHandler.handleUpdateOnLocal(id, token));
            promise.fail(handleFailure(listener.getException()));
            //promise.fail(new DxRuntimeException(500, listener.getException().getMessage()));
          })
          .build();

      Failsafe.with(retryPolicy)
          .getAsyncExecution(asyncExecution -> {
            tokenService.createToken().compose(adminToken -> {
              return centralCat.updateItem(request, adminToken.getString(TOKEN));
            }).onComplete(ar -> {
              if (ar.succeeded()) {
                asyncExecution.recordResult(ar.result());
              } else {
                asyncExecution.recordException(ar.cause());
              }
            });
          });
    } else if (catalogueType.equals(CatalogueType.LOCAL)) {
      localCat.updateItem(request, token).onComplete(completeHandler -> {
        if (completeHandler.succeeded()) {
          promise.complete(completeHandler.result());
        } else {
          promise.fail(completeHandler.cause());
        }
      });
    } else {
      promise.fail("Invalid catalogue type");
    }

    return promise.future();
  }

  @Override
  public Future<JsonObject> deleteItem(JsonObject request, String token, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    String id = request.getString(ID);
    if (catalogueType.equals(CatalogueType.CENTRAL)) {
      RetryPolicy<Object> retryPolicy = retryPolicyBuilder
          .onSuccess(successListener -> {
            promise.complete((JsonObject) successListener.getResult());
          })
          .onFailure(listener -> {
            LOGGER.warn("Failed to delete item from central");
            Future.future(f -> inconsistencyHandler.handleUploadToLocal(id, token));
            promise.fail(handleFailure(listener.getException()));
          })
          .build();

      Failsafe.with(retryPolicy)
          .getAsyncExecution(asyncExecution -> {
            tokenService.createToken().compose(adminToken -> {
              return centralCat.deleteItem(id, adminToken.getString(TOKEN));
            }).onComplete(ar -> {
              if (ar.succeeded()) {
                asyncExecution.recordResult(ar.result());
              } else {
                asyncExecution.recordException(ar.cause());
              }
            });
          });
    } else if (catalogueType.equals(CatalogueType.LOCAL)) {
      RetryPolicy<Object> retryPolicy = retryPolicyBuilder
          .onSuccess(successListener -> {
            promise.complete((JsonObject) successListener.getResult());
          })
          .onFailure(listener -> {
            LOGGER.warn("Failed to delete item from local catalogue");
            Future.future(f -> inconsistencyHandler.handleRecreateAdapter(id, token));
            promise.fail(handleFailure(listener.getException()));
          })
          .build();
      Failsafe.with(retryPolicy)
          .getAsyncExecution(asyncExecution -> {
            localCat.deleteItem(id, token)
                .onComplete(completeHandler -> {
                  if (completeHandler.succeeded()) {
                    promise.complete(completeHandler.result());
                  } else {
                    promise.fail(completeHandler.cause());
                  }
                });
          });
    } else {
      promise.fail("Invalid catalogue type");
    }

    return promise.future();
  }

  @Override
  public Future<JsonObject> getItem(String id, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    Future<JsonObject> getFuture;
    if (catalogueType.equals(CatalogueType.CENTRAL)) getFuture = centralCat.getItem(id);
    else if (catalogueType.equals(CatalogueType.LOCAL)) {
      getFuture = localCat.getItem(id);
    } else {
      promise.fail("Invalid catalogue type");
      return promise.future();
    }
    getFuture.onComplete(handler -> {
      if (handler.succeeded()) {
        promise.complete(handler.result());
      } else {
        promise.fail(handler.cause());
      }
    });

    return promise.future();
  }

  @Override
  public Future<JsonObject> getInstance(String id, String path, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    Future<JsonObject> getFuture;
    if (catalogueType.equals(CatalogueType.CENTRAL)) getFuture = centralCat.getInstance(id, path);
    else if (catalogueType.equals(CatalogueType.LOCAL)) {
      getFuture = localCat.getInstance(id, path);
    } else {
      promise.fail("Invalid catalogue type");
      return promise.future();
    }
    getFuture.onComplete(
        handler -> {
          if (handler.succeeded()) {
            promise.complete(handler.result());
          } else {
            promise.fail("Request failed");
          }
        });

    return promise.future();
  }

  @Override
  public Future<JsonObject> createInstance(
      String path, JsonObject request, String token, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    if (catalogueType.equals(CatalogueType.CENTRAL)) {
      RetryPolicy<Object> retryPolicy =
          retryPolicyBuilder
              .onSuccess(
                  successListener -> {
                    promise.complete((JsonObject) successListener.getResult());
                  })
              .onFailure(
                  listener -> {
                    LOGGER.warn("Failed to upload item to central");
                    String key = path.isEmpty() ? ID : "instanceId";
                    String id = request.getString(key);
                    Future.future(f -> inconsistencyHandler.handleDeleteInstanceOnLocal(id, path, token));
                    promise.fail(listener.getException().getMessage());
                  })
              .build();

      Failsafe.with(retryPolicy)
          .getAsyncExecution(
              asyncExecution -> {
                tokenService
                    .createToken()
                    .compose(
                        adminToken -> {
                          return centralCat.createInstance(request, path, adminToken.getString(TOKEN));
                        })
                    .onComplete(
                        ar -> {
                          if (ar.succeeded()) {
                            asyncExecution.recordResult(ar.result());
                          } else {
                            asyncExecution.recordException(new DxRuntimeException(400, ar.cause().getMessage()));
                          }
                        });
              });
    } else if (catalogueType.equals(CatalogueType.LOCAL)) {
      LOGGER.error(token);
      LOGGER.error(path);
      LOGGER.error(request);
      LOGGER.error(localCat);
      localCat
          .createInstance(request, path, token)
          .onComplete(
              completeHandler -> {
                if (completeHandler.succeeded()) {
                  promise.complete(completeHandler.result());
                } else {
                  promise.fail(completeHandler.cause());
                }
              });
    } else {
      promise.fail("Invalid catalogue type");
    }

    return promise.future();
  }

  @Override
  public Future<JsonObject> deleteInstance(
      String path, JsonObject request, String token, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    String id = request.getString(ID);
    if (catalogueType.equals(CatalogueType.CENTRAL)) {
      RetryPolicy<Object> retryPolicy =
          retryPolicyBuilder
              .onSuccess(
                  successListener -> {
                    promise.complete((JsonObject) successListener.getResult());
                  })
              .onFailure(
                  listener -> {
                    LOGGER.warn("Failed to delete instance from central");
                    Future.future(f -> inconsistencyHandler.handleUploadInstanceToLocal(id, path, token));
                    promise.fail(listener.getException().getMessage());
                  })
              .build();

      Failsafe.with(retryPolicy)
          .getAsyncExecution(
              asyncExecution -> {
                tokenService
                    .createToken()
                    .compose(
                        adminToken -> {
                          return centralCat.deleteInstance(id, path, adminToken.getString(TOKEN));
                        })
                    .onComplete(
                        ar -> {
                          if (ar.succeeded()) {
                            asyncExecution.recordResult(ar.result());
                          } else {
                            asyncExecution.recordException(new DxRuntimeException(400,ar.cause().getMessage()));
                          }
                        });
              });
    } else if (catalogueType.equals(CatalogueType.LOCAL)) {
      localCat
          .deleteInstance(id, path, token)
          .onComplete(
              completeHandler -> {
                if (completeHandler.succeeded()) {
                  promise.complete(completeHandler.result());
                } else {
                  promise.fail(completeHandler.cause());
                }
              });
    } else {
      promise.fail("Invalid catalogue type");
    }

    return promise.future();
  }

  @Override
  public Future<JsonObject> updateInstance(
      String instanceId, JsonObject request, String token, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    if (catalogueType.equals(CatalogueType.CENTRAL)) {
      RetryPolicy<Object> retryPolicy =
          retryPolicyBuilder
              .onSuccess(
                  successListener -> {
                    promise.complete((JsonObject) successListener.getResult());
                  })
              .onFailure(
                  listener -> {
                    LOGGER.warn("Failed to update instance to central");
                    Future.future(
                        f -> inconsistencyHandler.handleUpdateInstanceOnLocal(instanceId, token));
                    promise.fail(listener.getException().getMessage());
                  })
              .build();

      Failsafe.with(retryPolicy)
          .getAsyncExecution(
              asyncExecution -> {
                tokenService
                    .createToken()
                    .compose(
                        adminToken -> {
                          return centralCat.updateInstance(
                              instanceId, request, adminToken.getString(TOKEN));
                        })
                    .onComplete(
                        ar -> {
                          if (ar.succeeded()) {
                            asyncExecution.recordResult(ar.result());
                          } else {
                            asyncExecution.recordException(new DxRuntimeException(400, ar.cause().getMessage()));
                          }
                        });
              });
    } else if (catalogueType.equals(CatalogueType.LOCAL)) {
      localCat
          .updateInstance(instanceId, request, token)
          .onComplete(
              completeHandler -> {
                if (completeHandler.succeeded()) {
                  promise.complete(completeHandler.result());
                } else {
                  promise.fail(completeHandler.cause());
                }
              });
    } else {
      promise.fail("Invalid catalogue type");
    }

    return promise.future();
  }

  private String handleFailure(Throwable cause) {

    RespBuilder respBuilder;
    if (cause instanceof DxRuntimeException) {
      respBuilder = new RespBuilder()
          .withType("urn:dx:cat:RuntimeException")
          .withTitle("Dx Runtime Exception")
          .withDetail(cause.getMessage());
    } else if (cause instanceof UnknownHostException) {
      respBuilder = new RespBuilder()
          .withType("urn:dx:cat:InternalServerError")
          .withTitle("Internal Server Error")
          .withDetail(cause.getMessage());
    } else {
      respBuilder = new RespBuilder()
          .withType("urn:dx:cat:InternalServerError")
          .withTitle("Internal Server Error")
          .withDetail(cause.getMessage());
    }
    return respBuilder.getResponse();
  }

  @Override
  public Future<JsonObject> getDomain(String id, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    Future<JsonObject> getFuture;
    if (catalogueType.equals(CatalogueType.CENTRAL)) getFuture = centralCat.getDomain(id);
    else if (catalogueType.equals(CatalogueType.LOCAL)) {
      getFuture = localCat.getDomain(id);
    } else {
      promise.fail("Invalid catalogue type");
      return promise.future();
    }
    getFuture.onComplete(
        handler -> {
          if (handler.succeeded()) {
            promise.complete(handler.result());
          } else {
            promise.fail("Request failed");
          }
        });

    return promise.future();
  }

  @Override
  public Future<JsonObject> createDomain(
      JsonObject request, String token, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    if (catalogueType.equals(CatalogueType.CENTRAL)) {
      RetryPolicy<Object> retryPolicy =
          retryPolicyBuilder
              .onSuccess(
                  successListener -> {
                    promise.complete((JsonObject) successListener.getResult());
                  })
              .onFailure(
                  listener -> {
                    LOGGER.warn("Failed to upload domain to central");
                    String id = request.getString("domainId");
                    Future.future(f -> inconsistencyHandler.handleDeleteDomainOnLocal(id, token));
                    promise.fail(handleFailure(listener.getException()));
                  })
              .build();

      Failsafe.with(retryPolicy)
          .getAsyncExecution(
              asyncExecution -> {
                tokenService
                    .createToken()
                    .compose(
                        adminToken -> {
                          return centralCat.createDomain(request, adminToken.getString(TOKEN));
                        })
                    .onComplete(
                        ar -> {
                          if (ar.succeeded()) {
                            asyncExecution.recordResult(ar.result());
                          } else {
                            asyncExecution.recordException(ar.cause());
                          }
                        });
              });
    } else if (catalogueType.equals(CatalogueType.LOCAL)) {
      localCat
          .createDomain(request, token)
          .onComplete(
              completeHandler -> {
                if (completeHandler.succeeded()) {
                  promise.complete(completeHandler.result());
                } else {
                  promise.fail(completeHandler.cause());
                }
              });
    } else {
      promise.fail("Invalid catalogue type");
    }

    return promise.future();
  }

  @Override
  public Future<JsonObject> deleteDomain(
      JsonObject request, String token, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    String id = request.getString(ID);
    if (catalogueType.equals(CatalogueType.CENTRAL)) {
      RetryPolicy<Object> retryPolicy =
          retryPolicyBuilder
              .onSuccess(
                  successListener -> {
                    promise.complete((JsonObject) successListener.getResult());
                  })
              .onFailure(
                  listener -> {
                    LOGGER.warn("Failed to delete instance from central");
                    Future.future(f -> inconsistencyHandler.handleUploadDomainToLocal(id, token));
                    promise.fail(handleFailure(listener.getException()));
                  })
              .build();

      Failsafe.with(retryPolicy)
          .getAsyncExecution(
              asyncExecution -> {
                tokenService
                    .createToken()
                    .compose(
                        adminToken -> {
                          return centralCat.deleteDomain(id, adminToken.getString(TOKEN));
                        })
                    .onComplete(
                        ar -> {
                          if (ar.succeeded()) {
                            asyncExecution.recordResult(ar.result());
                          } else {
                            asyncExecution.recordException(ar.cause());
                          }
                        });
              });
    } else if (catalogueType.equals(CatalogueType.LOCAL)) {
      localCat
          .deleteDomain(id, token)
          .onComplete(
              completeHandler -> {
                if (completeHandler.succeeded()) {
                  promise.complete(completeHandler.result());
                } else {
                  promise.fail(completeHandler.cause());
                }
              });
    } else {
      promise.fail("Invalid catalogue type");
    }

    return promise.future();
  }

  @Override
  public Future<JsonObject> updateDomain(
      String domainId, JsonObject request, String token, CatalogueType catalogueType) {
    Promise<JsonObject> promise = Promise.promise();
    if (catalogueType.equals(CatalogueType.CENTRAL)) {
      RetryPolicy<Object> retryPolicy =
          retryPolicyBuilder
              .onSuccess(
                  successListener -> {
                    promise.complete((JsonObject) successListener.getResult());
                  })
              .onFailure(
                  listener -> {
                    LOGGER.warn("Failed to update instance to central");
                    Future.future(
                        f -> inconsistencyHandler.handleUpdateDomainOnLocal(domainId, token));
                    promise.fail(handleFailure(listener.getException()));
                  })
              .build();

      Failsafe.with(retryPolicy)
          .getAsyncExecution(
              asyncExecution -> {
                tokenService
                    .createToken()
                    .compose(
                        adminToken -> {
                          return centralCat.updateDomain(
                              domainId, request, adminToken.getString(TOKEN));
                        })
                    .onComplete(
                        ar -> {
                          if (ar.succeeded()) {
                            asyncExecution.recordResult(ar.result());
                          } else {
                            asyncExecution.recordException(ar.cause());
                          }
                        });
              });
    } else if (catalogueType.equals(CatalogueType.LOCAL)) {
      localCat
          .updateDomain(domainId, request, token)
          .onComplete(
              completeHandler -> {
                if (completeHandler.succeeded()) {
                  promise.complete(completeHandler.result());
                } else {
                  promise.fail(completeHandler.cause());
                }
              });
    } else {
      promise.fail("Invalid catalogue type");
    }

    return promise.future();
  }

}
