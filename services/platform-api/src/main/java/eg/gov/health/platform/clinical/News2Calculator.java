package eg.gov.health.platform.clinical;
/** NEWS2 (RCP 2017) adult early-warning score — pure, fully tested.
 *  Advisory only: the score supports, never replaces, clinical judgement. */
public final class News2Calculator {
  private News2Calculator() {}
  public record Vitals(int respiratoryRate, int spo2, boolean onOxygen, int systolicBp, int pulse, double temperature, String consciousness) {}
  public record Score(int total, int rr, int spo2, int bp, int pulse, int temp, int consciousness, String riskBand) {}
  public static Score score(Vitals v) {
    int rr = v.respiratoryRate() <= 8 ? 3 : v.respiratoryRate() <= 11 ? 1 : v.respiratoryRate() <= 20 ? 0 : v.respiratoryRate() <= 24 ? 2 : 3;
    int spo2 = v.spo2() <= 91 ? 3 : v.spo2() <= 93 ? 2 : v.spo2() <= 95 ? 1 : 0;
    if (v.onOxygen()) spo2 += 2;
    int bp = v.systolicBp() <= 90 ? 3 : v.systolicBp() <= 100 ? 2 : v.systolicBp() <= 110 ? 1 : v.systolicBp() <= 219 ? 0 : 3;
    int pulse = v.pulse() <= 40 ? 3 : v.pulse() <= 50 ? 1 : v.pulse() <= 90 ? 0 : v.pulse() <= 110 ? 1 : v.pulse() <= 130 ? 2 : 3;
    int temp = v.temperature() <= 35.0 ? 3 : v.temperature() <= 36.0 ? 1 : v.temperature() <= 38.0 ? 0 : v.temperature() <= 39.0 ? 1 : 2;
    int cons = (v.consciousness() == null || v.consciousness().equalsIgnoreCase("alert")) ? 0 : 3;
    int total = rr + spo2 + bp + pulse + temp + cons;
    boolean anyThree = rr == 3 || spo2 >= 3 || bp == 3 || pulse == 3 || temp == 3 || cons == 3;
    String band = total >= 7 ? "high" : total >= 5 ? "medium" : (anyThree ? "low-medium" : "low");
    return new Score(total, rr, spo2, bp, pulse, temp, cons, band);
  }
}
