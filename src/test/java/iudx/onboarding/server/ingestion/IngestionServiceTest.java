package iudx.onboarding.server.ingestion;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(VertxExtension.class)
public class IngestionServiceTest {
  @Mock
  HttpRequest<Buffer> httpRequest;
  @Mock
  AsyncResult<HttpResponse<Buffer>> httpResponseAsyncResult;
  @Mock
  private HttpResponse<Buffer> httpResponse;

  @Mock
  private Vertx vertx;

  @Mock
  private WebClient webClient;
  @Mock Buffer buffer;

   IngestionServiceImpl ingestionService;

  @BeforeEach
  public void setUp() {
    JsonObject config = new JsonObject()
      .put("resourceServerPort", 8080)
      .put("resourceServerBasePath", "/ngsi-ld/v1");

    IngestionServiceImpl.rsWebClient = mock(WebClient.class);
    ingestionService = new IngestionServiceImpl(vertx, config);
  }

  @Test
  @Description("register adaptor success")
  public void testRegisterAdapterSuccess(VertxTestContext testContext) {
    JsonObject response = new JsonObject();
    JsonArray jsonArray = new JsonArray().add(response);
    response.put("results", jsonArray);
    lenient().when(IngestionServiceImpl.rsWebClient.post(anyInt(),anyString(),anyString())).thenReturn(httpRequest);
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);

    when(httpResponseAsyncResult.succeeded()).thenReturn(true);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    when(httpResponse.body()).thenReturn(buffer);
    when(buffer.toJsonObject()).thenReturn(response);
   // when(buffer.)
    when(httpResponse.statusCode()).thenReturn(201);

    doAnswer(
      (Answer<Void>)
              invocation -> {
                ((Handler<AsyncResult<HttpResponse<Buffer>>>) invocation.getArgument(1))
                  .handle(httpResponseAsyncResult);
                return null;
              })
      .when(httpRequest)
      .sendJsonObject(any(JsonObject.class), any());
    ingestionService.registerAdapter("abc", "abc","abc")
      .onComplete(
        handler -> {
          if(handler.succeeded()) {
            verify(httpRequest, times(1)).sendJsonObject(any(JsonObject.class),any());
            testContext.completeNow();
          } else {
            testContext.failNow(handler.cause());
          }

        }
      );
  }
  @Test
  @Description("register adaptor failure")
  public void testRegisterAdapterFailure(VertxTestContext testContext) {
    JsonObject response = new JsonObject();
    JsonArray jsonArray = new JsonArray().add(response);
    response.put("results", jsonArray);
    lenient().when(IngestionServiceImpl.rsWebClient.post(anyInt(),anyString(),anyString())).thenReturn(httpRequest);
    when(httpRequest.putHeader(anyString(), anyString())).thenReturn(httpRequest);

    when(httpResponseAsyncResult.succeeded()).thenReturn(false);
    when(httpResponseAsyncResult.result()).thenReturn(httpResponse);
    when(httpResponse.bodyAsString()).thenReturn("failure message");

    doAnswer(
      (Answer<Void>)
        invocation -> {
          ((Handler<AsyncResult<HttpResponse<Buffer>>>) invocation.getArgument(1))
            .handle(httpResponseAsyncResult);
          return null;
        })
      .when(httpRequest)
      .sendJsonObject(any(JsonObject.class), any());
    ingestionService.registerAdapter("abc", "abc","abc")
      .onComplete(
        handler -> {
          if(handler.succeeded()) {
            verify(httpRequest, times(1)).sendJsonObject(any(JsonObject.class),any());
            testContext.failNow("failed");
          } else {
            assertEquals("failure message", handler.cause().getMessage());
            testContext.completeNow();
          }

        }
      );
  }

  @Test
  @Description("adapter on success")
  void testUnregisteredAdapterSuccess(Vertx vertx, VertxTestContext testContext) {

    WebClient mockedWebClient = Mockito.mock(WebClient.class);
    IngestionServiceImpl.rsWebClient = mockedWebClient;

    HttpRequest<Buffer> httpRequest = mock(HttpRequest.class);
    doReturn(httpRequest).when(mockedWebClient).delete(anyInt(), anyString(), anyString());
    doReturn(httpRequest).when(httpRequest).putHeader(anyString(), anyString());

    HttpResponse<Buffer> response = mock(HttpResponse.class);
    when(response.statusCode()).thenReturn(200);

    doReturn(Future.succeededFuture(response)).when(httpRequest).send();

    JsonObject jsonObject =
      new JsonObject().put("resourceServerPort", 124).put("resourceServerBasePath", "path");

    IngestionServiceImpl ingestionService = new IngestionServiceImpl(vertx, jsonObject);
    Future<JsonObject> futureResult =
      ingestionService.unregisteredAdapter("resourceServerUrl", "id", "token");

    futureResult.onComplete(
      result -> {
        if (result.succeeded()) {
          testContext.completeNow();
        } else {
          testContext.failNow(result.cause().getMessage());
        }
      });
  }

  @Test
  @Description("adapter on failure")
  void testUnregisteredAdapterFailure(Vertx vertx, VertxTestContext testContext) {

    WebClient mockedWebClient = Mockito.mock(WebClient.class);
    IngestionServiceImpl.rsWebClient = mockedWebClient;

    HttpRequest<Buffer> httpRequest = mock(HttpRequest.class);
    doReturn(httpRequest).when(mockedWebClient).delete(anyInt(), anyString(), anyString());
    doReturn(httpRequest).when(httpRequest).putHeader(anyString(), anyString());

    HttpResponse<Buffer> response = mock(HttpResponse.class);
    when(response.statusCode()).thenReturn(400);

    doReturn(Future.succeededFuture(response)).when(httpRequest).send();

    JsonObject jsonObject =
      new JsonObject().put("resourceServerPort", 124).put("resourceServerBasePath", "path");

    IngestionServiceImpl ingestionService = new IngestionServiceImpl(vertx, jsonObject);
    Future<JsonObject> futureResult =
      ingestionService.unregisteredAdapter("resourceServerUrl", "id", "token");

    futureResult.onComplete(
      result -> {
        if (result.succeeded()) {
          testContext.failNow("failed");
        } else {
          testContext.completeNow();
        }
      });
  }
}
