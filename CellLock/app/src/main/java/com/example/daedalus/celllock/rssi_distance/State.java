package com.example.daedalus.celllock.rssi_distance;

public class State {
  public final static int NORMAL = 0;
  public final static int WARNING = 1;
  public final static int DANGER = 2;
  public int rssiValue;
  public int mode;
}
