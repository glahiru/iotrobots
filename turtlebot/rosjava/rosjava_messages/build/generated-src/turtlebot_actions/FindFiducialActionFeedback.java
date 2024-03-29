package turtlebot_actions;

public interface FindFiducialActionFeedback extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "turtlebot_actions/FindFiducialActionFeedback";
  static final java.lang.String _DEFINITION = "# ====== DO NOT MODIFY! AUTOGENERATED FROM AN ACTION DEFINITION ======\n\nHeader header\nactionlib_msgs/GoalStatus status\nFindFiducialFeedback feedback\n";
  std_msgs.Header getHeader();
  void setHeader(std_msgs.Header value);
  actionlib_msgs.GoalStatus getStatus();
  void setStatus(actionlib_msgs.GoalStatus value);
  turtlebot_actions.FindFiducialFeedback getFeedback();
  void setFeedback(turtlebot_actions.FindFiducialFeedback value);
}
