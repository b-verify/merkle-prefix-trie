#!/bin/bash
export MAVEN_OPTS="-Xmx8G"
mvn exec:java -Dexec.mainClass=bench.MerklePathSizeBenchmark
