package rssi_distance;

public class ProtectionHandler implements PositionHandlerI {

  protected RssiHandlerI rssiHandler;
  protected State.ProtectionMode lastMode;
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
    lastMode = State.ProtectionMode.NORMAL;
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
    lastMode = State.ProtectionMode.NORMAL;
    firstTimeInNormalTime = true;
    firstTimeInNormalDistance = true;
    counterRssi = 0;
  }

	@Override
	public boolean modeFromRssi(State state) {
	  double currentTime = System.currentTimeMillis();

	  if (lastMode == State.ProtectionMode.DANGER) {
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
	      if (lastMode != State.ProtectionMode.WARNING)
          firstTimeInWarning = currentTime;
	      lastMode = State.ProtectionMode.WARNING;
      }
      else if (currentDistance < maxDistanceThres) {
        lastMode = State.ProtectionMode.NORMAL;
      }
      else {
        lastMode = State.ProtectionMode.DANGER;
      }
      if (lastMode != State.ProtectionMode.WARNING)
        lastDistance = currentDistance;
      lastTime = currentTime;
    }

    if (lastMode == State.ProtectionMode.WARNING) {
      if (currentTime - firstTimeInWarning > maxTimeInWarning) {
        lastMode = State.ProtectionMode.DANGER;
      }
    }

    state.mode = lastMode;
		return true;
	}

}
