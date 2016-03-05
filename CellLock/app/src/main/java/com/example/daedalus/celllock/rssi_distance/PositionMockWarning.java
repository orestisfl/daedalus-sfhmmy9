package com.example.daedalus.celllock.rssi_distance;

public class PositionMockWarning implements PositionHandlerI {

  public PositionMockWarning() {
  }

  @Override
  public void setToNormalMode() {
  }

	@Override
	public boolean modeFromRssi(State state) {
	  state.mode = State.ProtectionMode.WARNING;
		return true;
	}

}
