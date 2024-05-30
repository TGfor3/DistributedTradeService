# Client registration, positions and market value

**Team Members:** [Nir Solooki](mailto:nsolooki@mail.yu.edu>), [Tuvia Goldstein](mailto:tjgoldst@mail.yu.edu), [Asher Kirshtein](mailto:ackirsht@mail.yu.edu>)

## Project Plan

- [Scope and Use Cases](scope.md)
- [Distributed System Challenges](challenges.md)
- [Workflow Diagrams (BPMN)](workflow.md)
- [Software Architecture Diagrams (C4)](architecture.md)
- [Tools & Technologies](technologies.md)
- [Code](../services/T1Deploy/)
- [User Guide](user_guide.md)

## Overview

The goal of our group is to provide users with a live blotter displaying stock updates for the selected client. This is accomplished efficiently and reliably by carefully routing the incoming data to certain servers and colocating users accessing the same client to stream their updates from the same server.

We interface with Group 2 by
routing incoming requests for client registration and stock trading to their endpoints.

We interface with Group 3 by comsuming incoming data containing the updated market value for each stock each client holds. This data is then displayed to the end user.
