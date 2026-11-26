package eg.gov.health.platform.medicalrecords;

import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class RecordRulesTest {

  @Test void completeFileHasNoDeficiencies() {
    assertTrue(RecordRules.deficiencies(true, "final", true).isEmpty());
  }

  @Test void everyGapIsNamed() {
    var d = RecordRules.deficiencies(false, null, false);
    assertEquals(3, d.size());
    assertTrue(d.contains("clinical_note_missing"));
    assertTrue(d.contains("discharge_summary_missing"));
    assertTrue(d.contains("icd10_coding_missing"));
    assertEquals(java.util.List.of("discharge_summary_not_final"),
        RecordRules.deficiencies(true, "draft", true));
    assertTrue(RecordRules.deficiencies(true, "final", false).contains("icd10_coding_missing"));
    assertTrue(RecordRules.deficiencies(false, "final", true).contains("clinical_note_missing"));
  }

  @Test void icdFormatValidation() {
    assertTrue(RecordRules.icdFormatValid("I10"));
    assertTrue(RecordRules.icdFormatValid("J18.9"));
    assertTrue(RecordRules.icdFormatValid("o80"));
    assertTrue(RecordRules.icdFormatValid("Z38.0"));
    assertFalse(RecordRules.icdFormatValid("123"));
    assertFalse(RecordRules.icdFormatValid("ABC"));
    assertFalse(RecordRules.icdFormatValid("I1"));
    assertFalse(RecordRules.icdFormatValid(""));
    assertFalse(RecordRules.icdFormatValid(null));
  }

  @Test void filingOverdueAfterSla() {
    var now = OffsetDateTime.now();
    assertFalse(RecordRules.filingOverdue(now.minusDays(14), now));
    assertTrue(RecordRules.filingOverdue(now.minusDays(15), now));
    assertFalse(RecordRules.filingOverdue(now.minusDays(2), now));
    assertFalse(RecordRules.filingOverdue(null, now));
  }

  @Test void attentionRankOrdersOverdueThenReadyThenRest() {
    assertEquals(0, RecordRules.attentionRank(false, true));
    assertEquals(0, RecordRules.attentionRank(true, true));
    assertEquals(1, RecordRules.attentionRank(true, false));
    assertEquals(2, RecordRules.attentionRank(false, false));
    assertTrue(RecordRules.attentionRank(false, true) < RecordRules.attentionRank(true, false));
    assertTrue(RecordRules.attentionRank(true, false) < RecordRules.attentionRank(false, false));
  }
}
