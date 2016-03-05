package rssi_distance;

public class State {
  public enum ProtectionMode {
    NORMAL, WARNING, DANGER
  }
  public int rssiValue;
  public ProtectionMode mode;
}