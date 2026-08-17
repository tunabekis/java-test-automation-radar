# Java Test Automation Radar

[![CI](https://github.com/AstoKE/java-test-automation-radar/actions/workflows/ci.yml/badge.svg)](https://github.com/AstoKE/java-test-automation-radar/actions/workflows/ci.yml)

A small Java simulation of a proximity radar system, built as a vehicle for
practicing software testing techniques: unit testing, parameterized testing,
basis-path testing, mutation testing, and mocked integration testing.

## What it does

A `Radar` scans a list of `Target` objects positioned on a 2D plane around it
(the radar sits at the origin). For each target it:

1. Computes distance and bearing from the radar.
2. Discards targets outside `maxRangeKm`.
3. Computes a signal-to-noise ratio (SNR) from the target's radar
   cross-section (RCS) and distance, discarding targets below `snrThreshold`.
4. Classifies the remaining targets into a `ThreatLevel` (`LOW`, `MEDIUM`,
   `HIGH`) based on how far above the threshold their SNR is.

Each surviving target becomes a `Detection`, which also exposes a
`assessDetectionPriority()` method that ranks it from `PRIORITY_1_INTERCEPT`
(close and high threat) down to `PRIORITY_4_LOG`.

## Project structure

```
src/main/java/edu/tedu/radar/
  Radar.java                    - scans targets and produces detections
  Target.java                   - a detectable object with position and RCS
  Detection.java                 - a scan result: distance, bearing, SNR, threat, priority
  ThreatLevel.java                - LOW / MEDIUM / HIGH classification

src/test/java/edu/tedu/radar/
  Target_Test.java               - unit tests for Target
  Detection_Test.java            - unit tests for Detection, including basis-path
                                    coverage of assessDetectionPriority()
  Radar_Test.java                - unit and parameterized tests for Radar,
                                    including basis-path coverage of scan()
  RadarIntegrationTest.java      - Mockito-based integration tests exercising
                                    Radar.scan() against mocked Target objects
  UnitCSmokeSuite.java           - JUnit 5 suite grouping the unit tests
  FunctionalCCoreLogicSuite.java - JUnit 5 suite grouping the core-logic tests
```

## Tech stack

- Java 21
- Maven
- JUnit 5 (Jupiter, parameterized tests, platform suites)
- Mockito 5 (unit-level mocking for integration tests)
- PIT (pitest) for mutation testing

## Building and running

Requires JDK 21+ and Maven.

Run the full test suite:

```bash
mvn test
```

Run mutation testing (generates an HTML report under `target/pit-reports/`):

```bash
mvn org.pitest:pitest-maven:mutationCoverage
```

## Opening in an IDE

1. Open this folder as an existing Maven project.
2. Ensure the project SDK is set to JDK 21 or higher.
3. Source lives in `src/main/java`, tests in `src/test/java`.
