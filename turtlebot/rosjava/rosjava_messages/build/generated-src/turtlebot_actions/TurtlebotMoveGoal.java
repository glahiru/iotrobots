package turtlebot_actions;

public interface TurtlebotMoveGoal extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "turtlebot_actions/TurtlebotMoveGoal";
  static final java.lang.String _DEFINITION = "# ====== DO NOT MODIFY! AUTOGENERATED FROM AN ACTION DEFINITION ======\n#goal definition\nfloat32 turn_distance     # in radians, ccw = +, cw = -\nfloat32 forward_distance  # in meters, forward = +, backward = -\n";
  float getTurnDistance();
  void setTurnDistance(float value);
  float getForwardDistance();
  void setForwardDistance(float value);
}
