
public class Test {
  private native void nativeInit(int colorFormat);
  private native void nativeDestroy();
  public native void consumeNalUnitsFromDirectBuffer(String nalUnits, int numBytes, long packetPTS);
  public native boolean isFrameReady();
  public native int getWidth();
  public native int getHeight();
  public native int getOutputByteSize();
  public native long decodeFrameToDirectBuffer(String buffer);

  public static void main(String args[]) {

  }
}
