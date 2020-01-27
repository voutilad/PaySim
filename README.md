![](https://github.com/voutilad/paysim/workflows/Java%20CI/badge.svg)

This is a fork of PaySim designed for use as a library. 

Key changes from the original include:
- `Parameters` used statics for simulation params preventing concurrent usage and easy testing, so remove reliance on statics.
- Relying on deducing the type of the actor based on their name prefix was annoying, so explicitly track a `SuperActor.Type` making it easier to know without string nonsense
- PaySim was originally implemented as a single class relying on file IO, so abstract out the common parts (`PaySimState`) allowing for creation of an implementation that doesn't require writing simulation results to the file system
- Embedding PaySim was hard due to the file IO requirements, so in conjunction with `PaySimState` implement an in-memory implementation that produces consumable results. (Current implementation is a `java.util.Iterator<Transaction>` that allows the simulation to get ahead of the consumer but 200k transactions using a `java.util.concurrent.ArrayBlockingQueue` as a buffer.)

## Getting Started
There are a few prerequisites:

- Get and install a recent JDK 11 instance for your platform
- You'll need to grab my fork of [mason](https://github.com/voutilad/mason) and install it in your local maven repo. (Simply `mvn install`.)

### Embedding
You can build a standalone uberjar using `mvn package`. It'll produce a jar in `./target` containing all dependencies.

By default, the original PaySim class is wired up as the entry point, so the following will execute a simulation outputting to the file system:
```bash
$ java -jar target/paysim-2.0-voutilad-4.jar
```

Feel free to take the jar and add it to your project or application.

### Extending
If you'd like to create your own PaySim customization, extend the abstract `PaySimState` class.

See `org.paysim.IteratingPaySim` as an example.

## About the required Properties files
Currently PaySim expects a handful of properties files to initialize the simulation state. You need to provide:

- PaySim.properties -- the primary settings file
- Supporting properties files -- see the `paramFiles` directory

I recommend copying the existing ones in the project and distributing them with the jar.

> Note: the current version of PaySim requires aggregate financial transaction data to generate the simulated transactions. As such, it's capped at ~720 steps for now.

---
# Original README.md
## Project Leader

Dr. Edgar Lopez-Rojas
http://edgarlopez.net

More on PaySim: http://edgarlopez.net/simulation-tools/paysim/

Dataset sample: https://www.kaggle.com/ntnu-testimon/paysim1

## Description

PaySim, a Mobile Money Payment Simulator The Mobile Money Payment Simulation case study is based on a real company that has developed a mobile money implementation that provides mobile phone users with the ability to transfer money between themselves using the phone as a sort of electronic wallet. The task at hand is to develop an approach that detects suspicious activities that are indicative of fraud. Unfortunately, during the initial part of our research this service was only been running in a demo mode. This prevented us from collecting any data that could had been used for analysis of possible detection methods. The development of PaySim covers two phases. During the first phase, we modelled and implemented a MABS that used the schema of the real mobile money service and generated synthetic data following scenarios that were based on predictions of what could be possible when the real system starts operating. During the second phase we got access to transactional financial logs of the system and developed a new version of the simulator which uses aggregated transactional data to generate financial information more alike the original source. Kaggle has featured PaySim1 as dataset of the week of april 2018. See the full article: http://blog.kaggle.com/2017/05/01/datasets-of-the-week-april-2017/

## PaySim first paper of the simulator:

Please refer to this dataset using the following citations:

E. A. Lopez-Rojas , A. Elmir, and S. Axelsson. "PaySim: A financial mobile money simulator for fraud detection". In: The 28th European Modeling and Simulation Symposium-EMSS, Larnaca, Cyprus. 2016


## Acknowledgements
This work is part of the research project ”Scalable resource-efficient systems for big data analytics” funded by the Knowledge Foundation (grant: 20140032) in Sweden.

Master's thesis: Elmir A. PaySim Financial Simulator : PaySim Financial Simulator [Internet] [Dissertation]. 2016. Available from: http://urn.kb.se/resolve?urn=urn:nbn:se:bth-14061

2016 PhD Thesis Dr. Edgar Lopez-Rojas
http://bth.diva-portal.org/smash/record.jsf?pid=diva2%3A955852&dswid=-1552

2019 Contribution by Camille Barneaud (https://github.com/gadcam) and the company Flaminem (https://www.flaminem.com/) implementation of Money Laundering cases
