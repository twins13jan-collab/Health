package eg.gov.health.platform.notify;
import java.util.*;
/** Notification-outbox rules (completion batch 2). A message
 *  is sent only when a channel actually delivered it; a
 *  channel with no configured gateway fails honestly and is
 *  never dressed up as sent. Delivery is attempted at most
 *  three times; after that the message is dead and visible as
 *  dead — silent loss is the one outcome this design forbids. */
public final class NotificationRules {
  private NotificationRules() {}
  public static final Set<String> CHANNELS = Set.of("in_app","webhook","sms","email");
  public static final int MAX_ATTEMPTS = 3;
  public static boolean knownChannel(String channel) { return channel != null && CHANNELS.contains(channel); }
  public static boolean dispatchable(String status, int attempts) {
    return "queued".equals(status) && attempts < MAX_ATTEMPTS;
  }
  public static String statusAfterFailure(int attempts) {
    return attempts >= MAX_ATTEMPTS ? "dead" : "queued";
  }
}
