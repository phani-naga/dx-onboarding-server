package iudx.onboarding.server.catalogue.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CentralCatImpl implements CatalogueService {

  private static final Logger LOGGER = LogManager.getLogger(CentralCatImpl.class);
  private String catHost;
  private int catPort;
  private String catBasePath;
  static WebClient catWebClient;
  private KeyCloakClient keyCloakClient;

  public CentralCatImpl(Vertx vertx, JsonObject config, KeyCloakClient keyCloakClient) {
    LOGGER.debug("config : {}", config);
    this.keyCloakClient = keyCloakClient;
    this.catHost = config.getString("centralCatServerHost");
    this.catPort = config.getInteger("centralCatServerPort");
    this.catBasePath = config.getString("dxCatalogueBasePath");

    WebClientOptions options =
      new WebClientOptions().setTrustAll(true).setVerifyHost(false).setSsl(true);
    if (catWebClient == null) {
      catWebClient = WebClient.create(vertx, options);
    }

  }

  @Override
  public Future<JsonObject> createItem(JsonObject request, String token) {
    return null;
  }

  @Override
  public Future<JsonObject> updateItem(JsonObject request, String token) {
    return null;
  }

  @Override
  public Future<JsonObject> deleteItem(JsonObject request) {
    return null;
  }
}
