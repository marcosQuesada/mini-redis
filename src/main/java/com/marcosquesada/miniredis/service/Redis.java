package com.marcosquesada.miniredis.service;

import com.marcosquesada.miniredis.executor.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 1. SET key value
 * 2. SET key value EX seconds (need not implement other SET options)
 * 3. GET key
 * 4. DEL key
 * 5. DBSIZE
 * 6. INCR key
 * 7. ZADD key score member
 * 8. ZCARD key
 * 9. ZRANK key member
 * 10. ZRANGE key start stop
 */
public class Redis {

    public enum DataStructureType {
        STRING, SET, SORTED_SET;
    }

    private static final Logger logger = LoggerFactory.getLogger(Redis.class);

    public static final String SET = "SET";
    public static final String GET = "GET";
    public static final String DEL = "DEL";
    public static final String INCR = "INCR";

    public static final String ZADD = "ZADD";
    public static final String ZCARD = "ZCARD";
    public static final String ZRANK = "ZRANK";
    public static final String ZRANGE = "ZRANGE";
    public static final String ZREM = "ZREM";

    public static final String SADD = "SADD";
    public static final String SCARD = "SCARD";
    public static final String SREM = "SREM";
    public static final String SINTER = "SINTER";
    public static final String SMEMBERS = "SMEMBERS";

    public static final String DBSIZE = "DBSIZE";
    public static final String KEYS = "KEYS";
    public static final String FLUSHDB = "FLUSHDB";

    // keys to dataStructure
    private Map<String, DataStructure> data;
    private Executor executor;

    public Redis(Executor exec) {
        executor = exec;
        data = new HashMap<>();
    }

    public synchronized String set(String key, String value) {
        logger.info("SET key {} value {}", key, value);

        RString val = fetchString(key);
        val.setValue(value);

        return "OK";
    }

    //SET key value [EX seconds]
    public synchronized String set(String key, String value, Long expiration) {
        logger.info("Called SETX key {} value {} expiration {}", key, value, expiration);

        RString val = fetchString(key);
        val.setValue(value);

        addExpiration(key, expiration);
        return "OK";
    }

    public synchronized String get(String key) {
        DataStructure item = data.get(key);
        if (item == null) {
            return "(nil)";
        }

        RString value = (RString) item;
        logger.info("GET key {}, value {}", key, value.getValue());

        if (value.getValue() == "") {
            return "(nil)";
        }

        return value.getValue();
    }

    public synchronized String del(String key) {
        logger.info("DEL key {}", key);
        data.remove(key);

        return "OK";
    }

    // Return the number of keys in the currently-selected database.
    public synchronized Integer dbsize() {
        Integer totalKeys = data.size();
        logger.info("DBSIZE {}", totalKeys);

        return totalKeys;
    }


    public synchronized Integer incr(String key) {
        logger.info("INCR key {}", key);

        return fetchStringAsInt(key).incr();
    }

    public synchronized Integer zadd(String key, Long score, String member) {
        logger.info("Called ZADD key {} score {} member {}", key, score, member);

        SortedSet zset = fetchZset(key);

        return zset.zadd(score, member);
    }

    // Get the number of members in a sorted set
    public synchronized Integer zcard(String key) {
        logger.info("ZCARD key {}", key);
        SortedSet zset = fetchZset(key);

        return zset.zcard();
    }

    // Determines the index of a member in the sorted set O(log(N))
    public synchronized Integer zrank(String key, String member) {
        logger.info("ZRANK key {} member {}", key, member);
        SortedSet zset = fetchZset(key);

        return zset.zrank(member);
    }

    // Returns a range of members where position in sorted set goes from start to stop
    public synchronized List<String> zrange(String key, Integer start, Integer stop) {
        logger.info("Called ZRANGE key {} start {} stop {}", key, start, stop);
        DataStructure item = data.get(key);
        if (item == null) {
            return new ArrayList<>();
        }
        SortedSet zset = validateZSETKey(key);

        return zset.zrange(start, stop);
    }

    public synchronized Integer zrem(String key, String member) {
        logger.info("Called ZREM key {} member {}", key, member);
        DataStructure item = data.get(key);
        if (item == null) {
            return 0;
        }
        SortedSet zset = validateZSETKey(key);

        return zset.zrem(member);
    }

    public synchronized Integer sadd(String key, String member) {
        logger.info("SADD key {} member {}", key, member);
        Set set = fetchSet(key);

        return set.sadd(member);
    }

    public synchronized Integer scard(String key) {
        logger.info("SCARD key {} ", key);
        Set set = fetchSet(key);

        return set.scard();
    }

    public synchronized Integer srem(String key, String member) {
        logger.info("SREM key {} member {}", key, member);
        Set set = fetchSet(key);

        return set.srem(member);
    }

    public synchronized List<String> sinter(String key1, String key2) {
        logger.info("SINTER key1 {} key2 {}", key1, key2);
        Set set1 = fetchSet(key1);
        Set set2 = fetchSet(key2);

        return Set.sinter(set1.getSet(), set2.getSet());

    }

    public synchronized List<String> smembers(String key) {
        logger.info("SMEMBERS key {}", key);
        Set set1 = fetchSet(key);

        return set1.smembers();
    }

    public synchronized String flushDB() {
        logger.info("FLUSHDB");
        data = new HashMap<>();

        return "OK";
    }

    public synchronized List<String> keys() {
        logger.info("KEYS");

        return new ArrayList<>(data.keySet());
    }

    // addExpiration in seconds
    private void addExpiration(String key, Long delay) {
        logger.info("ADD EXPIRATION!");
        executor.schedule(() -> {
            logger.info("EXECUTING!");
            del(key);
        }, delay * 1000); // Convert to Milliseconds
    }

    private RString fetchString(String key) {
        data.putIfAbsent(key, new RString());

        return validateStringKey(key);
    }

    private RString fetchStringAsInt(String key) {
        data.putIfAbsent(key, new RString("0"));

        return validateStringKey(key);
    }

    private SortedSet fetchZset(String key) {
        data.putIfAbsent(key, new SortedSet(key));

        return validateZSETKey(key);
    }

    private Set fetchSet(String key) {
        data.putIfAbsent(key, new Set());

        return validateSETKey(key);
    }

    private RString validateStringKey(String key) {
        DataStructure item = data.get(key);
        if (item != null && item.getType() != DataStructureType.STRING) {
            throw new RuntimeException("value is not an integer or out of range");
        }

        return (RString) item;
    }

    private SortedSet validateZSETKey(String key) {
        DataStructure item = data.get(key);
        if (item != null && item.getType() != DataStructureType.SORTED_SET) {
            throw new RuntimeException("value is not an SortedSet or out of range");
        }

        return (SortedSet) item;
    }

    private Set validateSETKey(String key) {
        DataStructure item = data.get(key);
        if (item != null && item.getType() != DataStructureType.SET) {
            throw new RuntimeException("value is not an Set or out of range");
        }

        return (Set) item;
    }

}
