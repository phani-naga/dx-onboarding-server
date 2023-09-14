package iudx.onboarding.server.catalogue;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import iudx.onboarding.server.catalogue.service.LocalCatImpl;
import iudx.onboarding.server.common.CatalogueType;
import iudx.onboarding.server.ingestion.IngestionService;
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

    IngestionService ingestionService = mock(IngestionService.class);

    JsonObject config =
        new JsonObject()
            .put("centralCatServerHost", "localhost")
            .put("centralCatServerPort", 8080)
            .put("dxCatalogueBasePath", "/api")
            .put("localCatServerHost", "localhost")
            .put("localCatServerPort", 8080);
      LocalCatImpl.catWebClient = mock(WebClient.class);
      lenient().when(LocalCatImpl.catWebClient.post(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
      lenient().when(LocalCatImpl.catWebClient.put(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
      lenient().when(LocalCatImpl.catWebClient.delete(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
      lenient().when(LocalCatImpl.catWebClient.get(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
      catalogueService =
        new CatalogueServiceImpl(Vertx.vertx(), mock(TokenService.class), null, ingestionService,config);
  }

  @Test
  @Description("test createItem when handler succeeds and type is local")
  public void testCreateItemLocal(VertxTestContext testContext) {

    JsonObject request = new JsonObject().put("token", "xyz");
    CatalogueType localType = CatalogueType.LOCAL;
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
  public void testDeleteItemLocal(VertxTestContext testContext) {

    JsonObject request = new JsonObject().put("token", "xyz").put("id", "dummy");
    CatalogueType localType = CatalogueType.LOCAL;
    String id = "dummy";
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
  public void testDeleteItemLocalFailed(VertxTestContext testContext) {

    JsonObject request = new JsonObject().put("token", "xyz").put("id", "dummy");
    CatalogueType localType = CatalogueType.LOCAL;
    String id = "dummy";
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
  public void testDeleteItemFailed(VertxTestContext testContext) {

    JsonObject request = new JsonObject().put("token", "xyz").put("id", "dummy");
    CatalogueType localType = CatalogueType.LOCAL;
    String id = "dummy";
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
    @Description("test createInstance when handler succeeds and type is local")
    public void testCreateInstanceLocal(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
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
    @Description("test createInstance when handler fails and type is local")
    public void testCreateInstanceLocalFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
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
    @Description("test createInstance when handler fails and type is local")
    public void testCreateInstanceFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
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
    @Description("test updateInstance when handler succeeds and type is local")
    public void testUpdateInstanceLocal(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
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
    @Description("test updateInstance when handler fails and type is local")
    public void testUpdateInstanceFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
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
    @Description("test updateInstance when handler fails and type is local")
    public void testUpdateInstanceLocalFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
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
    @Description("test deleteInstance when handler succeeds and type is local")
    public void testDeleteInstanceLocal(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz").put("id", "dummy");
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
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
    @Description("test deleteInstance when handler fails and type is local")
    public void testDeleteInstanceLocalFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz").put("id", "dummy");
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
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
    @Description("test deleteInstance when handler fails and type is local")
    public void testDeleteInstanceFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz").put("id", "dummy");
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
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
    @Description("test getInstance when handler succeeds and type is local")
    public void testGetInstanceLocal(VertxTestContext testContext) {
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
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
    @Description("test getInstance when handler fails and type is local")
    public void testGetInstanceLocalFailed(VertxTestContext testContext) {
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
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
    @Description("test getInstance when handler fails and type is local")
    public void testGetInstanceFailed(VertxTestContext testContext) {
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
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

    @Test
    @Description("test createDomain when handler succeeds and type is local")
    public void testCreateDomainLocal(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
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
                .createDomain(request, "xyz", localType)
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
    @Description("test createDomain when handler fails and type is local")
    public void testCreateDomainLocalFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
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
                .createDomain(request, "xyz", localType)
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
    @Description("test createDomain when handler fails and type is local")
    public void testCreateDomainFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
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
                .createDomain(request, "xyz", localType)
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
    @Description("test updateDoamin when handler succeeds and type is local")
    public void testUpdateDomainLocal(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
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
                .updateDomain("abc",request, "xyz", localType)
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
    @Description("test updateDomain when handler fails and type is local")
    public void testUpdateDomainFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
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
                .updateDomain("abc", request, "xyz", localType)
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
    @Description("test updateDomain when handler fails and type is local")
    public void testUpdateDomainLocalFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz");
        CatalogueType localType = CatalogueType.LOCAL;
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
                .updateDomain("abc", request, "xyz", localType)
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
    @Description("test deleteDomain when handler succeeds and type is local")
    public void testDeleteDomainLocal(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz").put("id", "dummy");
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
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
                .deleteDomain(request, "xyz", localType)
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
    @Description("test delete domain when handler fails and type is local")
    public void testDeleteDomainLocalFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz").put("id", "dummy");
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
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
                .deleteDomain(request, "xyz", localType)
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
    @Description("test deleteDomain when handler fails and type is local")
    public void testDeleteDomainFailed(VertxTestContext testContext) {

        JsonObject request = new JsonObject().put("token", "xyz").put("id", "dummy");
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
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
                .deleteDomain(request, "xyz", localType)
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
    @Description("test getDomain when handler succeeds and type is local")
    public void testGetDomainLocal(VertxTestContext testContext) {
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
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
                .getDomain(id, localType)
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
    @Description("test getDomain when handler fails and type is local")
    public void testGetDomainLocalFailed(VertxTestContext testContext) {
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
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
                .getDomain(id, localType)
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
    @Description("test getDomain when handler fails and type is local")
    public void testGetDomainFailed(VertxTestContext testContext) {
        CatalogueType localType = CatalogueType.LOCAL;
        String id = "dummy";
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
                .getDomain(id, localType)
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
