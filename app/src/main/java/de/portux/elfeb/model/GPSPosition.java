package de.portux.elfeb.model;

public class GPSPosition {

  public final double latitude;
  public final double longitude;

  public static GPSPosition of(double latitude, double longitude) {
    return new GPSPosition(latitude, longitude);
  }

  private GPSPosition(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }
}
