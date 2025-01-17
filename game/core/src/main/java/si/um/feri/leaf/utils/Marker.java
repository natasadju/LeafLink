package si.um.feri.leaf.utils;

public class Marker {
    public double lat;
    public double lng;
    public String eventName;
    public String date;
    public String description;

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

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Marker(double lat, double lng, String eventName, String date, String description) {
        this.lat = lat;
        this.lng = lng;
        this.eventName= eventName;
        this.date= date;
        this.description= description;
    }
}
