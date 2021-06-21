![](https://github.com/voutilad/paysim/workflows/Java%20CI/badge.svg)

# A Fork of PaySim - Simulating Mobile Money Networks

This is a fork of [PaySim 2.0](https://github.com/EdgarLopezPhD/PaySim) designed for use as a library while maintaining
some ability to run standalone (if desired).

It's first usage is in conjunction with [Neo4j](https://neo4j.com) to create a network graph, facilitating application
of graph algorithms for detecting fraud characteristics.

## Why Fork?

Numerous high-level enhancements from the original include:

- Implementation of First and Third Party fraudsters
    - 1st Party Fraudsters steal or use fake identities to open new accounts and drain their value
    - 3rd Party Fraudsters prey upon normal clients via "compromised" merchants (like via card skimming) and drain
      accounts
- Incorporation of "identities" tied to clients to help facilitate First Party Fraud simulation including SSN, Email,
  and Phone Numbers

For developers looking to leverage PaySim in an embedded sense, major changes include:

- `Parameters` used Java `statics` for simulation params preventing concurrent usage and easy testing, so remove
  reliance on statics.
- Relying on deducing the type of the actor based on their name prefix was annoying, so explicitly track
  a `SuperActor.Type` making it easier to know without string nonsense
- PaySim was originally implemented as a single class relying on file IO, so abstract out the common
  parts (`PaySimState`) allowing for creation of an implementation that doesn't require writing simulation results to
  the file system
- Embedding PaySim was hard due to the file IO requirements, so in conjunction with `PaySimState` implement an in-memory
  implementation that produces consumable results. (Current implementation is a `java.util.Iterator<Transaction>` that
  allows the simulation to get ahead of the consumer but 200k transactions using
  a `java.util.concurrent.ArrayBlockingQueue` as a buffer.)
- MASON, the simulation framework, contains a LOT of features not needed for PaySim, so instead this project relies
  on [my fork](https://github.com/voutilad/mason) that slims it down.
- Incorporation of [SL4j](http://www.slf4j.org/) as a logging framework instead of reliance purely on `System.out`
  /`System.err`

## Getting Started

It's pretty easy, all you need is a JDK environment.

- Get and [install](https://adoptopenjdk.net) a recent JDK 11 instance for your platform. (Note: JDK 8 may work, but I'm
  not testing with it at the moment.)
- Clone this repo...I can't provide full pre-packaged releases due to licensing caveats. (Long story short: AFL3 and
  GPL3 don't mix!)
- Install a copy of [Apache Maven](https://maven.apache.org/download.cgi) for your system

In the project directory, simply run:

```shell script
$ mvn install
```

The above will both install the paysim library in your local Maven repo as well as create an easy to use "uberjar" in
the `target` directory.

To run the standalone PaySim like in the original project, you'd then run:

```shell script
$ java -jar ./target/paysim-2.1.0.jar
```

### Embedding and Using in other Projects

Until I host builds somewhere, the easiest way to get the code is via [jitpack.io](https://jitpack.io).

#### Using Maven

Add the jitpack repository:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add the dependency:

```xml
<dependency>
    <groupId>com.github.voutilad</groupId>
    <artifactId>paysim</artifactId>
    <version>2.1.0</version>
</dependency>
```

#### Using Gradle

Add the jitpack repository (assuming you use Groovy) to your `build.gradle` at the end of the repos list:

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

And the dependency:

```groovy
dependencies {
        implementation 'com.github.voutilad:paysim:2.1.0'
}
```

### Developing and Extending

If you'd like to create your own PaySim customization, extend the abstract `PaySimState` class.

See `org.paysim.IteratingPaySim` as an example.

## About the required Properties file and paramFiles

Currently PaySim expects a handful of properties files to initialize the simulation state. You need to provide:

- PaySim.properties -- the primary settings file
- Supporting properties files -- see the `paramFiles` directory

I recommend copying the existing ones in the project if you're adding PaySim to another project.

> Note: the current version of PaySim requires aggregate financial transaction data to generate the simulated transactions. As such, it's capped at ~720 steps for now.

---

# Original README.md pre-fork

## Project Leader

Dr. Edgar Lopez-Rojas
http://edgarlopez.net

More on PaySim: http://edgarlopez.net/simulation-tools/paysim/

Dataset sample: https://www.kaggle.com/ntnu-testimon/paysim1

## Description

PaySim, a Mobile Money Payment Simulator The Mobile Money Payment Simulation case study is based on a real company that
has developed a mobile money implementation that provides mobile phone users with the ability to transfer money between
themselves using the phone as a sort of electronic wallet. The task at hand is to develop an approach that detects
suspicious activities that are indicative of fraud. Unfortunately, during the initial part of our research this service
was only been running in a demo mode. This prevented us from collecting any data that could had been used for analysis
of possible detection methods. The development of PaySim covers two phases. During the first phase, we modelled and
implemented a MABS that used the schema of the real mobile money service and generated synthetic data following
scenarios that were based on predictions of what could be possible when the real system starts operating. During the
second phase we got access to transactional financial logs of the system and developed a new version of the simulator
which uses aggregated transactional data to generate financial information more alike the original source. Kaggle has
featured PaySim1 as dataset of the week of april 2018. See the full
article: http://blog.kaggle.com/2017/05/01/datasets-of-the-week-april-2017/

## PaySim first paper of the simulator:

Please refer to this dataset using the following citations:

E. A. Lopez-Rojas , A. Elmir, and S. Axelsson. "PaySim: A financial mobile money simulator for fraud detection". In: The
28th European Modeling and Simulation Symposium-EMSS, Larnaca, Cyprus. 2016

## Acknowledgements

This work is part of the research project ”Scalable resource-efficient systems for big data analytics” funded by the
Knowledge Foundation (grant: 20140032) in Sweden.

Master's thesis: Elmir A. PaySim Financial Simulator : PaySim Financial Simulator [Internet] [Dissertation]. 2016.
Available from: http://urn.kb.se/resolve?urn=urn:nbn:se:bth-14061

2016 PhD Thesis Dr. Edgar Lopez-Rojas
http://bth.diva-portal.org/smash/record.jsf?pid=diva2%3A955852&dswid=-1552

2019 Contribution by Camille Barneaud (https://github.com/gadcam) and the company Flaminem (https://www.flaminem.com/)
implementation of Money Laundering cases
