package com.marcosquesada.miniredis.service;

import java.util.*;

/**
 * 7. ZADD key score member
 * 8. ZCARD key
 * 9. ZRANK key member
 * 10. ZRANGE key start stop
 */
public class SortedSet implements DataStructure{

    private String key;
    private Map<String, Long> scores;
    private ArrayList<String> zset;
    private Comparator<String> comparator;

    public SortedSet(String key) {
        this.key = key;
        this.zset = new ArrayList<>();
        this.scores = new HashMap<>();

        this.comparator = new Comparator<String>() {
            @Override
            public int compare(String key1, String key2) {
                Long score1 = scores.get(key1);
                Long score2 = scores.get(key2);

                return score2.compareTo(score1);
            }
        };
    }

    public int zadd(Long score, String member) {
        if (scores.containsKey(member)) {
            scores.put(member, score);
            Collections.sort(zset, comparator);

            return 0;
        }

        scores.put(member, score);
        zset.add(member);

        Collections.sort(zset, comparator);

        return 1;
    }


    public Integer zcard() {
        return zset.size();
    }


    public Integer zrank(String member) {
        if (!scores.containsKey(member)) {
            return null;
        }

        return zset.indexOf(member);
    }


    //@TODO: AS EXTRA ADD WITHSCORES
    public List<String> zrange(Integer start, Integer stop) {
        Integer size = stop - start;
        if (size <= 0) {
            return new ArrayList<>();
        }

        if (start >= zset.size()) {
            return new ArrayList<>();
        }

        if (size > zset.size()) {
            stop = zset.size();
        }

        return new ArrayList<>(zset.subList(start, stop));
    }

    public int zrem(String member) {
        if (!scores.containsKey(member)) {
            return 0;
        }
        scores.remove(member);
        zset.remove(member);

        return 1;
    }

    // used on ZINTERSTORE DEVELOPMENT
    public ArrayList<String> getZset() {
        return zset;
    }

    public Redis.DataStructureType getType(){
        return Redis.DataStructureType.SORTED_SET;
    }
}