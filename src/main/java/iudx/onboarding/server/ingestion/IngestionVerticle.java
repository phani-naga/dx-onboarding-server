package iudx.onboarding.server.ingestion;

import static iudx.onboarding.server.common.Constants.INGESTION_ADDRESS;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IngestionVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LogManager.getLogger(IngestionVerticle.class);
  private MessageConsumer<JsonObject> consumer;
  private ServiceBinder binder;
  private IngestionService ingestionService;

  @Override
  public void start() throws Exception {
    ingestionService = new IngestionServiceImpl(vertx, config());
    binder = new ServiceBinder(vertx);
    consumer = binder
        .setAddress(INGESTION_ADDRESS)
        .register(IngestionService.class, ingestionService);

    LOGGER.info("Ingestion verticle deployed.");
  }

  @Override
  public void stop() {
    binder.unregister(consumer);
  }
}
