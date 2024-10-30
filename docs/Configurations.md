<p align="center">
<img src="./cdpg.png" width="300">
</p>

# Modules
This document contains the information of the configurations to setup various services and 
dependencies in order to bring up the DX Onboarding Server.
Please find the example configuration file [here](https://github.com/datakaveri/iudx-onboarding-server/blob/main/example-config/config-dev.json). While running 
the server, config.json could be added [secrets](https://github.com/datakaveri/iudx-onboarding-server/tree/main/secrets/all-verticles-configs).


## Api Server Verticle

| Key Name          | Value Datatype | Value Example | Description                                                              |
|:------------------|:--------------:|:--------------|:-------------------------------------------------------------------------|
| isWorkerVerticle  |    boolean     | false         | To check if worker verticle needs to be deployed for blocking operations |
| verticleInstances |    integer     | 1             | Number of instances required for verticles                               |
| httpPort          |    integer     | 8080          | Port for running the instance DX Onboarding Server                       |

## Other Configuration

| Key Name                                    | Value Datatype | Value Example             | Description                                                                                                                   |
|:--------------------------------------------|:--------------:|:--------------------------|:------------------------------------------------------------------------------------------------------------------------------|
| version                                     |     Float      | 1.0                       | config version                                                                                                                |
| zookeepers                                  |     Array      | zookeeper                 | zookeeper configuration to deploy clustered vert.x instance                                                                   |
| clusterId                                   |     String     | iudx-onboarding-cluster   | cluster id to deploy clustered vert.x instance                                                                                |
| commonConfig.dxApiBasePath                  |     String     | /dx/apd/acl/v1            | API base path for DX Onboarding Server. Reference : [link](https://swagger.io/docs/specification/v2_0/api-host-and-base-path/) |
| commonConfig.dxCatalogueBasePath            |     String     | /iudx/cat/v1              | API base path for DX Catalogue server. Reference : [link](https://swagger.io/docs/specification/v2_0/api-host-and-base-path/)  |
| commonConfig.dxAuthBasePath                 |     String     | /auth/v1                  | API base path for DX AAA server. Reference : [link](https://swagger.io/docs/specification/v2_0/api-host-and-base-path/)        |
| commonConfig.localCatServerHost             |     String     | api.cat-test.iudx.io      | Host name of Local DX Catalogue server                                                                                        |
| commonConfig.localCatServerPort             |    integer     | 443                       | Port number to access HTTPS APIs of Catalogue Server                                                                          |
| commonConfig.centralCatServerHost           |     String     | api.cat-test.iudx.io      | Host name of Central DX Catalogue Server                                                                                      |
| commonConfig.centralCatServerPort           |    integer     | 443                       | Port number to access HTTPS APIs of DX Catalogue server                                                                       |
| commonConfig.isUacAvailable                 |    boolean     | true                      |                                                                                                                               |

## Catalogue Verticle

| Key Name          | Value Datatype | Value Example | Description                                                              |
|:------------------|:--------------:|:--------------|:-------------------------------------------------------------------------|
| isWorkerVerticle  |    boolean     | false         | To check if worker verticle needs to be deployed for blocking operations |
| verticleInstances |    integer     | 1             | Number of instances required for verticles                               |

## Token Verticle

| Key Name              | Value Datatype | Value Example                                                                  | Description                                                              |
|:----------------------|:--------------:|:-------------------------------------------------------------------------------|:-------------------------------------------------------------------------|
| isWorkerVerticle      |    boolean     | false                                                                          | To check if worker verticle needs to be deployed for blocking operations |
| verticleInstances     |    integer     | 1                                                                              | Number of instances required for verticles                               |
| keycloakSite          |     string     | {{protocol}}://{{keycloakHost}}:{{keycloakPort}}/auth/realms/{{keycloakRealm}} | The url to the keycloak site                                             |
| keycloakClientId      |     string     | auth.iudx.org.in                                                               | trustee client ID                                                        |                                                    
| keycloakClientSecret  |      UUID      | 87d05695-1911-44f6-a1bc-d04422df6209                                           | trustee client secret                                                    |

## Ingestion Verticle

| Key Name                | Value Datatype | Value Example | Description                                                              |
|:------------------------|:--------------:|:--------------|:-------------------------------------------------------------------------|
| isWorkerVerticle        |    boolean     | false         | To check if worker verticle needs to be deployed for blocking operations |
| verticleInstances       |    integer     | 1             | Number of instances required for verticles                               |
| resourceServerPort      |    integer     | 1234          | Port number to access HTTPS APIs of Resource Server                      |
| resourceServerBasePath  |     string     | /iudx/rs/v1   | API base path for DX Catalogue server.                                   |

## Resource Server Verticle

| Key Name          | Value Datatype | Value Example | Description                                                              |
|:------------------|:--------------:|:--------------|:-------------------------------------------------------------------------|
| isWorkerVerticle  |    boolean     | false         | To check if worker verticle needs to be deployed for blocking operations |
| verticleInstances |    integer     | 1             | Number of instances required for verticles                               |

