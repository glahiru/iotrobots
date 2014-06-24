package pano_ros;

public interface PanoCaptureFeedback extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "pano_ros/PanoCaptureFeedback";
  static final java.lang.String _DEFINITION = "# ====== DO NOT MODIFY! AUTOGENERATED FROM AN ACTION DEFINITION ======\n# Define a feedback message\nfloat32 n_captures\n\n";
  float getNCaptures();
  void setNCaptures(float value);
}