package si.um.feri.leaf.utils;

import java.util.Date;

public class AirMarker {
    public double lat;
    public double lng;
    public String station;
    public Date date;
    private String markerStyle;
    private Integer pm25;
    private Integer pm10;

    public void setMarkerStyle(String markerStyle) {
        this.markerStyle = markerStyle;
    }

    public Integer getPm25() {
        return pm25;
    }

    public void setPm25(Integer pm25) {
        this.pm25 = pm25;
    }

    public Integer getPm10() {
        return pm10;
    }

    public void setPm10(Integer pm10) {
        this.pm10 = pm10;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    private Date timestamp;


    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMarkerStyle() {
        return markerStyle;
    }


    public AirMarker(double lat, double lng, String station, Date date, String markerStyle, Integer pm25, Integer pm10) {
        this.lat = lat;
        this.lng = lng;
        this.station = station;
        this.date= date;
        this.markerStyle=markerStyle;
        this.pm25=pm25;
        this.pm10=pm10;
    }
}
