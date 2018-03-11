Mini-Redis
=================

## Core Commands
 - DBSIZE
 - KEYS
 - FLUSHDB (No param needed as we just have one DB)

## STRING Commands (key/value)
 - GET key
 - SET key value
 - SET key value EX seconds
 - DEL key
 - INCR key

## Sorted Set Commands
 First atemt on TreeSet, but redis allows duplicated scores, so i've switch to List,
 implementing Comparator interface.
 - ZADD key score member
 - ZCARD key
 - ZRANK key member
 - ZRANGE key start stop
 - ZREM key member

## Set Commands (extra)
 Implemented in top of HashMaps:
 - SADD key member
 - SCARD key
 - SREM key member
 - SINTER keyA keyB
 - SMEMBER key

##  Key Types
 All datastructures have common interface, enables a unique general key index, giving type validation (each key has its type, cannot be used for other types)

## Threading and Thread Safe model
 Key expiration is handled as tasks (Runnables) to be executed with delay;

 As redis is mostly single threaded, operations are linealized executed one after each other, atomically. With this idea in mind, i've created an execution queue handled by just one worker, the one that invokes all redis commands. Synchronization between http caller and delegated execution are done using Callables and futures ( so yes, we're blocking http handler thread until Callable response is done, under persistent connections asynch request/response will be fully unblocked).

 As maybe this is a controversial point, Redis service is full thread safe, so that, workerpool can be scaled to X workers. Trade off here is: linealization is a constraint, we can go with singlethreads. Vs we allow parallel executions, we can scale up workers threadpool.

## Network layer (testCases)
 Http server has been done using a minimalistic http framework: Spark server
 Basic response formatters are added to mimic redis-cli response behaviour.

#### String KeyVal
```
 curl localhost:8080/?cmd=SET%20mykey%20sdasd
OK

curl localhost:8080/?cmd=GET%20mykey
sdasd

 curl localhost:8080/?cmd=INCR%20foo
(integer) 1

 curl localhost:8080/?cmd=GET%20foo
1
```
 Set with expires
```
 curl localhost:8080/?cmd=SET%20fooo%20bar%205
OK
 curl localhost:8080/?cmd=GET%20fooo
bar
```
5 seconds later...
```
 curl localhost:8080/?cmd=GET%20fooo
(nil)

```
Status
```
 curl localhost:8080/?cmd=KEYS
0) "foo"
1) "mykey"

 curl localhost:8080/?cmd=DEL%20foo
OK
 curl localhost:8080/?cmd=KEYS
0) "mykey"
```
Try to increment non integer key
```
 curl localhost:8080/?cmd=INCR%20mykey
(error) ERR value is not an integer or out of range
```
Clean DB
```
 curl localhost:8080/?cmd=FLUSHDB
OK
 curl localhost:8080/?cmd=KEYS
(empty list or set)
```

#### Sorted Set commands
```
  curl localhost:8080/?cmd=ZADD%20mykey%201000%20cool-value
(integer) 1

 curl localhost:8080/?cmd=KEYS
0) "mykey"

 curl localhost:8080/?cmd=ZADD%20mykey%20100%20fooo
(integer) 1

 curl localhost:8080/?cmd=ZADD%20mykey%201900%20cool-vaasdasd
(integer) 1

 curl localhost:8080/?cmd=ZADD%20mykey%201900%20coXXX
(integer) 1

 curl localhost:8080/?cmd=ZCARD%20mykey
(integer) 4

 curl localhost:8080/?cmd=DBSIZE
(integer) 1

 curl localhost:8080/?cmd=KEYS
0) "mykey"
```
All Sorted set keys
```
  curl localhost:8080/?cmd=ZRANGE%20mykey%200%20100
0) "cool-vaasdasd"
1) "coXXX"
2) "cool-value"
3) "fooo"
```
First two entries
```
 curl localhost:8080/?cmd=ZRANGE%20mykey%200%202
0) "coXXX"
1) "cool-vaasdasd"
```
```
 curl localhost:8080/?cmd=ZREM%20mykey%20fooo
(integer) 1
 curl localhost:8080/?cmd=ZREM%20mykey%20fooo
(integer) 0
 curl localhost:8080/?cmd=ZRANGE%20mykey%200%20100
0) "coXXX"
1) "cool-vaasdasd"
```

#### SET
```
 curl localhost:8080/?cmd=SADD%20mykey%20fooo
(integer) 1
 curl localhost:8080/?cmd=SCARD%20mykey
(integer) 1
  curl localhost:8080/?cmd=SADD%20mykey%20Bar
(integer) 1
  curl localhost:8080/?cmd=SADD%20mykey%20fla
(integer) 1
  curl localhost:8080/?cmd=SADD%20mykey%20flaaaa
(integer) 1
 curl localhost:8080/?cmd=SCARD%20mykey
(integer) 4
 curl localhost:8080/?cmd=SMEMBERS%20mykey
0) "Bar"
1) "fooo"
2) "fla"
3) "flaaaa"
```
Set intersection
```
 curl localhost:8080/?cmd=SADD%20mykey1%20Bar
(integer) 1
 curl localhost:8080/?cmd=SADD%20mykey1%20fo
(integer) 1
 curl localhost:8080/?cmd=SADD%20mykey1%20fla
(integer) 1
 curl localhost:8080/?cmd=SMEMBERS%20mykey1
0) "Bar"
1) "fo"
2) "fla"

 curl localhost:8080/?cmd=SINTER%20mykey%20mykey1
0) "Bar"
1) "fla"
```
```
 curl localhost:8080/?cmd=SREM%20mykey1%20fla
(integer) 1
 curl localhost:8080/?cmd=SMEMBERS%20mykey1
0) "Bar"
1) "fo"
```

## Pendings and TODO
 - ZRANGE WITHSCORES: Current implementation will need to iterate ranged results, to add scores. As an alternative, SortedSet implementation may be changed to store not just keys, adding scores too. To allow this aprox we must store references to all items in a map... Trade off is: Cuadratic complexity Vs Memory
 - Expires are handled just as Seconds, will be better to add Milliseconds support too
 - General refactor, command segregation to avoid those ugly switches, remove loggers, and better error handling...
 - Many commands more, as ZINTERSTORE ... :)



