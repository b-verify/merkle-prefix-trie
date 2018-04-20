export MAVEN_OPTS="-Xmx4092M"
mvn exec:java -Dexec.mainClass=bench.MerklePathSizeBenchmark
