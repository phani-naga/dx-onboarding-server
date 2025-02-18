package iudx.onboarding.server.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {

  public static final String ID = "id";
  public static final String TOKEN = "token";
  public static final String TYPE = "type";
  public static final String SUB = "sub";
  public static final String POLICY_USER_ID = "userId";
  public static final String POLICY_BUCKET = "bucket";
  public static final String POLICY_CREATE_BUCKET = "createBucket";
  public static final String MINIO_BUCKET_SUFFIX = "-bucket";
  public static final String MINIO_UI_BROWSER_PATH = "/minio/ui/browser/";
  public static final String ATTACH_POLICY_ENDPOINT = "/attach-bucket-to-user-policy";
  public static final String MINIO_POLICY_VERSION = "2012-10-17";
  public static final List<String>
      BUCKET_ACTIONS = Arrays.asList("s3:GetObject", "s3:DeleteObject", "s3:PutObject");
  public static final String IAM_USER_ARN_PREFIX = "arn:aws:iam::*:user/";
  public static final String S3_BUCKET_ARN_PREFIX = "arn:aws:s3:::";
  public static final String S3_ALL_OBJECTS_SUFFIX = "/*";
  public static final String EFFECT = "Effect";
  public static final String EFFECT_ALLOW = "Allow";
  public static final String PRINCIPAL = "Principal";
  public static final String PRINCIPAL_AWS = "AWS";
  public static final String ACTION = "Action";
  public static final String RESOURCE = "Resource";
  public static final String VERSION = "Version";
  public static final String STATEMENT = "Statement";


  public static final String BUCKET_URL = "bucketUrl";

  /**
   * Item types.
   */
  public static final String ITEM_TYPE_RESOURCE = "iudx:Resource";
  public static final String ITEM_TYPE_RESOURCE_GROUP = "iudx:ResourceGroup";
  public static final String ITEM_TYPE_RESOURCE_SERVER = "iudx:ResourceServer";
  public static final String ITEM_TYPE_PROVIDER = "iudx:Provider";
  public static final String ITEM_TYPE_COS = "iudx:COS";
  public static final String ITEM_TYPE_OWNER = "iudx:Owner";

  public static final ArrayList<String> ITEM_TYPES =
      new ArrayList<String>(Arrays.asList(ITEM_TYPE_RESOURCE, ITEM_TYPE_RESOURCE_GROUP,
          ITEM_TYPE_RESOURCE_SERVER, ITEM_TYPE_PROVIDER, ITEM_TYPE_COS, ITEM_TYPE_OWNER));

  /* Service Addresses */
  public static final String CATALOGUE_ADDRESS = "iudx.onboarding.server.catalogue";
  public static final String TOKEN_ADDRESS = "iudx.onboarding.server.token";
  public static final String INGESTION_ADDRESS = "iudx.onboarding.server.ingestion";
  public static final String RS_SERVICE_ADDRESS = "iudx.onboarding.server.resourceserver";
  public static final String MINIO_ADDRESS = "iudx.onboarding.server.minio";
}
