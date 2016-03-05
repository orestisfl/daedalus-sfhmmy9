package rssi_distance;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class PositionExperimentHandler implements PositionHandlerI {

  private PrintWriter writer;

  public PositionExperimentHandler(ArrayList<String> filename)
      throws FileNotFoundException, UnsupportedEncodingException {
    writer = new PrintWriter(filename.get(0), "UTF-8");
  }

  public void closeWriter() {
    writer.close();
  }

  @Override
  public void setToNormalMode() {
  }

  @Override
  public boolean modeFromRssi(State state) {
    writer.println(state.rssiValue);
    return true;
  }

}
