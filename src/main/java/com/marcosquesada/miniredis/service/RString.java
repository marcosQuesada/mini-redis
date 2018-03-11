package com.marcosquesada.miniredis.service;

public class RString implements DataStructure{

    private String value;

    public RString() {
    }

    public RString(String value) {
        this.value = value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    //Throws NumberFormatException
    public Integer incr() {
        Integer counter = Integer.parseInt(value);

        counter++;

        value = String.valueOf(counter);

        return counter;
    }

    public Redis.DataStructureType getType(){
        return Redis.DataStructureType.STRING;
    }
}
