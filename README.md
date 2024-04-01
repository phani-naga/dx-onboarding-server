
[![Build Status](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520onboarding%2520%28master%29%2520pipeline)](https://jenkins.iudx.io/job/iudx%20onboarding%20%28master%29%20pipeline/lastBuild/)
[![Jenkins Coverage](https://img.shields.io/jenkins/coverage/jacoco?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520onboarding%2520%28master%29%2520pipeline)](https://jenkins.iudx.io/job/iudx%20onboarding%20%28master%29%20pipeline/lastBuild/jacoco/)
[![Unit and Integration Tests](https://img.shields.io/jenkins/tests?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520onboarding%2520%28master%29%2520pipeline&label=unit%20and%20integration%20tests)](https://jenkins.iudx.io/job/iudx%20onboarding%20%28master%29%20pipeline/lastBuild/testReport/)
[![Security Tests](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520onboarding%2520%28master%29%2520pipeline&label=security%20tests)](https://jenkins.iudx.io/job/iudx%20onboarding%20%28master%29%20pipeline/lastBuild/zap/)

![IUDX](./docs/iudx.png)
# iudx-onboarding-server
The onboarding server is IUDX's helper server for onboarding items, instances and middle layer metadata in a federated Catalogue server model.


<p align="center">
<img src="./docs/onboarding_readme.drawio.png">
</p>

## Features
- Create, Update or Delete Items, Instances and middle layer metadata in federated catalogue using a single API call.
- Support for failsafe and retry on failure of request on any of the servers.
- Auto create exchange for resource groups on item creation.
- Scalable, service mesh architecture based implementation using open source components: Vert.X API framework
- Hazelcast and Zookeeper based cluster management and service discovery


## API Docs 
The api docs can be found [here](https://catalogue.iudx.org.in/apis).



## Get Started

### Prerequisite - Make configuration
Make a config file based on the template in `./configs/config-example.json` 
- Generate a certificate using Lets Encrypt or other methods
- Make a Java Keystore File and mention its path and password in the appropriate sections
- Modify the database url and associated credentials in the appropriate sections

### Docker based
1. Install docker and docker-compose
2. Clone this repo
3. Build the images 
   ` ./docker/build.sh`
4. Modify the `docker-compose.yml` file to map the config file you just created
5. Start the server in production (prod) or development (dev) mode using docker-compose 
   ` docker-compose up prod `


### Maven based
1. Install java 11 and maven
2. Use the maven exec plugin based starter to start the server 
   `mvn clean compile exec:java@onboarding-server`

### Redeployer
A hot-swappable redeployer is provided for quick development 
`./redeploy.sh`


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



### Testing

### Unit tests
1. Run the tests using `mvn clean test checkstyle:checkstyle pmd:pmd`  
2. Reports are stored in `./target/`


### Integration tests
Integration tests are through Rest Assured 
1. Run the server through either docker, maven or redeployer
2. Run the integration tests `mvn test-compile failsafe:integration-test -DskipUnitTests=true -DintTestHost=localhost -DintTestPort=8080`
3. Reports are stored in `./target/`



## Contributing
We follow Git Merge based workflow 
1. Fork this repository
2. Create a new feature branch in your fork. Multiple features must have a hyphen separated name, or refer to a milestone name as mentioned in Github -> Projects  
4. Commit to your fork and raise a Pull Request with upstream


## License
[View License](./LICENSE)

