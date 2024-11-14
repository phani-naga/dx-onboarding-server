package iudx.onboarding.server.minio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MinioServiceTest {

  @Mock
  private MinioClient minioClient;

  private MinioServiceImpl minioService;

  private final String minioServerUrl = "http://172.19.0.1:9000";
  private final String minioAdmin = "testAdmin";

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    minioService = new MinioServiceImpl(minioClient, minioServerUrl, minioAdmin);
  }

  @Test
  public void testCreateBucketWhenBucketDoesNotExist() throws Exception {
    String username = "testuser";
    String bucketName = username + "-bucket";

    // Mock bucket existence check to return false
    when(minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()))
        .thenReturn(false);

    // Mock bucket creation and policy setting
    doNothing().when(minioClient).makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    doNothing().when(minioClient).setBucketPolicy(any(SetBucketPolicyArgs.class));

    // Call createBucket
    Future<String> resultFuture = minioService.createBucket(username);

    // Verify interactions and result
    verify(minioClient, times(1)).makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    verify(minioClient, times(1)).setBucketPolicy(any(SetBucketPolicyArgs.class));
    assertEquals(minioServerUrl + "/buckets/" + bucketName, resultFuture.result());
  }

  @Test
  public void testCreateBucketWhenBucketAlreadyExists() throws Exception {
    String username = "existinguser";
    String bucketName = username + "-bucket";

    // Mock bucket existence check to return true
    when(minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()))
        .thenReturn(true);

    // Call createBucket
    Future<String> resultFuture = minioService.createBucket(username);

    // Verify no bucket creation or policy setting calls
    verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
    verify(minioClient, never()).setBucketPolicy(any(SetBucketPolicyArgs.class));
    assertEquals(minioServerUrl + "/buckets/" + bucketName, resultFuture.result());
  }

  @Test
  public void testCreateBucketWhenBucketCreationFails() throws Exception {
    String username = "failuser";
    String bucketName = username + "-bucket";

    // Mock bucket existence check to return false and bucket creation to throw an exception
    when(minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()))
        .thenReturn(false);
    doThrow(new RuntimeException("Bucket creation failed"))
        .when(minioClient).makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

    // Call createBucket and verify failure
    Future<String> resultFuture = minioService.createBucket(username);
    assertTrue(resultFuture.failed());
    assertEquals("Bucket creation failed", resultFuture.cause().getMessage());
  }

  @Test
  public void testSetBucketPolicySuccess()
      throws Exception {
    String username = "policyuser";
    String bucketName = username + "-bucket";

    // Mock policy setting
    doNothing().when(minioClient).setBucketPolicy(any(SetBucketPolicyArgs.class));

    // Call setBucketPolicy and verify success
    Future<Void> resultFuture = minioService.setBucketPolicy(username, minioAdmin);
    verify(minioClient, times(1)).setBucketPolicy(any(SetBucketPolicyArgs.class));
    assertTrue(resultFuture.succeeded());
  }

  @Test
  public void testSetBucketPolicyFailure() throws Exception {
    String username = "policyfailuser";

    // Mock policy setting to throw an exception
    doThrow(new RuntimeException("Policy setting failed"))
        .when(minioClient).setBucketPolicy(any(SetBucketPolicyArgs.class));

    // Call setBucketPolicy and verify failure
    Future<Void> resultFuture = minioService.setBucketPolicy(username, minioAdmin);
    assertTrue(resultFuture.failed());
    assertEquals("Policy setting failed", resultFuture.cause().getMessage());
  }

  @Test
  public void testCreateBucketPolicyJson() throws JsonProcessingException {
    String username = "jsonpolicyuser";
    String policyJson = minioService.createBucketPolicy(username, minioAdmin);

    assertNotNull(policyJson);
    assertTrue(policyJson.contains("s3:GetObject"));
    assertTrue(policyJson.contains("s3:DeleteObject"));
    assertTrue(policyJson.contains("s3:PutObject"));
    assertTrue(policyJson.contains("arn:aws:s3:::" + username + "-bucket/*"));
  }
}

