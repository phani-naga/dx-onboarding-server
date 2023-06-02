package iudx.onboarding.server.common;

import static iudx.onboarding.server.apiserver.util.Constants.*;

public class Api {

  private static volatile Api apiInstance;
  private final String dxApiBasePath;
  private StringBuilder onboardingUrl;
  private StringBuilder ingestionUrl;
  private Api(String dxApiBasePath) {
    this.dxApiBasePath = dxApiBasePath;
    buildPaths();
  }

  public static Api getInstance(String dxApiBasePath) {
    if (apiInstance == null) {
      synchronized (Api.class) {
        if (apiInstance == null) {
          apiInstance = new Api(dxApiBasePath);
        }
      }
    }
    return apiInstance;
  }

  private void buildPaths() {
    onboardingUrl = new StringBuilder(dxApiBasePath).append(ONBOARDING_AIP);
    ingestionUrl = new StringBuilder(dxApiBasePath).append(INGESTION_AIP);
  }

  public String getOnboardingUrl() {
    return onboardingUrl.toString();
  }

  public String getIngestionUrl() {
    return ingestionUrl.toString();
  }
}
