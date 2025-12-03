# dapm-thesis

This repository is inherited from the work of **Christian Becke** and **Zou Yong Nan Klaassen**  
and extended to support additional goals.

It is part of the DAPM (Distributed Architecture for Process Mining) **Online Data Streaming Pipelines** theses.

📖 **Documentation:**  
Read the original documentation here:  
[https://github.com/DAPM-Thesis/dapm-thesis/tree/main/dapm-pipeline/documentation](https://github.com/DAPM-Thesis/dapm-thesis/tree/main/dapm-pipeline/documentation)

> Original authors:  
> 1. Christian Becke  
> 2. Zou Yong Nan Klaassen  

---

## Projects

Each folder is a separate Maven project with its own `pom.xml`.

### 1. `dapm-pipeline`

Contains everything necessary to build streaming pipelines and includes template
code for creating Processing Elements (PEs):

- source  
- operator  
- sink  

### 2. `annotation-processor`

Provides message annotation functionality.  
This project is included in **`dapm-pipeline`** as a dependency.

---

## Building the JARs Locally

From the root directory:

```bash
cd dapm-pipeline
mvn clean

cd ../annotation-processor
mvn clean install

cd ../dapm-pipeline
mvn clean install
