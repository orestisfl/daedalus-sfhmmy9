package com.example.daedalus.celllock.rssi_distance;

public class ProtectionHandler implements PositionHandlerI {

  protected RssiHandlerI rssiHandler;
  protected int lastMode;
  protected double lastDistance;
  protected double lastTime;
  protected int counterRssi;

  protected boolean firstTimeInNormalTime;
  protected boolean firstTimeInNormalDistance;
  protected double firstTimeInWarning;

  protected double maxSpeed;
  protected double relativeDistThres;
  protected double relativeTimeThres;
  protected double maxDistanceThres;
  protected int minMeasRssi;

  protected double maxTimeInWarning;

  public ProtectionHandler() {
    rssiHandler = new FriisConverter();
    lastMode = State.NORMAL;
    firstTimeInNormalTime = true;
    firstTimeInNormalDistance = true;
    counterRssi = 0;
    maxSpeed = 1;
    relativeDistThres = 0.5;
    relativeTimeThres = relativeDistThres / maxSpeed;
    maxDistanceThres = 5.0;
    maxTimeInWarning = 2.0;
    minMeasRssi = 10;
  }

  @Override
  public void setToNormalMode() {
    lastMode = State.NORMAL;
    firstTimeInNormalTime = true;
    firstTimeInNormalDistance = true;
    counterRssi = 0;
  }

	@Override
	public boolean modeFromRssi(State state) {
	  double currentTime = System.currentTimeMillis();

	  if (lastMode == State.DANGER) {
	    state.mode = lastMode;
	    return true;
    }

	  rssiHandler.setCurrentRssi(state.rssiValue);
	  counterRssi++;

	  if (firstTimeInNormalTime) {
	    lastTime = currentTime;
	    firstTimeInNormalTime = false;
    }

	  if (currentTime - lastTime > relativeTimeThres || counterRssi >= minMeasRssi) {
	    counterRssi = 0;
	    double currentDistance = rssiHandler.distanceFromRssi();
	    if (firstTimeInNormalDistance) {
	      lastDistance = currentDistance;
	      firstTimeInNormalDistance = false;
      }
	    double relativeDistance = Math.abs(currentDistance - lastDistance);
	    if (relativeDistance > relativeDistThres) {
	      if (lastMode != State.WARNING)
          firstTimeInWarning = currentTime;
	      lastMode = State.WARNING;
      }
      else if (currentDistance < maxDistanceThres) {
        lastMode = State.NORMAL;
      }
      else {
        lastMode = State.DANGER;
      }
      if (lastMode != State.WARNING)
        lastDistance = currentDistance;
      lastTime = currentTime;
    }

    if (lastMode == State.WARNING) {
      if (currentTime - firstTimeInWarning > maxTimeInWarning) {
        lastMode = State.DANGER;
      }
    }

    state.mode = lastMode;
		return true;
	}

}
