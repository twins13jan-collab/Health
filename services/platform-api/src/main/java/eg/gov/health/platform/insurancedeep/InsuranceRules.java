package eg.gov.health.platform.insurancedeep;
/** Insurance rules (module 113). A claim is adjudicated only
 *  when the performed service matches its documentation and
 *  the tariff. Medical necessity is recorded independently of
 *  the coverage decision — a payer's denial never rewrites
 *  what was medically necessary; the two questions live in two
 *  columns forever. And a fraud signal is reviewed by a human
 *  before it can stop a settlement: a flag is a suspicion, not
 *  a verdict. */
public final class InsuranceRules {
  private InsuranceRules() {}
  public static boolean adjudicable(boolean documentationComplete, boolean tariffMatch) {
    return documentationComplete && tariffMatch;
  }
  public static boolean settleable(boolean fraudFlag, boolean fraudHumanReviewed) {
    return !fraudFlag || fraudHumanReviewed;
  }
}
