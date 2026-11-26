package eg.gov.health.platform.reports;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*;
/** National reports & open data (cycle 236). Indicators are owned,
 *  versioned and recomputable; snapshots carry their cutoff and coverage;
 *  low coverage is labelled, never generalised; definition changes issue
 *  new versions with comparability warnings; small counts are suppressed;
 *  publication needs national approval and de-identification, and the
 *  public reaches published datasets only. Corrections are notices with
 *  a reason, an impact and a replacement version. */
@RestController @RequestMapping("/api/national-reports")
public class NationalReportController {
  private final IndicatorDefinitionRepository indicators; private final DataSnapshotRepository snapshots;
  private final AggregationJobRepository jobs; private final PublicationDatasetRepository datasets;
  private final CorrectionNoticeRepository corrections; private final AuditService audit;
  public NationalReportController(IndicatorDefinitionRepository indicators, DataSnapshotRepository snapshots,
                                  AggregationJobRepository jobs, PublicationDatasetRepository datasets,
                                  CorrectionNoticeRepository corrections, AuditService audit) {
    this.indicators=indicators; this.snapshots=snapshots; this.jobs=jobs; this.datasets=datasets;
    this.corrections=corrections; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private IndicatorDefinition indicator(UUID id) {
    return indicators.findById(id).orElseThrow(() -> BizException.notFound("indicator_not_found","المؤشر غير موجود"));
  }

  public record DefinitionRequest(String code, String name, String formula, String numeratorDef, String denominatorDef,
                                  String exclusions, String sourceSystem, String owner, LocalDate effectiveFrom, String changeNote) {}
  @PostMapping("/indicators")
  Map<String,Object> define(@RequestBody DefinitionRequest r) {
    var p = principal();
    if (r.code()==null||r.name()==null||r.owner()==null)
      throw BizException.badRequest("invalid_definition","الكود والاسم والمالك مطلوبة — كل مؤشر له مالك ومصدر وإصدار");
    var existing = indicators.findByCodeOrderByVersionDesc(r.code());
    int version = existing.isEmpty() ? 1 : existing.get(0).getVersion() + 1;
    boolean comparabilityWarning = version > 1; // a changed definition breaks series homogeneity
    var d = indicators.save(new IndicatorDefinition(UUID.fromString(p.facilityId()), p.governorate(), r.code(), r.name(),
      r.formula(), r.numeratorDef(), r.denominatorDef(), r.exclusions(), r.sourceSystem(), r.owner(), version, r.effectiveFrom()));
    audit.record("report.indicator.defined","indicator_definition",d.getId().toString(),"{}");
    return Map.of("ok",true,"definition",d,"version",version,
      "comparability_warning", comparabilityWarning,
      "note", comparabilityWarning ? "definition changed: new version issued — do not compare across versions without this warning" : "initial version");
  }
  @PostMapping("/indicators/{id}/approve")
  Map<String,Object> approve(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var d = indicator(id);
    if (!d.getStatus().equals("draft")) throw BizException.conflict("not_draft","التعريف ليس مسودة");
    if (body.get("approvedBy")==null || !body.get("approvedBy").equals(d.getOwner()))
      throw BizException.badRequest("owner_approval","مالك المؤشر هو من يعتمد تعريفه");
    d.approve(); indicators.save(d);
    audit.record("report.indicator.approved","indicator_definition",id.toString(),"{}");
    return Map.of("ok",true,"definition",d);
  }
  @GetMapping("/indicators/compare")
  Map<String,Object> compare(@RequestParam String code, @RequestParam int fromVersion, @RequestParam int toVersion) {
    return Map.of("ok",true,"code",code,
      "comparability_warning_required", ReportRules.comparabilityWarningRequired(fromVersion, toVersion),
      "note", fromVersion==toVersion ? "same definition version — series is homogeneous"
             : "definitions differ between versions — comparison must carry a heterogeneity warning");
  }

  public record SnapshotRequest(Integer participatingFacilities, Integer expectedFacilities, Boolean anyLate) {}
  @PostMapping("/indicators/{id}/snapshots")
  Map<String,Object> snapshot(@PathVariable UUID id, @RequestBody SnapshotRequest r) {
    var p = principal(); var d = indicator(id);
    int part = r.participatingFacilities()==null?0:r.participatingFacilities();
    int exp = r.expectedFacilities()==null?0:r.expectedFacilities();
    var s = snapshots.save(new DataSnapshot(d.getFacilityId(), p.governorate(), id, part, exp, Boolean.TRUE.equals(r.anyLate())));
    audit.record("report.snapshot.taken","data_snapshot",s.getId().toString(),"{}");
    return Map.of("ok",true,"snapshot",s,
      "national_claim_allowed", ReportRules.nationalClaimAllowed(s.getCoveragePct().doubleValue()),
      "note","a late facility appears as coverage — the figure is never generalised to the whole country");
  }

  public record JobRequest(Integer rulesVersion, String scope, BigDecimal numeratorValue, BigDecimal denominatorValue) {}
  @PostMapping("/snapshots/{id}/aggregate")
  Map<String,Object> aggregate(@PathVariable UUID id, @RequestBody JobRequest r) {
    var p = principal();
    var s = snapshots.findById(id).orElseThrow(() -> BizException.notFound("snapshot_not_found","اللقطة غير موجودة"));
    if (r.numeratorValue()==null||r.denominatorValue()==null)
      throw BizException.badRequest("values_required","البسط والمقام مطلوبان — المؤشر قابل لإعادة الحساب دائمًا");
    var j = jobs.save(new AggregationJob(snapFacility(s), p.governorate(), id,
      r.rulesVersion()==null?1:r.rulesVersion(), r.scope(), r.numeratorValue(), r.denominatorValue()));
    audit.record("report.job.computed","aggregation_job",j.getId().toString(),"{}");
    return Map.of("ok",true,"job",j,"recomputable",true,
      "published_value", j.isSmallCountSuppressed() ? "<5 (suppressed)" : String.valueOf(j.getComputedValue()),
      "small_count_suppressed", j.isSmallCountSuppressed());
  }
  private UUID snapFacility(DataSnapshot s) { return sFacility(s); }
  private UUID sFacility(DataSnapshot s) { return s.getFacilityId(); }
  @PostMapping("/jobs/{id}/validate")
  Map<String,Object> validate(@PathVariable UUID id) {
    var j = jobs.findById(id).orElseThrow(() -> BizException.notFound("job_not_found","مهمة التجميع غير موجودة"));
    j.validate(); jobs.save(j);
    audit.record("report.job.validated","aggregation_job",id.toString(),"{}");
    return Map.of("ok",true,"job",j);
  }

  public record PublishRequest(String fields, String aggregationLevel, Boolean deidentified, String license, String approvedBy) {}
  @PostMapping("/jobs/{id}/publish")
  Map<String,Object> publish(@PathVariable UUID id, @RequestBody PublishRequest r) {
    var p = principal();
    var j = jobs.findById(id).orElseThrow(() -> BizException.notFound("job_not_found","مهمة التجميع غير موجودة"));
    var snap = snapshots.findById(j.getSnapshotId()).orElseThrow(() -> BizException.notFound("snapshot_not_found","اللقطة غير موجودة"));
    var def = indicator(snap.getIndicatorId());
    if (!ReportRules.publishable(def.getStatus(), j.getStatus(), Boolean.TRUE.equals(r.deidentified()), r.approvedBy()))
      throw BizException.badRequest("not_publishable","النشر يتطلب تعريفًا معتمدًا وتجميعًا متحققًا وإزالة هوية واعتماد الجهة القومية");
    var ds = datasets.save(new PublicationDataset(j.getFacilityId(), p.governorate(), id, r.fields(),
      r.aggregationLevel(), Boolean.TRUE.equals(r.deidentified()), r.license(), r.approvedBy()));
    j.publish(); jobs.save(j);
    audit.record("report.dataset.published","publication_dataset",ds.getId().toString(),"{}");
    return Map.of("ok",true,"dataset",ds,"public_access","published datasets only");
  }
  public record CorrectionRequest(String reason, String impact, String replacementVersion, String issuedBy) {}
  @PostMapping("/datasets/{id}/corrections")
  Map<String,Object> correct(@PathVariable UUID id, @RequestBody CorrectionRequest r) {
    var p = principal();
    var ds = datasets.findById(id).orElseThrow(() -> BizException.notFound("dataset_not_found","مجموعة البيانات غير موجودة"));
    if (r.reason()==null||r.reason().isBlank())
      throw BizException.badRequest("reason_required","تصحيح منشور يتطلب سببًا وأثرًا وإصدارًا بديلًا");
    var c = corrections.save(new CorrectionNotice(dsFacility(ds), p.governorate(), id, r.reason(), r.impact(), r.replacementVersion(), r.issuedBy()));
    audit.record("report.correction.issued","correction_notice",c.getId().toString(),"{}");
    return Map.of("ok",true,"correction",c);
  }
  private UUID dsFacility(PublicationDataset d) { return d.getFacilityId(); }

  @GetMapping("/public/datasets")
  Map<String,Object> publicDatasets() {
    var rows = datasets.findAll();
    return Map.of("ok",true,"datasets",rows,"count",rows.size(),
      "note","the public reaches published, de-identified datasets only — never facility-level raw data");
  }
}
