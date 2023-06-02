package iudx.onboarding.server.catalogue.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.core.json.JsonObject;
import iudx.onboarding.server.catalogue.CatalogueUtilService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocalCatImpl implements CatalogueService {

  private static final Logger LOGGER = LogManager.getLogger(LocalCatImpl.class);
  private String catHost;
  private int catPort;
  private String catBasePath;
  static WebClient catWebClient;

  public LocalCatImpl(Vertx vertx, JsonObject config) {
    LOGGER.debug("config : {}", config);
    this.catHost = config.getString("localCatServerHost");
    this.catPort = config.getInteger("localCatServerPort");
    this.catBasePath = config.getString("dxCatalogueBasePath");

    WebClientOptions options =
      new WebClientOptions().setTrustAll(true).setVerifyHost(false).setSsl(true);
    if (catWebClient == null) {
      catWebClient = WebClient.create(vertx, options);
    }

  }

  @Override
  public Future<JsonObject> createItem(JsonObject request) {
    return null;
  }

  @Override
  public Future<JsonObject> updateItem(JsonObject request) {
    return null;
  }

  @Override
  public Future<JsonObject> deleteItem(JsonObject request) {
    return null;
  }

}
