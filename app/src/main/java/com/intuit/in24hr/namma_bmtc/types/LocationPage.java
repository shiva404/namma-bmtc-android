package com.intuit.in24hr.namma_bmtc.types;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class LocationPage implements Serializable {
    private List<Location> locations;
    private int offset;
    private int size;

    public List<Location> getLocations() {
        if(locations == null)
            locations = new LinkedList<>();
        return locations;
    }


    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
