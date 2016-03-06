package com.example.daedalus.celllock.rssi_distance;

import android.util.Log;

import java.util.ArrayList;

public class PositionHandlerFactory {

  static public PositionHandlerI spawnPositionHandler(String type, String arg) {
    if (arg == null) {
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
      // Log.w("PositionHandlerFactory", "File not found in experiment");
    }
    return null;
  }
}
