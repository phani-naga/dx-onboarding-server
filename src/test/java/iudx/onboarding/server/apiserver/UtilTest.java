package iudx.onboarding.server.apiserver;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import iudx.onboarding.server.apiserver.util.Util;
import iudx.onboarding.server.common.HttpStatusCode;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UtilTest {

  @Test
  public void testToUriFunction() throws URISyntaxException {
    Function<String, URI> toUriFunction = Util.toUriFunction;
    String uriValue = "http://example.com";
    URI expectedUri = new URI(uriValue);
    URI actualUri = toUriFunction.apply(uriValue);
    assertEquals(expectedUri, actualUri);
  }

  @Test
  public void testToList() {
    JsonArray jsonArray = new JsonArray().add("item1").add("item2").add("item3");
    List<String> expectedList = List.of("item1", "item2", "item3");
    List<String> actualList = Util.toList(jsonArray);
    assertEquals(expectedList, actualList);
  }

  @Test
  public void testToListWithNullArray() {
    JsonArray jsonArray = null;
    List<String> actualList = Util.toList(jsonArray);
    assertNull(actualList);
  }

  @Test
  public void testErrorResponse() {
    HttpStatusCode code = HttpStatusCode.BAD_REQUEST;

    String expectedResponse =
        new JsonObject()
            .put("type", code.getUrn())
            .put("title", code.getDescription())
            .put("detail", code.getDescription())
            .toString();
    String actualResponse = Util.errorResponse(code);
    assertEquals(expectedResponse, actualResponse);
  }
}
