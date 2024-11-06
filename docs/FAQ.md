<p align="center">
<img src="./cdpg.png" width="300">
</p>

# Frequently Asked Questions (FAQs)

1. How do I request for a new feature to be added or change in an existing feature?
- Please create an issue [here](https://github.com/datakaveri/iudx-catalogue-server/issues)
2. What is the purpose of the Onboarding Server?
- The Onboarding Server is designed to manage metadata for data sources provided by various providers, enabling the cataloging, searching, and updating of resources. It serves as a metadata management layer for onboarding resources in a federated Catalogue setup.
3. What APIs are available in the Onboarding Server?
- The Onboarding Server supports CRUD operations for items, instances, domains, and other resource metadata. For example, APIs include:
  - /item - Manage items (create, read, update, delete)
  - /internal/ui/instance - Instance management for cataloging specific cities/locations 
  - /internal/ui/domain - Domain management for cataloguing purposes
4. How does the system handle high load and scalability?
- The system uses a service mesh architecture that allows specific containers to scale as needed. The EventBus and Hazelcast clustering also facilitate real-time communication and load balancing.
5. Who are the typical users of the Onboarding Server?
- The main users are data providers, catalog administrators, and authorized systems interacting 
   with the data exchange platform for metadata management. It is also used by municipal corporations, smart cities, and other organizational entities.
6. How do I configure the Onboarding Server?
- Configuration details are outlined in the [Configurations document](./Configurations.md). It 
  provides information necessary to connect the Onboarding Server with the DX Catalogue Server, DX Resource Server, and Keycloak.