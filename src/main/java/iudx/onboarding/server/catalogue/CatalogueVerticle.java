package iudx.onboarding.server.catalogue;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.serviceproxy.ServiceBinder;
import iudx.onboarding.server.token.TokenService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static iudx.onboarding.server.common.Constants.CATALOGUE_ADDRESS;
import static iudx.onboarding.server.common.Constants.TOKEN_ADDRESS;

public class CatalogueVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LogManager.getLogger(CatalogueVerticle.class);
  private MessageConsumer<JsonObject> consumer;
  private ServiceBinder binder;
  private CatalogueUtilService catalogueUtilService;
  private TokenService tokenService;
  private WebClient webClient;

  private CircuitBreaker circuitBreaker;
  private CircuitBreakerOptions circuitBreakerOptions;

  @Override
  public void start() throws Exception {

    tokenService = TokenService.createProxy(vertx, TOKEN_ADDRESS);
<<<<<<< HEAD

    //Circuit breaker is used for retries on Central CAT only
    circuitBreakerOptions = new CircuitBreakerOptions()
        .setMaxFailures(3)
        .setMaxRetries(2)
        .setTimeout(5000);
    circuitBreaker = CircuitBreaker.create("Reties-circuit", vertx, circuitBreakerOptions);
    circuitBreaker.openHandler(openHandler -> {

    }).closeHandler(closeHandler -> {

    });

    catalogueUtilService = new CatalogueServiceImpl(vertx, tokenService, circuitBreaker, config());
    binder = new ServiceBinder(vertx);
    consumer = binder.setAddress(CATALOGUE_ADDRESS).register(CatalogueUtilService.class, catalogueUtilService);

    LOGGER.info("Cache Verticle deployed.");
  }

  @Override
  public void stop() {
    binder.unregister(consumer);
  }
}
