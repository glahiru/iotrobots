package turtlebot_actions;

public interface FindFiducialActionGoal extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "turtlebot_actions/FindFiducialActionGoal";
  static final java.lang.String _DEFINITION = "# ====== DO NOT MODIFY! AUTOGENERATED FROM AN ACTION DEFINITION ======\n\nHeader header\nactionlib_msgs/GoalID goal_id\nFindFiducialGoal goal\n";
  std_msgs.Header getHeader();
  void setHeader(std_msgs.Header value);
  actionlib_msgs.GoalID getGoalId();
  void setGoalId(actionlib_msgs.GoalID value);
  turtlebot_actions.FindFiducialGoal getGoal();
  void setGoal(turtlebot_actions.FindFiducialGoal value);
}
