package com.alex.utils;

public class Row {

    private String name;
    private int index;

    public Row(String name, int i) {
        this.index = i;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
