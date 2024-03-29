package rocon_app_manager_msgs;

public interface Invite extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "rocon_app_manager_msgs/Invite";
  static final java.lang.String _DEFINITION = "# Invite the application manager to send (or cancel) the app manager control handles \n# (/start_app etc) to a remote target. That target is usually the client\'s gateway name.\n\nstring remote_target_name\n\n# Set up the default application namespace - there are typically three optinos here:\n#  - an absolute namespace\n#  - a relative namespace, in which case it will reside underneath the app manager namespace\n#  - unset, in which case it defaults to the relative namespace \'application\'\nstring application_namespace\nbool cancel\n---\nbool result\n";
}
