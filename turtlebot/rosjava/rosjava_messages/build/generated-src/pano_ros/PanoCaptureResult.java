package pano_ros;

public interface PanoCaptureResult extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "pano_ros/PanoCaptureResult";
  static final java.lang.String _DEFINITION = "# ====== DO NOT MODIFY! AUTOGENERATED FROM AN ACTION DEFINITION ======\n# Define the result\nuint32 pano_id\nuint32 n_captures\nstring bag_filename\n";
  int getPanoId();
  void setPanoId(int value);
  int getNCaptures();
  void setNCaptures(int value);
  java.lang.String getBagFilename();
  void setBagFilename(java.lang.String value);
}
