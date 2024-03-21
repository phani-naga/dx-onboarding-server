package iudx.onboarding.server.token;

import static iudx.onboarding.server.common.Constants.CATALOGUE_ADDRESS;
import static iudx.onboarding.server.common.Constants.TOKEN_ADDRESS;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import iudx.onboarding.server.catalogue.CatalogueVerticle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TokenVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LogManager.getLogger(CatalogueVerticle.class);
  private MessageConsumer<JsonObject> consumer;
  private ServiceBinder binder;
  private TokenService tokenService;

  @Override
  public void start() throws Exception {

    tokenService = new TokenServiceImpl(vertx, config());
    binder = new ServiceBinder(vertx);
    consumer = binder.setAddress(TOKEN_ADDRESS).register(TokenService.class, tokenService);

    LOGGER.info("Token Verticle deployed.");
  }

  @Override
  public void stop() {
    binder.unregister(consumer);
  }
}
