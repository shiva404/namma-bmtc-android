package com.intuit.in24hr.namma_bmtc.types;

import java.io.Serializable;

public class BusRoute implements Serializable{
    private String busRoute;
    private String from;
    private String to;
    private String details;

    public BusRoute() {
    }

    public BusRoute(String busRoute, String from, String to, String details) {
        this.busRoute = busRoute;
        this.from = from;
        this.to = to;
        this.details = details;
    }

    public String getBusRoute() {
        return busRoute;
    }

    public void setBusRoute(String busRoute) {
        this.busRoute = busRoute;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "BusRoute{" +
                "busRoute='" + busRoute + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}