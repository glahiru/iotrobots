package turtlebot_actions;

public interface TurtlebotMoveAction extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "turtlebot_actions/TurtlebotMoveAction";
  static final java.lang.String _DEFINITION = "# ====== DO NOT MODIFY! AUTOGENERATED FROM AN ACTION DEFINITION ======\n\nTurtlebotMoveActionGoal action_goal\nTurtlebotMoveActionResult action_result\nTurtlebotMoveActionFeedback action_feedback\n";
  turtlebot_actions.TurtlebotMoveActionGoal getActionGoal();
  void setActionGoal(turtlebot_actions.TurtlebotMoveActionGoal value);
  turtlebot_actions.TurtlebotMoveActionResult getActionResult();
  void setActionResult(turtlebot_actions.TurtlebotMoveActionResult value);
  turtlebot_actions.TurtlebotMoveActionFeedback getActionFeedback();
  void setActionFeedback(turtlebot_actions.TurtlebotMoveActionFeedback value);
}
