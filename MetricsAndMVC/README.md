# Real-time prices + system observability

**Team Members:** [Eli Levy](mailto:emlevy1@mail.yu.edu), [Oze Botach](mailto:oze@obotach.com), [Shmuel Newmark](mailto:snewmark@mail.yu.edu)

## Project Plan

- [Scope and Use Cases](scope.md)
- [Distributed System Challenges](challenges.md)
- [Workflow Diagrams (BPMN)](workflow.md)
- [Software Architecture Diagrams (C4)](architecture.md)
- [Tools & Technologies](technologies.md)

## Overview

Our team had three goals: to provide a way for other teams to easily access stock prices in real time, to recalculate client portfolio market value in real time, and to collect monitoring information from across the system and display that information visually. 

The real time prices interface directly with team 2 as they need to provide accurate quotes for clients looking to place buy/sell orders. 

The market value calculator ingests portfolio updates (buy/sell actions) from team 2 and recalculates the market value of the affect portfolio. The recalculated value is published to a kafka topic so that team 1 can read the update and display it to interested clients.

The observability framework directly consumes data from every machine running in the system. 
