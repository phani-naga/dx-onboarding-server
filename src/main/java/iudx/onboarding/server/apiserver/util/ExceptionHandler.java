package iudx.onboarding.server.apiserver.util;

import static iudx.onboarding.server.apiserver.util.Constants.HEADER_CONTENT_TYPE;
import static iudx.onboarding.server.apiserver.util.Constants.MIME_APPLICATION_JSON;

import io.vertx.core.Handler;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExceptionHandler implements Handler<RoutingContext> {

  private static final Logger LOGGER = LogManager.getLogger(ExceptionHandler.class);

  @Override
  public void handle(RoutingContext routingContext) {

    Throwable failure = routingContext.failure();
    if (failure instanceof DecodeException) {
      handleDecodeException(routingContext);
      return;
    } else if (failure instanceof ClassCastException) {
      handleClassCastException(routingContext);
      return;
    } else {
      routingContext.response()
          .setStatusCode(400)
          .putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
          .end(new RespBuilder()
              .withType("urn:dx:cat:InvalidSyntax")
              .withTitle("Invalid Syntax")
              .withDetail(failure.getMessage())
              .getResponse());
    }
  }


  /**
   * Handles the JsonDecode Exception.
   *
   * @param routingContext for handling HTTP Request
   */
  public void handleDecodeException(RoutingContext routingContext) {

    LOGGER.error("Error: Invalid Json payload; " + routingContext.failure().getLocalizedMessage());
    String response = "";

    response =
        new RespBuilder()
            .withType("urn:dx:cat:InvalidSchema")
            .withTitle("Invalid Schema")
            .withDetail("Invalid Json payload")
            .getResponse();

    routingContext
        .response()
        .setStatusCode(400)
        .putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
        .end(response);
  }

  /**
   * Handles the exception from casting a object to different object.
   *
   * @param routingContext the routing context of the request
   */
  public void handleClassCastException(RoutingContext routingContext) {

    LOGGER.error("Error: Invalid request payload; "
        + routingContext.failure().getLocalizedMessage());

    routingContext.response()
        .setStatusCode(400)
        .putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
        .end(
            new JsonObject()
                .put("type", "urn:dx:cat:Fail")
                .put("title", "Invalid payload")
                .encode());

    routingContext.next();
  }
}
