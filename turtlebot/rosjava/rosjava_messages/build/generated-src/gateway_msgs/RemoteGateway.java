package gateway_msgs;

public interface RemoteGateway extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "gateway_msgs/RemoteGateway";
  static final java.lang.String _DEFINITION = "###### Gateway information ######\nstring name\nstring ip\n#TODO blocking status,health\nbool firewall\n\n###### Public Interface ######\n\nRule[] public_interface\n\n\n###### Flipped Interface ######\n\n# Flipped and pulled interfaces would be useful for debugging \n#    https://github.com/robotics-in-concert/rocon_multimaster/issues/84\n\nRemoteRule[] flipped_interface\nRemoteRule[] pulled_interface\n\n###### Foreign Interface ######\n\n# Q) Should we show these?\n# A) Probably not, in the overall scheme of things, \n#    it doubles up the information from above\n\n# RemoteRule[] flipped_in_connections\n# RemoteRule[] pulled_connections\n";
  java.lang.String getName();
  void setName(java.lang.String value);
  java.lang.String getIp();
  void setIp(java.lang.String value);
  boolean getFirewall();
  void setFirewall(boolean value);
  java.util.List<gateway_msgs.Rule> getPublicInterface();
  void setPublicInterface(java.util.List<gateway_msgs.Rule> value);
  java.util.List<gateway_msgs.RemoteRule> getFlippedInterface();
  void setFlippedInterface(java.util.List<gateway_msgs.RemoteRule> value);
  java.util.List<gateway_msgs.RemoteRule> getPulledInterface();
  void setPulledInterface(java.util.List<gateway_msgs.RemoteRule> value);
}
