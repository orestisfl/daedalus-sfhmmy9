package com.example.daedalus.celllock.rssi_distance;

public class PositionMockFalse implements PositionHandlerI {


  public PositionMockFalse() {
  }

  @Override
  public void setToNormalMode() {
  }

	@Override
	public boolean modeFromRssi(State state) {
	  state.mode = State.ProtectionMode.NORMAL;
		return false;
	}

}
