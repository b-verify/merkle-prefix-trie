export MAVEN_OPTS="-Xmx4G"
mvn exec:java -Dexec.mainClass=bench.MerklePathSizeBenchmark
