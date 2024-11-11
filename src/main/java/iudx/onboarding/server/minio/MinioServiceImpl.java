package iudx.onboarding.server.minio;

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
import io.vertx.core.json.JsonObject;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MinioServiceImpl implements MinioService {
  private static final Logger LOGGER = LogManager.getLogger(MinioServiceImpl.class);
  private final MinioClient minioClient;
  private final String minioAdmin;
  private final String minioEndpoint;

  public MinioServiceImpl(MinioClient minioClient, JsonObject config) {
    this.minioEndpoint = config.getString("minioEndpoint");
    this.minioClient = minioClient;
    this.minioAdmin = config.getString("minioAdmin");
  }

  @Override
  public Future<String> createBucket(String username) {
    Promise<String> promise = Promise.promise();
    try {
      String bucketName = username + "-bucket";
      boolean bucketExists =
          minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

      if (!bucketExists) {
        // Create bucket if it doesn't exist
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        LOGGER.debug("Bucket {} created successfully", bucketName);

        // Set the bucket policy and complete promise with bucket URL upon success
        setBucketPolicy(username, minioAdmin).onComplete(policyResult -> {
          if (policyResult.succeeded()) {
            String bucketUrl = minioEndpoint + bucketName;
            promise.complete(bucketUrl);  // Return the bucket URL
          } else {
            promise.fail(policyResult.cause());
          }
        });
      } else {
        LOGGER.debug("Bucket {} already exists", bucketName);
        String bucketUrl = minioEndpoint + bucketName;
        promise.complete(bucketUrl);  // Return existing bucket URL
      }
    } catch (Exception e) {
      LOGGER.error("Error creating user bucket: ", e);
      promise.fail(e);
    }
    return promise.future();
  }

  Future<Void> setBucketPolicy(String userName, String admin) {
    Promise<Void> promise = Promise.promise();
    try {
      String policy = createBucketPolicy(userName, admin);
      minioClient.setBucketPolicy(
          SetBucketPolicyArgs.builder().bucket(userName + "-bucket").config(policy).build());
      LOGGER.debug("Bucket policy for {} added successfully", userName);
      promise.complete();
    } catch (Exception e) {
      LOGGER.error("Error setting bucket policy: ", e);
      promise.fail(e);
    }
    return promise.future();
  }

  String createBucketPolicy(String userName, String admin) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode policyJson = mapper.createObjectNode();

    // Define policy version
    policyJson.put("Version", "2012-10-17");

    // Statement for the specific user
    ObjectNode userStatement = mapper.createObjectNode();
    userStatement.put("Effect", "Allow");

    ObjectNode userPrincipal = mapper.createObjectNode();
    userPrincipal.put("AWS", "arn:aws:iam::*:user/" + userName);
    userStatement.set("Principal", userPrincipal);

    ArrayNode userActions = userStatement.putArray("Action");
    List<String> actions = Arrays.asList("s3:GetObject", "s3:DeleteObject", "s3:PutObject");
    actions.forEach(userActions::add);

    ArrayNode userResources = userStatement.putArray("Resource");
    String bucketName = userName + "-bucket";
    userResources.add("arn:aws:s3:::" + bucketName + "/*");

    // Create statements for user and admin access
    ArrayNode statementsArray = policyJson.putArray("Statement");
    statementsArray.add(userStatement);

    // Statement for the admin user
    ObjectNode adminStatement = mapper.createObjectNode();
    adminStatement.put("Effect", "Allow");

    ObjectNode adminPrincipal = mapper.createObjectNode();
    adminPrincipal.put("AWS", "arn:aws:iam::*:user/" + admin);
    adminStatement.set("Principal", adminPrincipal);

    ArrayNode adminActions = adminStatement.putArray("Action");
    adminActions.addAll(userActions);  // Reuse user actions for admin

    ArrayNode adminResources = adminStatement.putArray("Resource");
    adminResources.add("arn:aws:s3:::" + bucketName + "/*");

    statementsArray.add(adminStatement);

    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(policyJson);
  }
}