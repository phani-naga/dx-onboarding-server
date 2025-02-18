package iudx.onboarding.server.token;

import static iudx.onboarding.server.common.Constants.TOKEN;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.providers.KeycloakAuth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TokenServiceImpl implements TokenService {
  private static final Logger LOGGER = LogManager.getLogger(TokenServiceImpl.class);
  private final String keycloakClientId;
  private final String keycloakClientSecret;
  private final String keycloakSite;
  private final OAuth2Options options;
  Vertx vertx;
  JsonObject config;
  OAuth2Auth keycloak;
  private String jwtToken;

  public TokenServiceImpl(Vertx vertx, JsonObject config) {
    this.config = config;
    this.vertx = vertx;
    this.keycloakClientId = config.getString("keycloakClientId");
    this.keycloakClientSecret = config.getString("keycloakClientSecret");
    this.keycloakSite = config.getString("keycloakSite");
    this.options =
        new OAuth2Options()
            .setFlow(OAuth2FlowType.CLIENT)
            .setClientId(keycloakClientId)
            .setClientSecret(keycloakClientSecret)
            .setSite(keycloakSite);

    KeycloakAuth.discover(vertx, options)
        .onComplete(
            discover -> {
              if (discover.succeeded()) {
                this.keycloak = discover.result();
                LOGGER.info("Keycloak Discovery Successful");
              } else {
                LOGGER.error("Keycloak Discovery Failed " + discover.cause());
              }
            });
  }

  @Override
  public Future<JsonObject> createToken() {
    Promise<JsonObject> promise = Promise.promise();
    keycloak.authenticate(new JsonObject())
        .onSuccess(
            res -> {
              jwtToken = res.principal().getString("access_token");
              LOGGER.info("Token generated successfully ");
              promise.complete(new JsonObject().put(TOKEN, jwtToken));
            })
        .onFailure(err -> {
          LOGGER.error("Failed to generate the token " + err.getMessage());
          promise.fail(err.getMessage());
        });
    return promise.future();
  }

  @Override
  public Future<JsonObject> decodeToken(String token) {
    Promise<JsonObject> promise = Promise.promise();
    try {
      keycloak.authenticate(new TokenCredentials(token))
          .onSuccess(authInfo -> {
            JsonObject tokenInfo = authInfo.principal();
            LOGGER.debug("Token decoded successfully: " + tokenInfo);
            promise.complete(tokenInfo);
          })
          .onFailure(err -> {
            LOGGER.error("Token decoding failed: " + err.getLocalizedMessage());
            promise.fail(err.getLocalizedMessage());
          });
    } catch (Exception exception) {
      LOGGER.error("Token decoding failed: " + exception.getLocalizedMessage());
      promise.fail(exception.getLocalizedMessage());
    }
    return promise.future();
  }
}
