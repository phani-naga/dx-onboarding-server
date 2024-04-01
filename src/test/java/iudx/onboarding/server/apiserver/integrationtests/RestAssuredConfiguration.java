package iudx.onboarding.server.apiserver.integrationtests;

import io.vertx.core.json.JsonObject;
import iudx.onboarding.server.apiserver.integrationtests.tokens.TokenSetup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static io.restassured.RestAssured.*;
import static iudx.onboarding.server.apiserver.integrationtests.tokens.TokenForITs.*;

public class RestAssuredConfiguration implements BeforeAllCallback {
  private static final Logger LOGGER = LogManager.getLogger(RestAssuredConfiguration.class);

  @Override
  public void beforeAll(ExtensionContext context) {
    JsonObject config = Configuration.getConfiguration("./secrets/all-verticles-configs/config-test.json", 0);
    String authServerHost = config.getString("authServerHost");
    boolean testOnDepl = Boolean.parseBoolean(System.getProperty("intTestDepl"));
    if (testOnDepl) {
      String testHost = config.getString("testHost");
      baseURI = "https://" + testHost;
      port = 443;
    } else {
      String testHost = System.getProperty("intTestHost");

      if (testHost != null) {
        baseURI = "http://" + testHost;
      } else {
        baseURI = "http://localhost";
      }

      String testPort = System.getProperty("intTestPort");

      if (testPort != null) {
        port = Integer.parseInt(testPort);
      } else {
        port = 8090;
      }
    }
    basePath = "/iudx/cat/v1";
    String dxAuthBasePath = "auth/v1";
    String authEndpoint = "https://" + authServerHost + "/" + dxAuthBasePath + "/token";
    String proxyHost = System.getProperty("intTestProxyHost");
    String proxyPort = System.getProperty("intTestProxyPort");


    if (proxyHost != null && proxyPort != null) {
      proxy(proxyHost, Integer.parseInt(proxyPort));
    }

    LOGGER.debug("setting up the tokens");
    TokenSetup.setupTokens(
      authEndpoint,
      config.getJsonObject("clientCredentials"));

    // Wait for tokens to be available before proceeding
    waitForTokens();

    enableLoggingOfRequestAndResponseIfValidationFails();
  }

  private void waitForTokens() {
    int maxAttempts = 5;
    int attempt = 0;

    // Keep trying to get tokens until they are available or max attempts are reached
    while ((cosAdminToken == null || rsAdminToken == null || providerToken == null)
      && attempt < maxAttempts) {
      LOGGER.debug("Waiting for tokens to be available. Attempt: " + (attempt + 1));
      // Introduce a delay between attempts
      try {
        Thread.sleep(1000); // Adjust the delay as needed
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      attempt++;
    }

    if (cosAdminToken == null || rsAdminToken == null || providerToken == null) {
      // Log an error or throw an exception if tokens are still not available
      throw new RuntimeException("Failed to retrieve tokens after multiple attempts.");
    } else {
      LOGGER.debug("Tokens are now available. Proceeding with RestAssured configuration.");
    }
  }
}
