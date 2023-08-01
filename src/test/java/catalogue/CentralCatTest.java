package catalogue;

import io.vertx.core.AsyncResult;
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
import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
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
  @Mock private WebClient client;
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

    centralCat = new CentralCatImpl(vertx, config, client);
  }

  @Test
  @Description("test createItem method in central when method succeeds")
  public void createItem(VertxTestContext testContext) {
    JsonObject request = new JsonObject();
    String token = "";
    when(client.post(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    when(httpResponse.statusCode()).thenReturn(201);
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
    centralCat
        .createItem(request, token)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                verify(httpRequest, times(1)).sendJsonObject(any(), any());

                testContext.completeNow();
              } else {
                testContext.failNow("fail");
              }
            });
  }

  @Test
  @Description("test createItem method in central when method fails")
  public void createItemFailed(VertxTestContext testContext) {
    JsonObject request = new JsonObject();
    String token = "";
    when(client.post(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
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
        .createItem(request, token)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                verify(httpRequest, times(1)).sendJsonObject(any(), any());

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
    String token = "";
    when(client.put(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
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
        .updateItem(request, token)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                verify(httpRequest, times(1)).sendJsonObject(any(), any());

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
    String token = "";
    when(client.put(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);
    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
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
        .updateItem(request, token)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                verify(httpRequest, times(1)).sendJsonObject(any(), any());

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
    String token = "";
    when(client.delete(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
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
        .deleteItem(id, token)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                verify(httpRequest, times(1)).send(any());

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
    String token = "";
    when(client.delete(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
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
        .deleteItem(id, token)
        .onComplete(
            handler -> {
              if (handler.succeeded()) {
                testContext.failNow("fail");
                verify(httpRequest, times(1)).send(any());

              } else {
                testContext.completeNow();
              }
            });
  }

  @Test
  @Description("test getItem method in central when method succeeds")
  public void getItem(VertxTestContext testContext) {
    String id = "dummy";
    String token = "";
    when(client.get(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
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
    String token = "";
    when(client.get(anyInt(), anyString(), anyString())).thenReturn(httpRequest);
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

                testContext.failNow("fail");

              } else {
                testContext.completeNow();
              }
            });
  }
}
