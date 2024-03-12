package iudx.onboarding.server.catalogue;

import static iudx.onboarding.server.common.Constants.*;

import dev.failsafe.RetryPolicy;
import dev.failsafe.RetryPolicyBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import iudx.onboarding.server.apiserver.exceptions.DxRuntimeException;
import iudx.onboarding.server.ingestion.IngestionService;
import iudx.onboarding.server.token.TokenService;
import java.net.UnknownHostException;
import java.time.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CatalogueVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LogManager.getLogger(CatalogueVerticle.class);
  private MessageConsumer<JsonObject> consumer;
  private ServiceBinder binder;
  private CatalogueUtilService catalogueUtilService;
  private TokenService tokenService;


  @Override
  public void start() throws Exception {

    tokenService = TokenService.createProxy(vertx, TOKEN_ADDRESS);

    RetryPolicyBuilder<Object> retryPolicyBuilder = RetryPolicy.builder()
        .handle(DxRuntimeException.class)
        .handle(UnknownHostException.class)
        .abortOn(e -> e instanceof UnknownHostException)
        .withBackoff(Duration.ofSeconds(5), Duration.ofSeconds(7), 1.1)
        .withMaxAttempts(3)
        .onRetry(retryListener -> LOGGER.error("Operation failed... retrying"));

    catalogueUtilService = new CatalogueServiceImpl(vertx, tokenService, retryPolicyBuilder, config());
    binder = new ServiceBinder(vertx);
    consumer = binder.setAddress(CATALOGUE_ADDRESS).register(CatalogueUtilService.class, catalogueUtilService);

    LOGGER.info("Catalogue Verticle deployed.");
  }

  @Override
  public void stop() {
    binder.unregister(consumer);
  }
}
