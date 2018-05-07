#!/bin/bash
export MAVEN_OPTS="-Xmx25G"
mvn exec:java -Dexec.mainClass=bench.MerklePathSizeBenchmark
