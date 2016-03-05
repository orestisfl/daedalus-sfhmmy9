package rssi_distance;

import java.util.ArrayList;

public class PositionHandlerFactory {

  public PositionHandlerFactory() {
  }

  public PositionHandlerI spawnPositionHandler(String type, ArrayList<String> args) {
    try {
      switch (type) {
        case "experiment":
          return new PositionExperimentHandler(args);
        case "true":
          return new ProtectionHandler();
      }
    }
    catch (Exception e) {
      Log.w("File not found in experiment");
    }
    return null;
  }
}
