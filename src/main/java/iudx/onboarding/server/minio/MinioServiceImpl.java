package iudx.onboarding.server.minio;

import static iudx.onboarding.server.common.Constants.BUCKET;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MinioServiceImpl implements MinioService {
  private static final Logger LOGGER = LogManager.getLogger(MinioServiceImpl.class);
  public static WebClient webClient;
  private final MinioClient minioClient;
  private final String minioAdmin;
  private final String minioServerUrl;
  private final String authorizationKey;
  private final String minioPolicyApiUrl;

  public MinioServiceImpl(Vertx vertx, MinioClient minioClient, String minioServerUrl,
                          String minioAdmin, String minioPolicyApiUrl, String authorizationKey) {
    this.minioServerUrl = minioServerUrl;
    this.minioClient = minioClient;
    this.minioAdmin = minioAdmin;
    this.authorizationKey = authorizationKey;
    this.minioPolicyApiUrl = minioPolicyApiUrl;

    WebClientOptions options =
        new WebClientOptions().setTrustAll(true).setVerifyHost(false).setSsl(true);
    if (webClient == null) {
      webClient = WebClient.create(vertx, options);
    }
  }

  @Override
  public Future<String> createBucket(String username) {
    Promise<String> promise = Promise.promise();
    try {
      String bucketName = username + BUCKET;
      boolean bucketExists =
          minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

      if (!bucketExists) {
        // Create bucket if it doesn't exist
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        LOGGER.debug("Bucket {} created successfully", bucketName);

        // Set the bucket policy and complete promise with bucket URL upon success
        setBucketPolicy(username, minioAdmin).onComplete(policyResult -> {
          if (policyResult.succeeded()) {
            String bucketUrl = minioServerUrl + "/minio/ui/browser/" + bucketName;
            promise.complete(bucketUrl);  // Return the bucket URL
          } else {
            promise.fail(policyResult.cause());
          }
        });
      } else {
        LOGGER.debug("Bucket {} already exists", bucketName);
        String bucketUrl = minioServerUrl + "/buckets/" + bucketName;
        promise.complete(bucketUrl);  // Return existing bucket URL
      }
    } catch (Exception e) {
      LOGGER.error("Error creating user bucket: ", e);
      promise.fail(e);
    }
    return promise.future();
  }

  @Override
  public Future<Void> attachBucketToNamePolicy(JsonObject policyRequest) {
    Promise<Void> promise = Promise.promise();

    webClient.post(minioPolicyApiUrl, "/attach-bucket-to-user-policy")
        .putHeader("Content-Type", "application/json")
        .putHeader("Authorization", authorizationKey)
        .sendJsonObject(policyRequest, ar -> {
          if (ar.succeeded()) {
            LOGGER.info(ar.result().statusCode());
            LOGGER.info(ar.result().bodyAsJsonObject());
            LOGGER.info("Bucket policy attached successfully");
            promise.complete();
          } else {
            LOGGER.error("Failed to attach bucket policy: " + ar.cause().getMessage());
            promise.fail(ar.cause());
          }
        });

    return promise.future();

  }

  Future<Void> setBucketPolicy(String userName, String admin) {
    Promise<Void> promise = Promise.promise();
    try {
      String policy = createBucketPolicy(userName, admin);
      minioClient.setBucketPolicy(
          SetBucketPolicyArgs.builder().bucket(userName + BUCKET).config(policy).build());
      LOGGER.debug("Bucket policy for {} added successfully", userName);
      promise.complete();
    } catch (Exception e) {
      LOGGER.error("Error setting bucket policy: ", e);
      promise.fail(e);
    }
    return promise.future();
  }

  String createBucketPolicy(String userName, String admin) throws JsonProcessingException {
    if (admin == null || admin.isEmpty()) {
      throw new IllegalArgumentException("MinIO admin user cannot be null or empty");
    }

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode policyJson = mapper.createObjectNode();

    // Define policy version
    policyJson.put("Version", "2012-10-17");

    // Actions that both user and admin can perform
    List<String> actions = Arrays.asList("s3:GetObject", "s3:DeleteObject", "s3:PutObject");
    String bucketName = userName + BUCKET;

    // Create statements for user and admin access
    ArrayNode statementsArray = policyJson.putArray("Statement");
    statementsArray.add(createStatement(userName, actions, bucketName));
    statementsArray.add(createStatement(admin, actions, bucketName));

    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(policyJson);
  }

  private ObjectNode createStatement(String principalUser, List<String> actions,
                                     String bucketName) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode statement = mapper.createObjectNode();
    statement.put("Effect", "Allow");

    ObjectNode principal = mapper.createObjectNode();
    principal.put("AWS", "arn:aws:iam::*:user/" + principalUser);
    statement.set("Principal", principal);

    ArrayNode actionArray = statement.putArray("Action");
    actions.forEach(actionArray::add);

    ArrayNode resourceArray = statement.putArray("Resource");
    resourceArray.add("arn:aws:s3:::" + bucketName + "/*");

    return statement;
  }
}