package iudx.onboarding.server.common;

import static iudx.onboarding.server.apiserver.util.Constants.*;
import static iudx.onboarding.server.apiserver.util.Constants.INSTANCE_API;

public class Api {

  private static volatile Api apiInstance;
  private final String dxApiBasePath;
  private StringBuilder onboardingUrl;
  private StringBuilder ingestionUrl;
  private StringBuilder tokenUrl;
  private StringBuilder instanceUrl;
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
    onboardingUrl = new StringBuilder(dxApiBasePath).append(ONBOARDING_API);
    ingestionUrl = new StringBuilder(dxApiBasePath).append(INGESTION_API);
    tokenUrl = new StringBuilder(dxApiBasePath).append(TOKEN_API);
    instanceUrl = new StringBuilder(dxApiBasePath).append(INSTANCE_API);
  }

  public String getOnboardingUrl() {
    return onboardingUrl.toString();
  }

  public String getIngestionUrl() {
    return ingestionUrl.toString();
  }
  public String getTokenUrl(){
    return tokenUrl.toString();
  }
  public String getInstanceUrl(){
    return  instanceUrl.toString();
  }

}
