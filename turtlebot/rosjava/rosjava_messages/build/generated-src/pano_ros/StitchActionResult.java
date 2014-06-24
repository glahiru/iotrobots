package pano_ros;

public interface StitchActionResult extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "pano_ros/StitchActionResult";
  static final java.lang.String _DEFINITION = "# ====== DO NOT MODIFY! AUTOGENERATED FROM AN ACTION DEFINITION ======\n\nHeader header\nactionlib_msgs/GoalStatus status\nStitchResult result\n";
  std_msgs.Header getHeader();
  void setHeader(std_msgs.Header value);
  actionlib_msgs.GoalStatus getStatus();
  void setStatus(actionlib_msgs.GoalStatus value);
  pano_ros.StitchResult getResult();
  void setResult(pano_ros.StitchResult value);
}