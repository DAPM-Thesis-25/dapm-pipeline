# dapm-thesis
This repository is a facet of the DAPM (Distributed Architecture for Process Mining) Online Data Streaming Pipelines theses. 

Read documentation [here](https://github.com/DAPM-Thesis/dapm-thesis/tree/main/dapm-pipeline/documentation)

The repository is shared by three DAPM groups:
1. Christian Becke, Zou Yong Nan Klaassen
2. Reshma Zaman, Tama Sarker
3. Hussein Dirani, Raihanullah Mehran
## Projects
Each folder is a separate Maven project with its own `pom.xml` file.

### 1. **dapm-pipeline**
Contains everything necessary to build pipelines and also includes template code for creating processing elements (source, operator, sink).

### 2. **annotation-processor**
Provides message annotation functionality. This project is included in **dapm-pipeline** as a dependency.

## Building the JARs Locally
Run these steps from the root directory:

```
cd dapm-pipeline
mvn clean

cd ../annotation-processor
mvn clean install

cd ../dapm-pipeline
mvn clean install
```

Then include the local JAR build as a dependency in another project:
```xml
<dependency>
  <groupId>com.github.dapm-thesis</groupId>
    <artifactId>dapm-pipeline</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```


## Jitpack
Instead of using the local JAR builds, you can use [Jitpack](https://jitpack.io/#DAPM-Thesis/dapm-thesis), an easier way to add the latest version of **dapm-pipeline** as a dependency in other projects.

A `jitpack.yml` in the project root ensures only the **dapm-pipeline** and **annotation-processor** folders are included in the build.

```yml
jdk:
  - openjdk21
install:
  - wget https://downloads.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
  - tar -xvzf apache-maven-3.9.6-bin.tar.gz
  - export M2_HOME=$PWD/apache-maven-3.9.6
  - export PATH=$M2_HOME/bin:$PATH
  - mvn -version

  - cd dapm-pipeline && mvn clean && cd ..
  - cd annotation-processor && mvn clean install -DskipTests && cd ..
  - cd dapm-pipeline && mvn clean install -DskipTests
  ```

### Get Started with Jitpack (Build)
To push a new build to Jitpack, create and push a git tag:

```
git tag v1.0.0
git push origin v1.0.0
```
Use a new version tag for each release since Jitpack only builds on new tags.

Find the latest Jitpack version [here](https://jitpack.io/#DAPM-Thesis/dapm-thesis).



### Get started with Jitpack (dependency)
To use the Jitpack build in another project, add this to your project's `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.DAPM-Thesis</groupId>
        <artifactId>dapm-thesis</artifactId>
        <version>v1.0.0</version>
    </dependency>
</dependencies>
```
Update the version as needed. Then run: `mvn clean install`# dapm-pipeline
