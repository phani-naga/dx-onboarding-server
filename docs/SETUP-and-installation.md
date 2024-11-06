<p align="center">
<img src="./cdpg.png" width="300">
</p>

# Setup and Installation Guide
This document contains the installation and configuration information required to deploy the 
Data Exchange (DX) Onboarding Server.

## Configuration
In order to connect the Onboarding Server with DX Catalogue Server, DX Resource Server, Keycloak 
please refer [Configurations](./Configurations.md). It contains appropriate information which 
shall be updated as per the deployment.

## Dependencies
In this section we explain about the dependencies and their scope. It is expected that the 
dependencies are met before starting the deployment of DX Onboarding Server.

### External Dependencies
| Software Name | Purpose                                       | 
|:--------------|:----------------------------------------------|
| KeyCloak      | Used for authentication and token management. |

### Internal Dependencies
|  Software Name      | Purpose                                     | 
|:--------------------|:--------------------------------------------|
| DX Resource Server  | Used to register an adapter                 |
| DX Catalogue Server | Manages and serves metadata for datasets.   |

### Prerequisite - Make Configuration
Make a config file based on the template in ./configs/config-example.json

- Generate a certificate using Lets Encrypt or other methods
- Make a Java Keystore File and mention its path and password in the appropriate sections
- Modify the database url and associated credentials in the appropriate sections

### Keystore
The server requires certificates to be stored in Java keystore format.
1. Obtain certs for your domain using Letsencrypt. Note: Self signed certificates using openssl will also work.
2. Concat all pems into one file
   `sudo cat /etc/letsencrypt/live/demo.example.com/*.pem > fullcert.pem`
3. Convert to pkcs format
   ` openssl pkcs12 -export -out fullcert.pkcs12 -in fullcert.pem`
4. Create new temporary keystore using JDK keytool, will prompt for password
   `keytool -genkey -keyalg RSA -alias mykeystore -keystore mykeystore.ks`  
   `keytool -delete -alias mykeystore -keystore mykeystore.ks`
5. Make JKS, will prompt for password
   `keytool -v -importkeystore -srckeystore fullcert.pkcs12 -destkeystore mykeystore.ks -deststoretype JKS`
6. Store JKS in config directory and edit the keyfile name and password entered in previous step
7. Mention keystore mount path (w.r.t docker-compose) in config.json

## Installation Steps

### Maven
1. Install java 11 and maven
2. Use the maven exec plugin based starter to start the server. Goto the root folder where the pom.xml file is present and run the below command.
   `mvn clean compile exec:java@onboarding-server`

### JAR
1. Install java 11 and maven
2. Set Environment variables
```
export ONBOARDING_URL=https://<onboarding-domain-name>
export LOG_LEVEL=INFO
```
3. Use maven to package the application as a JAR. Goto the root folder where the pom.xml file is present and run the below command.
   `mvn clean package -Dmaven.test.skip=true`
4. 2 JAR files would be generated in the `target/` directory
   - `iudx.onboarding.server-cluster-${VERSION}-fat.jar` - clustered vert.x containing micrometer metrics
   - `iudx.onboarding.server-dev-0.0.1-SNAPSHOT-fat.jar` - non-clustered vert.x and does not 
     contain micrometer metrics

#### Running the clustered JAR
**Note**: The clustered JAR requires Zookeeper to be installed. Refer [here](https://zookeeper.apache.org/doc/current/zookeeperStarted.html) to learn more about how to set up Zookeeper. Additionally, the `zookeepers` key in the config being used needs to be updated with the IP address/domain of the system running Zookeeper.
The JAR requires 3 runtime arguments when running:

* --config/-c : path to the config file
* --host/-i : the hostname for clustering
* --modules/-m : comma separated list of module names to deploy

e.g. ```java -jar target/iudx.onboarding.server-cluster-0.0.1-SNAPSHOT-fat.jar --host $(hostname)
-c secrets/all-verticles-configs/config.json -m iudx.onboarding.server.catalogue.
CatalogueVerticle, iudx.onboarding.server.apiserver.ApiServerVerticle, iudx.onboarding.server.token.TokenVerticle, iudx.onboarding.server.ingestion.IngestionVerticle, iudx.onboarding.server.resourceserver.ResourceServerVerticle```

Use the `--help/-h` argument for more information. You may additionally append an 
`ONBOARDING_JAVA_OPTS` 
environment
variable containing any Java options to pass to the application.

e.g.
```
$ export ONBOARDING_JAVA_OPTS="Xmx40496m"
$ java $ONBOARDING_JAVA_OPTS -jar target/iudx.onboarding.server-cluster-0.0.1-SNAPSHOT-fat.jar ...

```

#### Running the non-clustered JAR
The JAR requires 1 runtime argument when running

* --config/-c : path to the config file

e.g. `java -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.
Log4j2LogDelegateFactory -jar target/iudx.onboarding.server-cluster-0.0.1-SNAPSHOT-fat.jar -c 
secrets/all-verticles-configs/config.json`

Use the `--help/-h` argument for more information. You may additionally append an `RS_JAVA_OPTS` environment variable containing any Java options to pass to the application.

e.g.
```
$ export ONBOARDING_JAVA_OPTS="-Xmx1024m"
$ java ONBOARDING_JAVA_OPTS -jar target/iudx.onboarding.server-cluster-0.0.1-SNAPSHOT-fat.jar ...
```

### Docker
1. Install docker and docker-compose
2. Clone this repo
3. Build the images
   ` ./docker/build.sh`
4. Modify the `docker-compose.yml` file to map the config file
5. Start the server in production (prod) or development (dev) mode using docker-compose
   ` docker-compose up prod `

### Redeployer
A hot-swappable redeployer is provided for quick development
`./redeploy.sh`

## Logging and Monitoring
### Log4j 2
- For asynchronous logging, logging messages to the console in a specific format, Apache's log4j 2 is used
- For log formatting, adding appenders, adding custom logs, setting log levels, log4j2.xml could be updated : [link](https://github.com/datakaveri/iudx-onboarding-server/blob/main/src/main/resources/log4j2.xml)
- Please find the reference to log4j 2 : [here](https://logging.apache.org/log4j/2.x/manual/index.html)

### Micrometer
- Micrometer is used for observability of the application
- Reference link: [vertx-micrometer-metrics](https://vertx.io/docs/vertx-micrometer-metrics/java/)
- The metrics from micrometer is stored in Prometheus which can be used to alert, observe,
  take steps towards the current state of the application
- The data sent to Prometheus can then be visualised in Grafana
- Reference link: [vertx-prometheus-grafana](https://how-to.vertx.io/metrics-prometheus-grafana-howto/)
- DX Deployment repository references for [Prometheus](https://github.com/datakaveri/iudx-deployment/tree/master/K8s-deployment/K8s-cluster/addons/mon-stack/prometheus), [Loki](https://github.com/datakaveri/iudx-deployment/tree/master/K8s-deployment/K8s-cluster/addons/mon-stack/loki), [Grafana](https://github.com/datakaveri/iudx-deployment/tree/master/K8s-deployment/K8s-cluster/addons/mon-stack/grafana)

## Testing
### Unit Testing
1. Run the server through either docker, maven or redeployer
2. Run the unit tests and generate a surefire report
   `mvn clean test-compile surefire:test surefire-report:report`
3. Jacoco reports are stored in `./target/`

### Integration Testing

Integration tests are through Postman/Newman whose script can be found from [here](https://github.com/datakaveri/iudx-onboarding-server/tree/main/src/test/resources).
1. Install prerequisites
- [postman](https://www.postman.com/) + [newman](https://www.npmjs.com/package/newman)
- [newman reporter-htmlextra](https://www.npmjs.com/package/newman-reporter-htmlextra)
2. Example Postman environment can be found [here]()
- Please find the README to setup postman environment file [here]()
3. Run the server through either docker, maven or redeployer
4. Run the integration tests and generate the newman report
   `newman run <postman-collection-path> -e <postman-environment> --insecure -r htmlextra --reporter-htmlextra-export .`
5. Command to store report in `target/newman`:  `newman run <postman-collection-path> -e <postman-environment> --insecure -r htmlextra --reporter-htmlextra-export ./target/newman/report.html`


### Performance Testing
- JMeter is for used performance testing, load testing of the application
- Please find the reference to JMeter : [here](https://jmeter.apache.org/usermanual/get-started.html)
- Command to generate HTML report at `target/jmeter`
```
rm -r -f target/jmeter && jmeter -n -t jmeter/<file-name>.jmx -l target/jmeter/sample-reports.csv -e -o target/jmeter/
```

### Security Testing
- For security testing, Zed Attack Proxy(ZAP) Scanning is done to discover security risks, vulnerabilities to help us address them
- A report is generated to show vulnerabilities as high risk, medium risk, low risk and false positive
- Please find the reference to ZAP : [here](https://www.zaproxy.org/getting-started/)
