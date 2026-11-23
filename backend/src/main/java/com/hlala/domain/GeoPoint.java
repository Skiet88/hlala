package com.hlala.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class GeoPoint {

    // Boxed on purpose: Hibernate makes primitive columns NOT NULL, and a room's pin is optional.
    private Double latitude;
    private Double longitude;

    protected GeoPoint() {
    }

    public GeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /** Haversine distance in meters - used by screening to check a checker's visit location. */
    public double distanceMetersTo(GeoPoint other) {
        double r = 6371000;
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
