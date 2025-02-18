package iudx.onboarding.server.minio;

import io.minio.MinioClient;

public class MinioClientFactory {

  public static MinioClient createMinioClient(String endpoint, String region, String accessKey,
                                              String secretKey) {
    return MinioClient.builder()
        .endpoint(endpoint)
        .region(region)
        .credentials(accessKey, secretKey)
        .build();
  }
}