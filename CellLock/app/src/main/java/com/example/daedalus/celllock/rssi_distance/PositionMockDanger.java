package com.example.daedalus.celllock.rssi_distance;

public class PositionMockDanger implements PositionHandlerI {

  public PositionMockDanger() {
  }

  @Override
  public void setToNormalMode() {
  }

	@Override
	public boolean modeFromRssi(State state) {
	  state.mode = State.ProtectionMode.DANGER;
		return true;
	}

}
