# A Java Implementation of Authenticated Dictionaries and Authenticated Sets Using Merkle Prefix Tries (MPT) 
[![Build Status](https://travis-ci.org/henryaspegren/b_verify-merkle-prefix-trie.svg?branch=master)](https://travis-ci.org/henryaspegren/b_verify-merkle-prefix-trie)

This is a full implementation of <b>Authenticated Dictionaries</b> (<i>a set of key-value mappings</i>) and <b>Authenticated Sets</b> (<i>a set of values</i>) using Merkle Prefix Tries. This datastructure allows very small lograthmic proofs of membership and non-membership. These data structures also support updates for use in a dynamic setting. These updates are very small because the implementation exploits client caching to avoid retransmitting unchanged internal values.

# Benchmarks
We benchmark using a dictionary with 
`10^7 key, pairs`
with 32-byte keys and 32-byte values which we can generate in a few minutes - most of the generation time is due to garbage collection pauses
`Total key, value data 		(bytes): 640000000 (0.64 GB)`
`Entire mpt proof size  	(bytes): 824397218 (0.82 GB)`
`Average path proof size 	(bytes): 1116 (1.1 KB)`
`Max path proof size     	(bytes): 1433 (1.4 KB)'

The bottleneck is the amount of heap space available to the JVM. Also as the heap is filled up the garbage collection pauses become more noticable

To reproduce these benchmarks run
`$sh benchmark.sh`


# Serialization 
Serialization: the individual messages have an overhead of 2-6 bytes.
Stub - 36 bytes (32 byte hash + 4 byte overhead)
Leaf - k+v+6 bytes (key size + value size + 6 byte overhead)
InteriorNode  - 2 byte overhead 

### Build
`$sh build.sh`

### Install
`$mvn install`

### Test
`$mvn test`
