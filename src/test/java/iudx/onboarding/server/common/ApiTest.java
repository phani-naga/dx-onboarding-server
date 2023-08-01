package iudx.onboarding.server.common;

import iudx.onboarding.server.common.Api;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiTest {
  @Test
  public void testGetOnboardingUrl() {
    String dxApiBasePath = "http://example.com/api";
    Api api = Api.getInstance(dxApiBasePath);
    String expectedOnboardingUrl = "http://example.com/api/item";
    String actualOnboardingUrl = api.getOnboardingUrl();
    assertEquals(expectedOnboardingUrl, actualOnboardingUrl);
  }

  @Test
  public void testGetIngestionUrl() {
    String dxApiBasePath = "http://example.com/api";
    Api api = Api.getInstance(dxApiBasePath);
    String expectedIngestionUrl = "http://example.com/api/ingestion";
    String actualIngestionUrl = api.getIngestionUrl();
    assertEquals(expectedIngestionUrl, actualIngestionUrl);
  }

  @Test
  public void testGetTokenUrl() {
    String dxApiBasePath = "http://example.com/api";
    Api api = Api.getInstance(dxApiBasePath);
    String expectedTokenUrl = "http://example.com/api/token";
    String actualTokenUrl = api.getTokenUrl();
    assertEquals(expectedTokenUrl, actualTokenUrl);
  }
}
