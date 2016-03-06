package com.example.daedalus.celllock.rssi_distance;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @brief Converts rssi value to distance between transmitter and receiver
 *
 * @details Uses a modified fitted Friis equation: power = A / (dist + B)^2 + C,
 *          where A, B and C are constants found by fitting on experimental data
 *          that represent directionality, wavelength of bluetooth technology
 *          (2.4GHz), constant transmission power of mobile phone device and
 *          errors due to modelling inefficiency, ambient noise on
 *          2.4Ghz band and measurement
 * @note User of this class should provide a file named "model_params.txt" which
 *       contains A, B, C parameters
 */
public class FriisConverter implements RssiHandlerI {

  private double A, B, C;
  protected ArrayList<Integer> lastRssiValues;

  public FriisConverter() {
    lastRssiValues = new ArrayList<Integer>();
    A = 7.099 * Math.pow(10, -7);
    C = -4.4382 * Math.pow(10, -6);
  }

  @Override
  public void setCurrentRssi(int rssiValue) {
    lastRssiValues.add(rssiValue);
  }

  @Override
  public double distanceFromRssi() {
    double filteredRssi = filterRssi();
    double distance = reverseModel(filteredRssi);
    lastRssiValues.clear();
    return distance;
  }

  protected double reverseModel(double meanRssi) {
    double power = Math.pow(10, meanRssi / 10);
    double transDist = Math.sqrt(A / (power - C));
    return transDist;
  }

  protected double filterRssi() {
    double meanBefore = meanRssi(lastRssiValues);
    double stdBefore = stdRssi(lastRssiValues, meanBefore);
    double meanAfter = 0;
    if (lastRssiValues.size() < 3) {
      meanAfter = meanBefore;
    }
    else {
      ArrayList<Integer> rssiListAfter = new ArrayList<Integer>();
      for (Integer x: lastRssiValues) {
        if ((x <= meanBefore + stdBefore) && (x >= meanBefore - stdBefore)) {
          rssiListAfter.add(x);
        }
      }
      meanAfter = meanRssi(rssiListAfter);
    }
    return meanAfter;
  }

  protected double meanRssi(ArrayList<Integer> rssiList) {
    double mean = 0;
    for (Integer x: rssiList) {
      mean += x;
    }
    mean /= rssiList.size();
    return mean;
  }

  protected double stdRssi(ArrayList<Integer> rssiList, double meanRssi) {
    double std = 0;
    for (Integer x: rssiList) {
      std += (x - meanRssi) * (x - meanRssi);
    }
    std /= rssiList.size();
    std = Math.sqrt(std);
    return std;
  }

}
