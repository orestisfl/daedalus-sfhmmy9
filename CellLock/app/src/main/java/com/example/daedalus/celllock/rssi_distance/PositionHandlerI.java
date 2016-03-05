package com.example.daedalus.celllock.rssi_distance;

public interface PositionHandlerI {
  public void setToNormalMode();
  public boolean modeFromRssi(State state);
}
