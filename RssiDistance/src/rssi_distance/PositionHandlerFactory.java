package rssi_distance;

public class PositionHandlerFactory {

  static public PositionHandlerI spawnPositionHandler(String type, String arg) {
    if (type == null) {
      return null;
    }
    try {
      if (type.equalsIgnoreCase("experiment")) {
        return new PositionExperimentHandler(arg);
      }
      else if (type.equalsIgnoreCase("real")) {
        return new ProtectionHandler();
      }
    }
    catch (Exception e) {
    }
    return null;
  }

}
