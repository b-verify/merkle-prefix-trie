# A Java Implementation of Merkle Prefix Tries (MPT) 

This is a full implementation of Merkle Prefix Tries that tries to exploit efficient serialization and client caching to reduce proof sizes in an online, dynamic setting

# Benchmarks
Serialization: the individual messages have an overhead of 2-6 bytes.
Stub - 36 bytes (32 byte hash + 4 byte overhead)
Leaf - k+v+6 bytes (key size + value size + 6 byte overhead)
InteriorNode  - 2 byte overhead 

Memory: generating a tree of 100k nodes by individually inserting 100k distinct nodes takes around 10 minutes. The worst case height of the tree is 31. Average height is much shorter. 


### Build
`$sh build.sh`

### Install
`$mvn install`

### Test
`$mvn test`
(I've made the test cases smaller to reduce time to run the tests - on my Macbook they run in ~17 seconds)