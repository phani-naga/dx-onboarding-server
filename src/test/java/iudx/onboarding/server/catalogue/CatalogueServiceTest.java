package iudx.onboarding.server.catalogue;

import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeExecutor;
import dev.failsafe.RetryPolicy;
import dev.failsafe.RetryPolicyBuilder;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import iudx.onboarding.server.catalogue.service.LocalCatImpl;
import iudx.onboarding.server.common.CatalogueType;
import iudx.onboarding.server.token.TokenService;
import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.vertx.junit5.*;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(VertxExtension.class)
public class CatalogueServiceTest {

  CatalogueServiceImpl catalogueService;
  @Mock private WebClient catWebClient;
  @Mock HttpRequest<Buffer> httpRequest;
  @Mock HttpResponse<Buffer> httpResponse;
  @Mock AsyncResult<HttpResponse<Buffer>> httpResponseAsyncResult;
  @Mock Buffer buffer;
  @Mock Throwable throwable;


  @BeforeEach
  void setUp() {
    JsonObject config =
        new JsonObject()
            .put("centralCatServerHost", "localhost")
            .put("centralCatServerPort", 8080)
            .put("dxCatalogueBasePath", "/api")
            .put("localCatServerHost", "localhost")
            .put("localCatServerPort", 8080);
      LocalCatImpl.catWebClient = mock(WebClient.class);
      catalogueService =
        new CatalogueServiceImpl(Vertx.vertx(), mock(TokenService.class), null, config);
  }

  @Test
  @Description("test createItem when handler succeeds and type is local")
  public void testCreateItemLocal(VertxTestContext testContext) {

    JsonObject request = new JsonObject().put("token", "xyz");
   CatalogueType localType = CatalogueType.LOCAL;
    when(LocalCatImpl.catWebClient.post(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    when(httpResponse.statusCode()).thenReturn(201);
    when(httpResponse.body()).thenReturn(buffer);

      doAnswer(
              (Answer<Void>)
                      invocation -> {
                          ((Handler<AsyncResult<HttpResponse<Buffer>>>) invocation.getArgument(1))
                                  .handle(httpResponseAsyncResult);
                          return null;
                      })
              .when(httpRequest)
              .sendJsonObject(any(JsonObject.class), any());
      catalogueService
        .createItem(request, "xyz", localType)
        .onComplete(
            ar -> {
              if (ar.succeeded()) {
                verify(httpRequest, times(1)).sendJsonObject(any(), any());
                testContext.completeNow();
              } else {
                testContext.failNow(ar.cause());
              }
            });
  }

 @Test
  @Description("test createItem when handler fails and type is local")
  public void testCreateItemLocalFailed(VertxTestContext testContext) {

    JsonObject request = new JsonObject().put("token", "xyz");
    CatalogueType localType = CatalogueType.LOCAL;
    when(LocalCatImpl.catWebClient.post(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(false);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    doAnswer(
            (Answer<AsyncResult<HttpResponse<Buffer>>>)
                arg0 -> {
                  ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                      .handle(httpResponseAsyncResult);
                  return null;
                })
        .when(httpRequest)
        .sendJsonObject(any(), any());
    catalogueService
        .createItem(request, "xyz", localType)
        .onComplete(
            ar -> {
              if (ar.succeeded()) {
                verify(httpRequest, times(1)).sendJsonObject(any(), any());

                testContext.failNow(ar.cause());

              } else {
                testContext.completeNow();
              }
            });
  }

  @Test
  @Description("test createItem when handler fails and type is local")
  public void testCreateItemFailed(VertxTestContext testContext) {

    JsonObject request = new JsonObject().put("token", "xyz");
    CatalogueType localType = CatalogueType.LOCAL;
    when(LocalCatImpl.catWebClient.post(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(false);
    when(httpResponseAsyncResult.cause()).thenReturn(throwable);
    doAnswer(
            (Answer<AsyncResult<HttpResponse<Buffer>>>)
                arg0 -> {
                  ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                      .handle(httpResponseAsyncResult);
                  return null;
                })
        .when(httpRequest)
        .sendJsonObject(any(), any());
    catalogueService
        .createItem(request, "xyz", localType)
        .onComplete(
            ar -> {
              if (ar.succeeded()) {
                verify(httpRequest, times(1)).sendJsonObject(any(), any());

                testContext.failNow(ar.cause());

              } else {
                testContext.completeNow();
              }
            });
  }


  @Test
  @Description("test updateItem when handler succeeds and type is local")
  public void testUpdateItemLocal(VertxTestContext testContext) {

    JsonObject request = new JsonObject().put("token", "xyz");
    CatalogueType localType = CatalogueType.LOCAL;
    when(LocalCatImpl.catWebClient.put(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    when(httpResponse.statusCode()).thenReturn(200);
    when(httpResponse.body()).thenReturn(buffer);
    doAnswer(
            (Answer<AsyncResult<HttpResponse<Buffer>>>)
                arg0 -> {
                  ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                      .handle(httpResponseAsyncResult);
                  return null;
                })
        .when(httpRequest)
        .sendJsonObject(any(), any());
    catalogueService
        .updateItem(request, "xyz", localType)
        .onComplete(
            ar -> {
              if (ar.succeeded()) {
                verify(httpRequest, times(1)).sendJsonObject(any(), any());

                testContext.completeNow();
              } else {
                testContext.failNow(ar.cause());
              }
            });
  }

  @Test
  @Description("test updateItem when handler fails and type is local")
  public void testUpdateItemFailed(VertxTestContext testContext) {

    JsonObject request = new JsonObject().put("token", "xyz");
    CatalogueType localType = CatalogueType.LOCAL;
    when(LocalCatImpl.catWebClient.put(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(false);
    when(httpResponseAsyncResult.cause()).thenReturn(throwable);
    doAnswer(
            (Answer<AsyncResult<HttpResponse<Buffer>>>)
                arg0 -> {
                  ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                      .handle(httpResponseAsyncResult);
                  return null;
                })
        .when(httpRequest)
        .sendJsonObject(any(), any());
    catalogueService
        .updateItem(request, "xyz", localType)
        .onComplete(
            ar -> {
              if (ar.succeeded()) {
                verify(httpRequest, times(1)).sendJsonObject(any(), any());

                testContext.failNow(ar.cause());
              } else {
                testContext.completeNow();
              }
            });
  }

  @Test
  @Description("test updateItem when handler fails and type is local")
  public void testUpdateItemLocalFailed(VertxTestContext testContext) {

    JsonObject request = new JsonObject().put("token", "xyz");
    CatalogueType localType = CatalogueType.LOCAL;
    when(LocalCatImpl.catWebClient.put(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(false);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    doAnswer(
            (Answer<AsyncResult<HttpResponse<Buffer>>>)
                arg0 -> {
                  ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                      .handle(httpResponseAsyncResult);
                  return null;
                })
        .when(httpRequest)
        .sendJsonObject(any(), any());
    catalogueService
        .updateItem(request, "xyz", localType)
        .onComplete(
            ar -> {
              if (ar.succeeded()) {
                verify(httpRequest, times(1)).sendJsonObject(any(), any());

                testContext.failNow(ar.cause());
              } else {
                testContext.completeNow();
              }
            });
  }

  @Test
  @Description("test deleteItem when handler succeeds and type is local")
  public void testDelateItemLocal(VertxTestContext testContext) {

    JsonObject request = new JsonObject().put("token", "xyz").put("id", "dummy");
    CatalogueType localType = CatalogueType.LOCAL;
    String id = "dummy";
    when(LocalCatImpl.catWebClient.delete(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    when(httpResponse.statusCode()).thenReturn(200);
    when(httpResponse.body()).thenReturn(buffer);
    doAnswer(
            (Answer<AsyncResult<HttpResponse<Buffer>>>)
                arg0 -> {
                  ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                      .handle(httpResponseAsyncResult);
                  return null;
                })
        .when(httpRequest)
        .send(any());
    catalogueService
        .deleteItem(request, "xyz", localType)
        .onComplete(
            ar -> {
              if (ar.succeeded()) {
                verify(httpRequest, times(1)).send(any());

                testContext.completeNow();
              } else {
                testContext.failNow(ar.cause());
              }
            });
  }

  @Test
  @Description("test deleteItem when handler fails and type is local")
  public void testDelateItemLocalFailed(VertxTestContext testContext) {

    JsonObject request = new JsonObject().put("token", "xyz").put("id", "dummy");
    CatalogueType localType = CatalogueType.LOCAL;
    String id = "dummy";
    when(LocalCatImpl.catWebClient.delete(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(false);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    doAnswer(
            (Answer<AsyncResult<HttpResponse<Buffer>>>)
                arg0 -> {
                  ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                      .handle(httpResponseAsyncResult);
                  return null;
                })
        .when(httpRequest)
        .send(any());
    catalogueService
        .deleteItem(request, "xyz", localType)
        .onComplete(
            ar -> {
              if (ar.succeeded()) {
                verify(httpRequest, times(1)).send(any());

                testContext.failNow(ar.cause());
              } else {
                testContext.completeNow();
              }
            });
  }

  @Test
  @Description("test deleteItem when handler fails and type is local")
  public void testDelateItemFailed(VertxTestContext testContext) {

    JsonObject request = new JsonObject().put("token", "xyz").put("id", "dummy");
    CatalogueType localType = CatalogueType.LOCAL;
    String id = "dummy";
    when(LocalCatImpl.catWebClient.delete(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(false);
    when(httpResponseAsyncResult.cause()).thenReturn(throwable);
    doAnswer(
            (Answer<AsyncResult<HttpResponse<Buffer>>>)
                arg0 -> {
                  ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                      .handle(httpResponseAsyncResult);
                  return null;
                })
        .when(httpRequest)
        .send(any());
    catalogueService
        .deleteItem(request, "xyz", localType)
        .onComplete(
            ar -> {
              if (ar.succeeded()) {
                verify(httpRequest, times(1)).send(any());

                testContext.failNow(ar.cause());
              } else {
                testContext.completeNow();
              }
            });
  }

  @Test
  @Description("test getItem when handler succeeds and type is local")
  public void testGetItemLocal(VertxTestContext testContext) {
    CatalogueType localType = CatalogueType.LOCAL;
    String id = "dummy";
    when(LocalCatImpl.catWebClient.get(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    when(httpResponse.statusCode()).thenReturn(200);
    when(httpResponse.body()).thenReturn(buffer);
    doAnswer(
            (Answer<AsyncResult<HttpResponse<Buffer>>>)
                arg0 -> {
                  ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                      .handle(httpResponseAsyncResult);
                  return null;
                })
        .when(httpRequest)
        .send(any());
    catalogueService
        .getItem(id, localType)
        .onComplete(
            ar -> {
              if (ar.succeeded()) {
                verify(httpRequest, times(1)).send(any());

                testContext.completeNow();
              } else {
                testContext.failNow(ar.cause());
              }
            });
  }

  @Test
  @Description("test getItem when handler fails and type is local")
  public void testGetItemLocalFailed(VertxTestContext testContext) {
    CatalogueType localType = CatalogueType.LOCAL;
    String id = "dummy";
    when(LocalCatImpl.catWebClient.get(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(false);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    doAnswer(
            (Answer<AsyncResult<HttpResponse<Buffer>>>)
                arg0 -> {
                  ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                      .handle(httpResponseAsyncResult);
                  return null;
                })
        .when(httpRequest)
        .send(any());
    catalogueService
        .getItem(id, localType)
        .onComplete(
            ar -> {
              if (ar.succeeded()) {
                verify(httpRequest, times(1)).send(any());

                testContext.failNow(ar.cause());
              } else {
                testContext.completeNow();
              }
            });
  }

  @Test
  @Description("test getItem when handler fails and type is local")
  public void testGetItemFailed(VertxTestContext testContext) {
    CatalogueType localType = CatalogueType.LOCAL;
    String id = "dummy";
    when(LocalCatImpl.catWebClient.get(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(false);
    when(httpResponseAsyncResult.cause()).thenReturn(throwable);
    doAnswer(
            (Answer<AsyncResult<HttpResponse<Buffer>>>)
                arg0 -> {
                  ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                      .handle(httpResponseAsyncResult);
                  return null;
                })
        .when(httpRequest)
        .send(any());
    catalogueService
        .getItem(id, localType)
        .onComplete(
            ar -> {
              if (ar.succeeded()) {
                verify(httpRequest, times(1)).send(any());

                testContext.failNow(ar.cause());
              } else {
                testContext.completeNow();
              }
            });
  }

    @Test
    @Description("test createItem when handler succeeds and type is local")
    public void testCreateInstanceLocal(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
        when(LocalCatImpl.catWebClient.post(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(201);
        when(httpResponse.body()).thenReturn(buffer);

        doAnswer(
                (Answer<Void>)
                        invocation -> {
                            ((Handler<AsyncResult<HttpResponse<Buffer>>>) invocation.getArgument(1))
                                    .handle(httpResponseAsyncResult);
                            return null;
                        })
                .when(httpRequest)
                .sendJsonObject(any(JsonObject.class), any());
        catalogueService
                .createInstance(request, "xyz", localType)
                .onComplete(
                        ar -> {
                            if (ar.succeeded()) {
                                verify(httpRequest, times(1)).sendJsonObject(any(), any());
                                verify(LocalCatImpl.catWebClient, times(1)).post(anyInt(), anyString(), anyString());
                                testContext.completeNow();
                            } else {
                                testContext.failNow(ar.cause());
                            }
                        });
    }

    @Test
    @Description("test createItem when handler fails and type is local")
    public void testCreateInstanceLocalFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
        when(LocalCatImpl.catWebClient.post(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(false);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        doAnswer(
                (Answer<AsyncResult<HttpResponse<Buffer>>>)
                        arg0 -> {
                            ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                                    .handle(httpResponseAsyncResult);
                            return null;
                        })
                .when(httpRequest)
                .sendJsonObject(any(), any());
        catalogueService
                .createInstance(request, "xyz", localType)
                .onComplete(
                        ar -> {
                            if (ar.succeeded()) {
                                verify(httpRequest, times(1)).sendJsonObject(any(), any());
                                verify(LocalCatImpl.catWebClient, times(1)).post(anyInt(), anyString(), anyString());
                                testContext.failNow(ar.cause());

                            } else {
                                testContext.completeNow();
                            }
                        });
    }

    @Test
    @Description("test createItem when handler fails and type is local")
    public void testCreateInstanceFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
        when(LocalCatImpl.catWebClient.post(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(false);
        when(httpResponseAsyncResult.cause()).thenReturn(throwable);
        doAnswer(
                (Answer<AsyncResult<HttpResponse<Buffer>>>)
                        arg0 -> {
                            ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                                    .handle(httpResponseAsyncResult);
                            return null;
                        })
                .when(httpRequest)
                .sendJsonObject(any(), any());
        catalogueService
                .createInstance(request, "xyz", localType)
                .onComplete(
                        ar -> {
                            if (ar.succeeded()) {
                                verify(httpRequest, times(1)).sendJsonObject(any(), any());
                                verify(LocalCatImpl.catWebClient, times(1)).post(anyInt(), anyString(), anyString());
                                testContext.failNow(ar.cause());

                            } else {
                                testContext.completeNow();
                            }
                        });
    }


    @Test
    @Description("test updateItem when handler succeeds and type is local")
    public void testUpdateInstanceLocal(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
        when(LocalCatImpl.catWebClient.put(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam("id","abc")).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(buffer);
        doAnswer(
                (Answer<AsyncResult<HttpResponse<Buffer>>>)
                        arg0 -> {
                            ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                                    .handle(httpResponseAsyncResult);
                            return null;
                        })
                .when(httpRequest)
                .sendJsonObject(any(), any());
        catalogueService
                .updateInstance("abc",request, "xyz", localType)
                .onComplete(
                        ar -> {
                            if (ar.succeeded()) {
                                verify(httpRequest, times(1)).sendJsonObject(any(), any());
                                verify(LocalCatImpl.catWebClient, times(1)).put(anyInt(), anyString(), anyString());
                                testContext.completeNow();
                            } else {
                                testContext.failNow(ar.cause());
                            }
                        });
    }

    @Test
    @Description("test updateItem when handler fails and type is local")
    public void testUpdateInstanceFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
        when(LocalCatImpl.catWebClient.put(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam("id","abc")).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(false);
        when(httpResponseAsyncResult.cause()).thenReturn(throwable);
        doAnswer(
                (Answer<AsyncResult<HttpResponse<Buffer>>>)
                        arg0 -> {
                            ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                                    .handle(httpResponseAsyncResult);
                            return null;
                        })
                .when(httpRequest)
                .sendJsonObject(any(), any());
        catalogueService
                .updateInstance("abc", request, "xyz", localType)
                .onComplete(
                        ar -> {
                            if (ar.succeeded()) {
                                verify(httpRequest, times(1)).sendJsonObject(any(), any());
                                verify(LocalCatImpl.catWebClient, times(1)).put(anyInt(), anyString(), anyString());
                                testContext.failNow(ar.cause());
                            } else {
                                testContext.completeNow();
                            }
                        });
    }

    @Test
    @Description("test updateItem when handler fails and type is local")
    public void testUpdateInstanceLocalFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
        when(LocalCatImpl.catWebClient.put(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam("id", "abc")).thenReturn(httpRequest);
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(false);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        doAnswer(
                (Answer<AsyncResult<HttpResponse<Buffer>>>)
                        arg0 -> {
                            ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                                    .handle(httpResponseAsyncResult);
                            return null;
                        })
                .when(httpRequest)
                .sendJsonObject(any(), any());
        catalogueService
                .updateInstance("abc", request, "xyz", localType)
                .onComplete(
                        ar -> {
                            if (ar.succeeded()) {
                                verify(httpRequest, times(1)).sendJsonObject(any(), any());
                                verify(LocalCatImpl.catWebClient, times(1)).put(anyInt(), anyString(), anyString());
                                testContext.failNow(ar.cause());
                            } else {
                                testContext.completeNow();
                            }
                        });
    }

    @Test
    @Description("test deleteItem when handler succeeds and type is local")
    public void testDeleteInstanceLocal(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz").put("id", "dummy");
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
        when(LocalCatImpl.catWebClient.delete(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(buffer);
        doAnswer(
                (Answer<AsyncResult<HttpResponse<Buffer>>>)
                        arg0 -> {
                            ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                                    .handle(httpResponseAsyncResult);
                            return null;
                        })
                .when(httpRequest)
                .send(any());
        catalogueService
                .deleteInstance(request, "xyz", localType)
                .onComplete(
                        ar -> {
                            if (ar.succeeded()) {
                                verify(httpRequest, times(1)).send(any());
                                verify(LocalCatImpl.catWebClient, times(1)).delete(anyInt(), anyString(), anyString());
                                testContext.completeNow();
                            } else {
                                testContext.failNow(ar.cause());
                            }
                        });
    }

    @Test
    @Description("test deleteItem when handler fails and type is local")
    public void testDelateInstanceLocalFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz").put("id", "dummy");
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
        when(LocalCatImpl.catWebClient.delete(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(false);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        doAnswer(
                (Answer<AsyncResult<HttpResponse<Buffer>>>)
                        arg0 -> {
                            ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                                    .handle(httpResponseAsyncResult);
                            return null;
                        })
                .when(httpRequest)
                .send(any());
        catalogueService
                .deleteInstance(request, "xyz", localType)
                .onComplete(
                        ar -> {
                            if (ar.succeeded()) {
                                verify(httpRequest, times(1)).send(any());
                                verify(LocalCatImpl.catWebClient, times(1)).delete(anyInt(), anyString(), anyString());
                                testContext.failNow(ar.cause());
                            } else {
                                testContext.completeNow();
                            }
                        });
    }

    @Test
    @Description("test deleteItem when handler fails and type is local")
    public void testDelateInstanceFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz").put("id", "dummy");
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
        when(LocalCatImpl.catWebClient.delete(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(false);
        when(httpResponseAsyncResult.cause()).thenReturn(throwable);
        doAnswer(
                (Answer<AsyncResult<HttpResponse<Buffer>>>)
                        arg0 -> {
                            ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                                    .handle(httpResponseAsyncResult);
                            return null;
                        })
                .when(httpRequest)
                .send(any());
        catalogueService
                .deleteInstance(request, "xyz", localType)
                .onComplete(
                        ar -> {
                            if (ar.succeeded()) {
                                verify(httpRequest, times(1)).send(any());
                                verify(LocalCatImpl.catWebClient, times(1)).delete(anyInt(), anyString(), anyString());
                                testContext.failNow(ar.cause());
                            } else {
                                testContext.completeNow();
                            }
                        });
    }

    @Test
    @Description("test getItem when handler succeeds and type is local")
    public void testGetInstanceLocal(VertxTestContext testContext) {
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
        when(LocalCatImpl.catWebClient.get(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(buffer);
        doAnswer(
                (Answer<AsyncResult<HttpResponse<Buffer>>>)
                        arg0 -> {
                            ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                                    .handle(httpResponseAsyncResult);
                            return null;
                        })
                .when(httpRequest)
                .send(any());
        catalogueService
                .getInstance(id, localType)
                .onComplete(
                        ar -> {
                            if (ar.succeeded()) {
                                verify(httpRequest, times(1)).send(any());
                                verify(LocalCatImpl.catWebClient, times(1)).get(anyInt(), anyString(), anyString());
                                testContext.completeNow();
                            } else {
                                testContext.failNow(ar.cause());
                            }
                        });
    }

    @Test
    @Description("test getItem when handler fails and type is local")
    public void testGetInstanceLocalFailed(VertxTestContext testContext) {
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
        when(LocalCatImpl.catWebClient.get(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(false);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        doAnswer(
                (Answer<AsyncResult<HttpResponse<Buffer>>>)
                        arg0 -> {
                            ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                                    .handle(httpResponseAsyncResult);
                            return null;
                        })
                .when(httpRequest)
                .send(any());
        catalogueService
                .getInstance(id, localType)
                .onComplete(
                        ar -> {
                            if (ar.succeeded()) {
                                verify(httpRequest, times(1)).send(any());
                                verify(LocalCatImpl.catWebClient, times(1)).get(anyInt(), anyString(), anyString());
                                testContext.failNow(ar.cause());
                            } else {
                                testContext.completeNow();
                            }
                        });
    }

    @Test
    @Description("test getItem when handler fails and type is local")
    public void testGetInstanceFailed(VertxTestContext testContext) {
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
        when(LocalCatImpl.catWebClient.get(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(false);
        when(httpResponseAsyncResult.cause()).thenReturn(throwable);
        doAnswer(
                (Answer<AsyncResult<HttpResponse<Buffer>>>)
                        arg0 -> {
                            ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                                    .handle(httpResponseAsyncResult);
                            return null;
                        })
                .when(httpRequest)
                .send(any());
        catalogueService
                .getInstance(id, localType)
                .onComplete(
                        ar -> {
                            if (ar.succeeded()) {
                                verify(httpRequest, times(1)).send(any());
                                verify(LocalCatImpl.catWebClient, times(1)).get(anyInt(), anyString(), anyString());
                                testContext.failNow(ar.cause());
                            } else {
                                testContext.completeNow();
                            }
                        });
    }

}
