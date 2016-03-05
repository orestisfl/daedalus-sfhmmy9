package com.example.daedalus.celllock.rssi_distance;

public interface RssiHandlerI {
  public void setCurrentRssi(int rssiValue);
  public double distanceFromRssi();
}
