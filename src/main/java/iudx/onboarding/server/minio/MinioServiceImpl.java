package iudx.onboarding.server.minio;

import static iudx.onboarding.server.apiserver.util.Constants.HEADER_Authorization;
import static iudx.onboarding.server.apiserver.util.Constants.HEADER_CONTENT_TYPE;
import static iudx.onboarding.server.apiserver.util.Constants.MIME_APPLICATION_JSON;
import static iudx.onboarding.server.common.Constants.ACTION;
import static iudx.onboarding.server.common.Constants.ATTACH_POLICY_ENDPOINT;
import static iudx.onboarding.server.common.Constants.BUCKET_ACTIONS;
import static iudx.onboarding.server.common.Constants.EFFECT;
import static iudx.onboarding.server.common.Constants.EFFECT_ALLOW;
import static iudx.onboarding.server.common.Constants.IAM_USER_ARN_PREFIX;
import static iudx.onboarding.server.common.Constants.MINIO_BUCKET_SUFFIX;
import static iudx.onboarding.server.common.Constants.MINIO_POLICY_VERSION;
import static iudx.onboarding.server.common.Constants.MINIO_UI_BROWSER_PATH;
import static iudx.onboarding.server.common.Constants.PRINCIPAL;
import static iudx.onboarding.server.common.Constants.PRINCIPAL_AWS;
import static iudx.onboarding.server.common.Constants.RESOURCE;
import static iudx.onboarding.server.common.Constants.S3_ALL_OBJECTS_SUFFIX;
import static iudx.onboarding.server.common.Constants.S3_BUCKET_ARN_PREFIX;
import static iudx.onboarding.server.common.Constants.STATEMENT;
import static iudx.onboarding.server.common.Constants.VERSION;

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

  /**
   * Constructor to initialize MinioServiceImpl.
   *
   * @param vertx             Vert.x instance
   * @param minioClient       Minio client instance
   * @param minioServerUrl    Minio server URL
   * @param minioAdmin        Minio admin username
   * @param minioPolicyApiUrl Minio policy API URL
   * @param authorizationKey  Authorization key for API calls to minio-policy-api server
   */
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
      String bucketName = username + MINIO_BUCKET_SUFFIX;
      boolean bucketExists =
          minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

      if (!bucketExists) {
        // Create bucket if it doesn't exist
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        LOGGER.debug("Bucket {} created successfully", bucketName);

        // Set the bucket policy and complete promise with bucket URL upon success
        setBucketPolicy(username, minioAdmin).onComplete(policyResult -> {
          if (policyResult.succeeded()) {
            String bucketUrl = minioServerUrl + MINIO_UI_BROWSER_PATH + bucketName;
            promise.complete(bucketUrl);  // Return the bucket URL
          } else {
            promise.fail(policyResult.cause());
          }
        });
      } else {
        LOGGER.debug("Bucket {} already exists", bucketName);
        String bucketUrl = minioServerUrl + MINIO_UI_BROWSER_PATH + bucketName;
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

    webClient.postAbs(minioPolicyApiUrl + ATTACH_POLICY_ENDPOINT)
        .putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
        .putHeader(HEADER_Authorization, authorizationKey)
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

  /**
   * Sets bucket policy for a specific user and admin.
   *
   * @param userName The name of the user
   * @param admin    The admin username
   * @return Future indicating success or failure of the policy setup
   */
  Future<Void> setBucketPolicy(String userName, String admin) {
    Promise<Void> promise = Promise.promise();
    try {
      String policy = createBucketPolicy(userName, admin);
      minioClient.setBucketPolicy(
          SetBucketPolicyArgs.builder().bucket(userName + MINIO_BUCKET_SUFFIX).config(policy)
              .build());
      LOGGER.debug("Bucket policy for {} added successfully", userName);
      promise.complete();
    } catch (Exception e) {
      LOGGER.error("Error setting bucket policy: ", e);
      promise.fail(e);
    }
    return promise.future();
  }

  /**
   * Creates a JSON bucket policy allowing specified actions for a user and an admin.
   *
   * @param userName The username for the policy
   * @param admin    The admin username
   * @return JSON policy as a string
   * @throws JsonProcessingException If an error occurs while generating the policy JSON
   */
  String createBucketPolicy(String userName, String admin) throws JsonProcessingException {
    if (admin == null || admin.isEmpty()) {
      throw new IllegalArgumentException("MinIO admin user cannot be null or empty");
    }
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode policyJson = mapper.createObjectNode();

    // Define policy version
    policyJson.put(VERSION, MINIO_POLICY_VERSION);

    // Actions that both user and admin can perform
    String bucketName = userName + MINIO_BUCKET_SUFFIX;

    // Create statements for user and admin access
    ArrayNode statementsArray = policyJson.putArray(STATEMENT);
    statementsArray.add(createStatement(userName, BUCKET_ACTIONS, bucketName));
    statementsArray.add(createStatement(admin, BUCKET_ACTIONS, bucketName));

    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(policyJson);
  }

  /**
   * Creates a statement node for the bucket policy.
   *
   * @param principalUser The user for whom the policy is being created
   * @param actions       List of actions allowed
   * @param bucketName    The bucket name
   * @return A JSON ObjectNode representing the policy statement
   */
  private ObjectNode createStatement(String principalUser, List<String> actions,
                                     String bucketName) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode statement = mapper.createObjectNode();
    statement.put(EFFECT, EFFECT_ALLOW);

    ObjectNode principal = mapper.createObjectNode();
    principal.put(PRINCIPAL_AWS, IAM_USER_ARN_PREFIX + principalUser);
    statement.set(PRINCIPAL, principal);

    ArrayNode actionArray = statement.putArray(ACTION);
    actions.forEach(actionArray::add);

    ArrayNode resourceArray = statement.putArray(RESOURCE);
    resourceArray.add(S3_BUCKET_ARN_PREFIX + bucketName + S3_ALL_OBJECTS_SUFFIX);

    return statement;
  }
}