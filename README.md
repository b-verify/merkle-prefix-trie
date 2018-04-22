# A Java Implementation of Authenticated Dictionaries and Authenticated Sets Using Merkle Prefix Tries (MPT) 
[![Build Status](https://travis-ci.org/henryaspegren/b_verify-merkle-prefix-trie.svg?branch=master)](https://travis-ci.org/henryaspegren/b_verify-merkle-prefix-trie)

This is a full implementation of <b>Authenticated Dictionaries</b> (<i>a set of key-value mappings</i>) and <b>Authenticated Sets</b> (a set of values) using Merkle Prefix Tries. This datastructure allows very small lograthmic proofs of membership and non-membership. These data structures also support updates for use in a dynamic setting. These updates are very small because the implementation exploits client caching to avoid retransmitting unchanged internal values.

# Benchmarks
For a dictionary with 
`10^6  entries`
which we can generate in less than a minute, here are the stats:

`Entire tree proof size 		(bytes): 50789842`

`Average path proof size 	(bytes): 1021`

`Max path proof size     	(bytes): 1908`

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
(I've made the test cases smaller to reduce time to run the tests - on my Macbook they run in ~17 seconds)
