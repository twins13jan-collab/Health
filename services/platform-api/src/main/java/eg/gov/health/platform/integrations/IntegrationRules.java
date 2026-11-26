package eg.gov.health.platform.integrations;
/** Integration & message rules (cycle 234). Every message is traced to
 *  an ACK or a final failure — an HTTP 200 proves neither meaning nor
 *  effect. Duplicate messages are stopped by the idempotency key.
 *  Invalid payloads are quarantined in the error queue, never forced
 *  through. Failures retry with a graded backoff and land in the
 *  dead-letter queue with a reconciliation case. A vendor version
 *  change requires a compatibility test before activation. The
 *  operator reprocesses messages but never edits their content. */
public final class IntegrationRules {
  private IntegrationRules() {}
  public static final int MAX_ATTEMPTS = 3;
  /** Complete success needs an ACK *and* semantic verification. */
  public static boolean fullyAcknowledged(String ackType, boolean semanticVerified) {
    return "ACK".equals(ackType) && semanticVerified;
  }
  public static boolean deadLetter(int attempts) { return attempts >= MAX_ATTEMPTS; }
  /** Activation requires compatibility testing for the endpoint's version. */
  public static boolean activatable(boolean compatibilityTested) { return compatibilityTested; }
}
