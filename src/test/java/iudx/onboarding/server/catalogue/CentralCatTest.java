package iudx.onboarding.server.catalogue;

import dev.failsafe.RetryPolicyBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import iudx.onboarding.server.catalogue.service.CentralCatImpl;
import iudx.onboarding.server.catalogue.service.LocalCatImpl;
import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(VertxExtension.class)
public class CentralCatTest {
  @Mock  WebClient catWebClient;
  @Mock private Vertx vertx;

  @Mock private HttpRequest<Buffer> httpRequest;

  @Mock private HttpResponse<Buffer> httpResponse;
  @Mock AsyncResult<HttpResponse<Buffer>> httpResponseAsyncResult;
  @Mock Buffer buffer;
  CentralCatImpl centralCat;
  @BeforeEach
  void setUp() {
    JsonObject config =
        new JsonObject()
            .put("centralCatServerHost", "localhost")
            .put("centralCatServerPort", 8080)
            .put("dxCatalogueBasePath", "/api");
    centralCat = new CentralCatImpl(vertx, config);
    CentralCatImpl.catWebClient = mock(WebClient.class);
    lenient().when(CentralCatImpl.catWebClient.post(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
    lenient().when(CentralCatImpl.catWebClient.put(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
    lenient().when(CentralCatImpl.catWebClient.delete(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
    lenient().when(CentralCatImpl.catWebClient.get(anyInt(), anyString(), anyString())).thenReturn(httpRequest);

  }

    @Test
    @Description("test createItem method in central when method succeeds")
    public void createItem(Vertx vertx, VertxTestContext testContext) {
        JsonObject request = new JsonObject();
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(201);
        when(httpResponse.body()).thenReturn(Buffer.buffer("{\"result\": \"success\"}")); // Adjust this response JSON as needed
        doAnswer(
                (Answer<Void>)
                        invocation -> {
                            ((Handler<AsyncResult<HttpResponse<Buffer>>>) invocation.getArgument(1))
                                    .handle(httpResponseAsyncResult);
                            return null;
                        })
                .when(httpRequest)
                .sendJsonObject(any(JsonObject.class), any());

      centralCat.createItem(request, "abc")
              .onComplete(
                handler -> {
                    if (handler.succeeded()) {
                        verify(httpRequest, times(1)).sendJsonObject(any(JsonObject.class), any());
                        verify(CentralCatImpl.catWebClient, times(1)).post(anyInt(), anyString(), anyString());
                        testContext.completeNow();
                    } else {
                        testContext.failNow(handler.cause());
                    }
                });
    }

  @Test
  @Description("test createItem method in central when method fails")
  public void createItemFailed(VertxTestContext testContext) {
    JsonObject request = new JsonObject();
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    when(httpResponse.statusCode()).thenReturn(202);
    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {

                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpRequest)
        .sendJsonObject(any(), any());
    centralCat
        .createItem(request, "abc")
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                verify(httpRequest, times(1)).sendJsonObject(any(), any());
                verify(CentralCatImpl.catWebClient, times(1)).post(anyInt(), anyString(), anyString());
                testContext.failNow("fail");
              } else {
                testContext.completeNow();
              }
            });
  }

  @Test
  @Description("test updateItem method in central when method succeeds")
  public void updateItem(VertxTestContext testContext) {
    JsonObject request = new JsonObject();
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    when(httpResponse.statusCode()).thenReturn(200);
    when(httpResponse.body()).thenReturn(buffer);
    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {

                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpRequest)
        .sendJsonObject(any(), any());
    centralCat
        .updateItem(request, "abc")
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                verify(httpRequest, times(1)).sendJsonObject(any(), any());
                verify(CentralCatImpl.catWebClient, times(1)).put(anyInt(), anyString(), anyString());
                testContext.completeNow();
              } else {
                testContext.failNow("fail");
              }
            });
  }

  @Test
  @Description("test updateItem method in central when method fails")
  public void updateItemFailed(VertxTestContext testContext) {
    JsonObject request = new JsonObject();
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    when(httpResponse.body()).thenReturn(buffer);
    when(httpResponse.statusCode()).thenReturn(201);
    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {

                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpRequest)
        .sendJsonObject(any(), any());
    centralCat
        .updateItem(request, "abc")
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                verify(httpRequest, times(1)).sendJsonObject(any(), any());
                verify(CentralCatImpl.catWebClient, times(1)).put(anyInt(), anyString(), anyString());
                testContext.failNow("fail");

              } else {
                testContext.completeNow();
              }
            });
  }

  @Test
  @Description("test deleteItem method in central when method succeeds")
  public void deleteItem(VertxTestContext testContext) {
    String id = "dummy";
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    when(httpResponse.statusCode()).thenReturn(200);
    when(httpResponse.body()).thenReturn(buffer);
    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {

                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpRequest)
        .send(any());
    centralCat
        .deleteItem(id, "abc")
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                verify(httpRequest, times(1)).send(any());
                verify(CentralCatImpl.catWebClient, times(1)).delete(anyInt(), anyString(), anyString());
                testContext.completeNow();

              } else {
                testContext.failNow("fail");
              }
            });
  }

  @Test
  @Description("test deleteItem method in central when method fails")
  public void deleteItemFailed(VertxTestContext testContext) {
    String id = "dummy";
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    when(httpResponse.statusCode()).thenReturn(201);
    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {

                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpRequest)
        .send(any());
    centralCat
        .deleteItem(id, "abc")
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                verify(CentralCatImpl.catWebClient, times(1)).delete(anyInt(), anyString(), anyString());
                verify(httpRequest, times(1)).send(any());
                testContext.failNow("fail");


              } else {
                testContext.completeNow();
              }
            });
  }

  @Test
  @Description("test getItem method in central when method succeeds")
  public void getItem(VertxTestContext testContext) {
    String id = "dummy";
    when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    when(httpResponse.statusCode()).thenReturn(200);
    when(httpResponse.body()).thenReturn(buffer);
    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {

                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpRequest)
        .send(any());
    centralCat
        .getItem(id)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                verify(httpRequest, times(1)).send(any());
                verify(CentralCatImpl.catWebClient, times(1)).get(anyInt(),anyString(),anyString());
                testContext.completeNow();

              } else {
                testContext.failNow("fail");
              }
            });
  }

  @Test
  @Description("test getItem method in central when method fails")
  public void getItemFailed(VertxTestContext testContext) {
    String id = "dummy";
    when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    when(httpResponse.statusCode()).thenReturn(201);
    doAnswer(
            new Answer<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                  throws Throwable {

                ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                    .handle(httpResponseAsyncResult);
                return null;
              }
            })
        .when(httpRequest)
        .send(any());
    centralCat
        .getItem(id)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                verify(httpRequest, times(1)).send(any());
                verify(CentralCatImpl.catWebClient, times(1)).get(anyInt(),anyString(),anyString());
                testContext.failNow("fail");

              } else {
                testContext.completeNow();
              }
            });
  }

    @Test
    @Description("test createInstance method in central when method succeeds")
    public void createInstance(Vertx vertx, VertxTestContext testContext) {
        JsonObject request = new JsonObject().put("id", "id");
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
      when(httpRequest.setQueryParam(anyString(), anyString())).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(201);
        when(httpResponse.body()).thenReturn(Buffer.buffer("{\"result\": \"success\"}")); // Adjust this response JSON as needed

        doAnswer(
                (Answer<Void>)
                        invocation -> {
                            ((Handler<AsyncResult<HttpResponse<Buffer>>>) invocation.getArgument(1))
                                    .handle(httpResponseAsyncResult);
                            return null;
                        })
                .when(httpRequest)
                .sendJsonObject(any(JsonObject.class), any());

        centralCat.createInstance(request, "/internal/ui/instance","abc")
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                                verify(httpRequest, times(1)).sendJsonObject(any(JsonObject.class), any());
                                verify(CentralCatImpl.catWebClient, times(1)).post(anyInt(),anyString(),anyString());
                                testContext.completeNow();
                            } else {
                                testContext.failNow(handler.cause());
                            }
                        });
    }

    @Test
    @Description("test createInstance method in central when method fails")
    public void createInstanceFailed(VertxTestContext testContext) {
        JsonObject request = new JsonObject().put("id", "id");
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
      when(httpRequest.setQueryParam(anyString(), anyString())).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(202);
        doAnswer(
                new Answer<AsyncResult<HttpResponse<Buffer>>>() {
                    @Override
                    public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                            throws Throwable {

                        ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                                .handle(httpResponseAsyncResult);
                        return null;
                    }
                })
                .when(httpRequest)
                .sendJsonObject(any(), any());
      centralCat.createInstance(request, "/internal/ui/instance","abc")
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                                verify(httpRequest, times(1)).sendJsonObject(any(), any());
                                verify(CentralCatImpl.catWebClient, times(1)).post(anyInt(),anyString(),anyString());
                                testContext.failNow("fail");
                            } else {
                                testContext.completeNow();
                            }
                        });
    }

    @Test
    @Description("test updateInstance method in central when method succeeds")
    public void updateInstance(VertxTestContext testContext) {
        JsonObject request = new JsonObject();
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam("id", "abc")).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(buffer);
        doAnswer(
                new Answer<AsyncResult<HttpResponse<Buffer>>>() {
                    @Override
                    public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                            throws Throwable {

                        ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                                .handle(httpResponseAsyncResult);
                        return null;
                    }
                })
                .when(httpRequest)
                .sendJsonObject(any(), any());
        centralCat
                .updateInstance("abc", request, "")
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                                verify(httpRequest, times(1)).sendJsonObject(any(), any());
                                verify(CentralCatImpl.catWebClient, times(1)).put(anyInt(),anyString(),anyString());
                                testContext.completeNow();
                            } else {
                                testContext.failNow("fail");
                            }
                        });
    }

    @Test
    @Description("test updateInstance method in central when method fails")
    public void updateInstanceFailed(VertxTestContext testContext) {
        JsonObject request = new JsonObject();
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam("id","abc")).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(buffer);
        when(httpResponse.statusCode()).thenReturn(201);
        doAnswer(
                new Answer<AsyncResult<HttpResponse<Buffer>>>() {
                    @Override
                    public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                            throws Throwable {

                        ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                                .handle(httpResponseAsyncResult);
                        return null;
                    }
                })
                .when(httpRequest)
                .sendJsonObject(any(), any());
        centralCat
                .updateInstance("abc", request, "")
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                                verify(httpRequest, times(1)).sendJsonObject(any(), any());
                                verify(CentralCatImpl.catWebClient, times(1)).put(anyInt(),anyString(),anyString());
                                testContext.failNow("fail");

                            } else {
                                testContext.completeNow();
                            }
                        });
    }

    @Test
    @Description("test deleteInstance method in central when method succeeds")
    public void deleteInstance(VertxTestContext testContext) {
        String id = "dummy";
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(buffer);
        doAnswer(
                new Answer<AsyncResult<HttpResponse<Buffer>>>() {
                    @Override
                    public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                            throws Throwable {

                        ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                                .handle(httpResponseAsyncResult);
                        return null;
                    }
                })
                .when(httpRequest)
                .send(any());
        centralCat
                .deleteInstance(id, "/internal/ui/instance", "")
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                                verify(httpRequest, times(1)).send(any());
                                verify(CentralCatImpl.catWebClient, times(1)).delete(anyInt(),anyString(),anyString());
                                testContext.completeNow();

                            } else {
                                testContext.failNow("fail");
                            }
                        });
    }

    @Test
    @Description("test deleteInstance method in central when method fails")
    public void deleteInstanceFailed(VertxTestContext testContext) {
        String id = "dummy";
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(201);
        doAnswer(
                new Answer<AsyncResult<HttpResponse<Buffer>>>() {
                    @Override
                    public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                            throws Throwable {

                        ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                                .handle(httpResponseAsyncResult);
                        return null;
                    }
                })
                .when(httpRequest)
                .send(any());
        centralCat
            .deleteInstance(id, "/internal/ui/instance", "")
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                                verify(httpRequest, times(1)).send(any());
                                verify(CentralCatImpl.catWebClient, times(1)).delete(anyInt(),anyString(),anyString());
                                testContext.failNow("fail");
                            } else {
                                testContext.completeNow();
                            }
                        });
    }

    @Test
    @Description("test getInstance method in central when method succeeds")
    public void getInstance(VertxTestContext testContext) {
        String id = "dummy";
        when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(buffer);
        doAnswer(
                new Answer<AsyncResult<HttpResponse<Buffer>>>() {
                    @Override
                    public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                            throws Throwable {

                        ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                                .handle(httpResponseAsyncResult);
                        return null;
                    }
                })
                .when(httpRequest)
                .send(any());
        centralCat
                .getInstance(id, "/internal/ui/instance")
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                                verify(httpRequest, times(1)).send(any());
                                verify(CentralCatImpl.catWebClient, times(1)).get(anyInt(),anyString(),anyString());
                                testContext.completeNow();

                            } else {
                                testContext.failNow("fail");
                            }
                        });
    }

    @Test
    @Description("test getInstance method in central when method fails")
    public void getInstanceFailed(VertxTestContext testContext) {
        String id = "dummy";
        when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(201);
        doAnswer(
                new Answer<AsyncResult<HttpResponse<Buffer>>>() {
                    @Override
                    public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                            throws Throwable {

                        ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                                .handle(httpResponseAsyncResult);
                        return null;
                    }
                })
                .when(httpRequest)
                .send(any());
        centralCat
                .getInstance(id, "/internal/ui/instance")
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                                verify(httpRequest, times(1)).send(any());
                                verify(CentralCatImpl.catWebClient, times(1)).get(anyInt(),anyString(),anyString());
                                testContext.failNow("fail");

                            } else {
                                testContext.completeNow();
                            }
                        });
    }

    @Test
    @Description("test createDomain method in central when method succeeds")
    public void createDomain(Vertx vertx, VertxTestContext testContext) {
        JsonObject request = new JsonObject();
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(201);
        when(httpResponse.body()).thenReturn(Buffer.buffer("{\"result\": \"success\"}")); // Adjust this response JSON as needed

        doAnswer(
                (Answer<Void>)
                        invocation -> {
                            ((Handler<AsyncResult<HttpResponse<Buffer>>>) invocation.getArgument(1))
                                    .handle(httpResponseAsyncResult);
                            return null;
                        })
                .when(httpRequest)
                .sendJsonObject(any(JsonObject.class), any());

        centralCat.createDomain(request, "abc")
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                                verify(httpRequest, times(1)).sendJsonObject(any(JsonObject.class), any());
                                verify(CentralCatImpl.catWebClient, times(1)).post(anyInt(),anyString(),anyString());
                                testContext.completeNow();
                            } else {
                                testContext.failNow(handler.cause());
                            }
                        });
    }

    @Test
    @Description("test createDomain method in central when method fails")
    public void createDoaminFailed(VertxTestContext testContext) {
        JsonObject request = new JsonObject();
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(202);
        doAnswer(
                new Answer<AsyncResult<HttpResponse<Buffer>>>() {
                    @Override
                    public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                            throws Throwable {

                        ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                                .handle(httpResponseAsyncResult);
                        return null;
                    }
                })
                .when(httpRequest)
                .sendJsonObject(any(), any());
        centralCat
                .createDomain(request, "abc")
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                                verify(httpRequest, times(1)).sendJsonObject(any(), any());
                                verify(CentralCatImpl.catWebClient, times(1)).post(anyInt(),anyString(),anyString());
                                testContext.failNow("fail");
                            } else {
                                testContext.completeNow();
                            }
                        });
    }

    @Test
    @Description("test updateDomain method in central when method succeeds")
    public void updateDomain(VertxTestContext testContext) {
        JsonObject request = new JsonObject();
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam("id", "abc")).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(buffer);
        doAnswer(
                new Answer<AsyncResult<HttpResponse<Buffer>>>() {
                    @Override
                    public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                            throws Throwable {

                        ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                                .handle(httpResponseAsyncResult);
                        return null;
                    }
                })
                .when(httpRequest)
                .sendJsonObject(any(), any());
        centralCat
                .updateDomain("abc", request, "")
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                                verify(httpRequest, times(1)).sendJsonObject(any(), any());
                                verify(CentralCatImpl.catWebClient, times(1)).put(anyInt(),anyString(),anyString());
                                testContext.completeNow();
                            } else {
                                testContext.failNow("fail");
                            }
                        });
    }

    @Test
    @Description("test updateDomain method in central when method fails")
    public void updateDomainFailed(VertxTestContext testContext) {
        JsonObject request = new JsonObject();
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam("id","abc")).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(buffer);
        when(httpResponse.statusCode()).thenReturn(201);
        doAnswer(
                new Answer<AsyncResult<HttpResponse<Buffer>>>() {
                    @Override
                    public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                            throws Throwable {

                        ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(1))
                                .handle(httpResponseAsyncResult);
                        return null;
                    }
                })
                .when(httpRequest)
                .sendJsonObject(any(), any());
        centralCat
                .updateDomain("abc", request, "")
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                                verify(httpRequest, times(1)).sendJsonObject(any(), any());
                                verify(CentralCatImpl.catWebClient, times(1)).put(anyInt(),anyString(),anyString());
                                testContext.failNow("fail");

                            } else {
                                testContext.completeNow();
                            }
                        });
    }

    @Test
    @Description("test deleteDomain method in central when method succeeds")
    public void deleteDomain(VertxTestContext testContext) {
        String id = "dummy";
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(buffer);
        doAnswer(
                new Answer<AsyncResult<HttpResponse<Buffer>>>() {
                    @Override
                    public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                            throws Throwable {

                        ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                                .handle(httpResponseAsyncResult);
                        return null;
                    }
                })
                .when(httpRequest)
                .send(any());
        centralCat
                .deleteDomain(id, "")
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                                verify(httpRequest, times(1)).send(any());
                                verify(CentralCatImpl.catWebClient, times(1)).delete(anyInt(),anyString(),anyString());
                                testContext.completeNow();

                            } else {
                                testContext.failNow("fail");
                            }
                        });
    }

    @Test
    @Description("test deleteDomain method in central when method fails")
    public void deleteDomainFailed(VertxTestContext testContext) {
        String id = "dummy";
        when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
        when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(201);
        doAnswer(
                new Answer<AsyncResult<HttpResponse<Buffer>>>() {
                    @Override
                    public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                            throws Throwable {

                        ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                                .handle(httpResponseAsyncResult);
                        return null;
                    }
                })
                .when(httpRequest)
                .send(any());
        centralCat
                .deleteDomain(id, "")
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                                verify(httpRequest, times(1)).send(any());
                                verify(CentralCatImpl.catWebClient, times(1)).delete(anyInt(),anyString(),anyString());
                                testContext.failNow("fail");
                            } else {
                                testContext.completeNow();
                            }
                        });
    }

    @Test
    @Description("test getDomain method in central when method succeeds")
    public void getDomain(VertxTestContext testContext) {
        String id = "dummy";
        when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(buffer);
        doAnswer(
                new Answer<AsyncResult<HttpResponse<Buffer>>>() {
                    @Override
                    public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                            throws Throwable {

                        ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                                .handle(httpResponseAsyncResult);
                        return null;
                    }
                })
                .when(httpRequest)
                .send(any());
        centralCat
                .getDomain(id)
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                                verify(httpRequest, times(1)).send(any());
                                verify(CentralCatImpl.catWebClient, times(1)).get(anyInt(),anyString(),anyString());
                                testContext.completeNow();

                            } else {
                                testContext.failNow("fail");
                            }
                        });
    }

    @Test
    @Description("test getDomain method in central when method fails")
    public void getDomainFailed(VertxTestContext testContext) {
        String id = "dummy";
        when(httpRequest.addQueryParam("id", id)).thenReturn(httpRequest);
        when(httpResponseAsyncResult.succeeded()).thenReturn(true);
        when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(201);
        doAnswer(
                new Answer<AsyncResult<HttpResponse<Buffer>>>() {
                    @Override
                    public AsyncResult<HttpResponse<Buffer>> answer(InvocationOnMock arg0)
                            throws Throwable {

                        ((Handler<AsyncResult<HttpResponse<Buffer>>>) arg0.getArgument(0))
                                .handle(httpResponseAsyncResult);
                        return null;
                    }
                })
                .when(httpRequest)
                .send(any());
        centralCat
                .getDomain(id)
                .onComplete(
                        handler -> {
                            if (handler.succeeded()) {
                                verify(httpRequest, times(1)).send(any());
                                verify(CentralCatImpl.catWebClient, times(1)).get(anyInt(),anyString(),anyString());
                                testContext.failNow("fail");

                            } else {
                                testContext.completeNow();
                            }
                        });
    }
}
