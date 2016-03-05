package com.example.daedalus.celllock.rssi_distance;

import android.util.Log;

import java.util.ArrayList;

public class PositionHandlerFactory {

  static public PositionHandlerI spawnPositionHandler(String type, ArrayList<String> args) {
    try {
      switch (type) {
        case "experiment":
          return new PositionExperimentHandler(args);
        case "true":
          return new ProtectionHandler();
      }
    }
    catch (Exception e) {
      Log.w("PositionHandlerFactory", "File not found in experiment");
    }
    return null;
  }
}
