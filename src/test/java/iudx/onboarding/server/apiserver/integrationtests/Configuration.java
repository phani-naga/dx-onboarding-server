package iudx.onboarding.server.apiserver.integrationtests;

import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.FileInputStream;

public class Configuration {
  private static final Logger LOGGER = LogManager.getLogger(Configuration.class);
  public static JsonObject getConfiguration(String filePath) {

    try(FileInputStream inputStream = new FileInputStream(filePath)) {
      String confFile = IOUtils.toString(inputStream);
      JsonObject conf = new JsonObject(confFile);
      return conf;
    } catch (Exception e) {
      return new JsonObject();
    }
  }

  public static JsonObject getConfiguration(String filePath, int index) {

    try(FileInputStream inputStream = new FileInputStream(filePath)) {
      String confFile = IOUtils.toString(inputStream);
      JsonObject conf = new JsonObject(confFile).getJsonArray("modules").getJsonObject(index);
      return conf;
    } catch (Exception e) {
      return new JsonObject();
    }
  }

}
