package com.intuit.in24hr.namma_bmtc.types;


import java.io.Serializable;

/**
 *
 */
public class Location implements Serializable {
    private String routeNumber;
    private String crowdLevel;
    private Double latitude;
    private Double longitude;
    private String refToken;



    public String getRefToken() {
        return refToken;
    }

    public void setRefToken(String refToken) {
        this.refToken = refToken;
    }

    public Location(String refToken, Double latitude, Double longitude,  String routeNumber, String crowdLevel) {
        this.crowdLevel = crowdLevel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.refToken = refToken;
        this.routeNumber = routeNumber;
    }

    public Location() {
    }

    public Location(Double latitude, Double longitude) {

        this.latitude = latitude;
        this.longitude = longitude;
    }

//    public Map<String, Object> getMap(){
//        HashMap<String, Object> hashMap = new HashMap<>(4);
//        hashMap.put("routeNumber", routeNumber);
//        hashMap.put("crowdLevel", crowdLevel);
//        hashMap.put("latitude", latitude);
//        hashMap.put("longitude", longitude);
//        return hashMap;
//    }


    public Location(String routeNumber, Double latitude, Double longitude, String crowdLevel) {
        this.crowdLevel = crowdLevel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.routeNumber = routeNumber;
    }

    public String getCrowdLevel() {
        return crowdLevel;
    }

    public void setCrowdLevel(String crowdLevel) {
        this.crowdLevel = crowdLevel;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    public void setRouteNumber(String routeNumber) {
        this.routeNumber = routeNumber;
    }

    @Override
    public String toString() {
        return "Location{" +
                "routeNumber='" + routeNumber + '\'' +
                ", crowdLevel='" + crowdLevel + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", refToken='" + refToken + '\'' +
                '}';
    }
}
