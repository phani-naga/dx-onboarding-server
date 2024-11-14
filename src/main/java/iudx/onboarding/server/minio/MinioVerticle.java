package iudx.onboarding.server.minio;

import static iudx.onboarding.server.common.Constants.MINIO_ADDRESS;

import io.minio.MinioClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MinioVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LogManager.getLogger(MinioVerticle.class);
  private ServiceBinder binder;
  private MinioService minioService;
  private MinioClient minioClient;
  private MessageConsumer<JsonObject> consumer;

  @Override
  public void start() throws Exception {
    String minioHost = config().getString("minioHost");
    String minioPort = config().getString("minioPort");
    String minioServerUrl = minioHost + ":" + minioPort;
    String minioAdmin = config().getString("minioAdmin");
    minioClient = MinioClientFactory.createMinioClient(
        minioServerUrl,
        config().getString("minioRegion"),
        config().getString("minioAccessKey"),
        config().getString("minioSecretKey"));
    minioService = new MinioServiceImpl(minioClient, minioServerUrl, minioAdmin);

    binder = new ServiceBinder(vertx);
    consumer = binder.setAddress(MINIO_ADDRESS).register(MinioService.class, minioService);
    LOGGER.debug("Minio Verticle deployed.");
  }

  @Override
  public void stop() {
    binder.unregister(consumer);
    LOGGER.debug("MinioVerticle stopped.");
  }
}
