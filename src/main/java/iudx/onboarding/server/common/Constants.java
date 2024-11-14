package iudx.onboarding.server.common;

import java.util.ArrayList;
import java.util.Arrays;

public class Constants {

  public static final String ID = "id";
  public static final String TOKEN = "token";
  public static final String TYPE = "type";
  public static final String SUB = "sub";

  /** Item types. */
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
