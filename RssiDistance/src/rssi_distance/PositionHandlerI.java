package rssi_distance;

import rssi_distance.State;

public interface PositionHandlerI {
  public void setToNormalMode();
  public boolean modeFromRssi(State state);
}
