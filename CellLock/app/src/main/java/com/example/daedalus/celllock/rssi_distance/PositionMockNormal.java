package com.example.daedalus.celllock.rssi_distance;

public class PositionMockNormal implements PositionHandlerI {

  public PositionMockNormal() {
  }

  @Override
  public void setToNormalMode() {
  }

	@Override
	public boolean modeFromRssi(State state) {
	  state.mode = State.ProtectionMode.NORMAL;
		return true;
	}

}
