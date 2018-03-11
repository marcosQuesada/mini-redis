package com.marcosquesada.miniredis.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Set implements DataStructure{
    private HashSet set;

    public Set(){
        set = new HashSet();
    }


    public int sadd(String member){
        if (set.contains(member)) {
            return 0;
        }

        set.add(member);

        return 1;
    }

    public int srem(String member){
        if (!set.contains(member)) {
            return 0;
        }

        set.remove(member);

        return 1;
    }

    public Integer scard(){
        return set.size();
    }

    //SINTER key1 key2
    public static List<String> sinter(HashSet set1, HashSet set2) {
        HashSet s1 = new HashSet(set1);
        HashSet s2 = new HashSet(set2);
        Boolean success = s1.retainAll(s2);
        if (!success) {
            return new ArrayList<>();
        }

        return new ArrayList<>(s1);
    }

    public List<String> smembers(){

        return new ArrayList<>(set);
    }

    public Redis.DataStructureType getType(){
        return Redis.DataStructureType.SET;
    }

    public HashSet getSet() {
        return set;
    }
}
