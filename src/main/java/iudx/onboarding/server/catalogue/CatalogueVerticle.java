package iudx.onboarding.server.catalogue;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static iudx.onboarding.server.common.Constants.*;

public class CatalogueVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LogManager.getLogger(CatalogueVerticle.class);
  private MessageConsumer<JsonObject> consumer;
  private ServiceBinder binder;
  private CatalogueUtilService catalogueUtilService;

  @Override
  public void start() throws Exception {

    catalogueUtilService = new CatalogueServiceImpl(vertx,config());
    binder = new ServiceBinder(vertx);
    consumer = binder.setAddress(CATALOGUE_ADDRESS).register(CatalogueUtilService.class, catalogueUtilService);

    LOGGER.info("Cache Verticle deployed.");
  }

  @Override
  public void stop() {
    binder.unregister(consumer);
  }
}
