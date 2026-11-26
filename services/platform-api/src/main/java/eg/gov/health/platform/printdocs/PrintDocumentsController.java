package eg.gov.health.platform.printdocs;
import eg.gov.health.platform.common.*;
import eg.gov.health.platform.pharmacy.*;
import eg.gov.health.platform.diagnostics.*;
import eg.gov.health.platform.finance.*;
import eg.gov.health.platform.admissionsdeep.*;
import eg.gov.health.platform.patient.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Printable official documents rendered from stored records. */
@RestController @RequestMapping("/api/print-documents")
public class PrintDocumentsController {
  private final PdfDocumentService pdf;
  private final PrescriptionRepository prescriptions; private final LabOrderRepository labOrders; private final LabResultRepository labResults;
  private final InvoiceRepository invoices; private final InvoiceLineRepository invoiceLines;
  private final DischargeSummaryRepository summaries; private final PatientRepository patients;
  public PrintDocumentsController(PdfDocumentService pdf, PrescriptionRepository prescriptions, LabOrderRepository labOrders,
      LabResultRepository labResults, InvoiceRepository invoices, InvoiceLineRepository invoiceLines,
      DischargeSummaryRepository summaries, PatientRepository patients) {
    this.pdf=pdf; this.prescriptions=prescriptions; this.labOrders=labOrders; this.labResults=labResults;
    this.invoices=invoices; this.invoiceLines=invoiceLines; this.summaries=summaries; this.patients=patients;
  }
  private void principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
  }
  private String patientName(UUID patientId) {
    return patients.findById(patientId).map(pt -> pt.getFullName() + " — " + pt.getNationalId()).orElse(patientId.toString());
  }
  private ResponseEntity<byte[]> pdf(String name, byte[] bytes) {
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
      .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + name + ".pdf\"").body(bytes);
  }
  @GetMapping("/prescription/{id}")
  ResponseEntity<byte[]> prescription(@PathVariable UUID id) {
    principal();
    var rx = prescriptions.findById(id).orElseThrow(() -> BizException.notFound("rx_not_found","الوصفة غير موجودة"));
    return pdf("prescription-" + id, pdf.render("وصفة طبية", id.toString(), List.of(
      new String[]{"المريض", patientName(rx.getPatientId())},
      new String[]{"الدواء", rx.getDrugName() + " (" + rx.getDrugCode() + ")"},
      new String[]{"الجرعة", rx.getDose()},
      new String[]{"التكرار", rx.getFrequency()},
      new String[]{"المدة", rx.getDuration()},
      new String[]{"الحالة", rx.getStatus()},
      new String[]{"الطبيب المعالج", rx.getPrescribedBy()}),
      List.of("تُصرف هذه الوصفة من صيدلية المنشأة بعد التحقق من الحساسية الدوائية.")));
  }
  @GetMapping("/lab-report/{orderId}")
  ResponseEntity<byte[]> labReport(@PathVariable UUID orderId) {
    principal();
    var o = labOrders.findById(orderId).orElseThrow(() -> BizException.notFound("order_not_found","الطلب غير موجود"));
    var results = labResults.findByOrderIdOrderByVersionDesc(orderId);
    var rows = new ArrayList<String[]>(List.of(
      new String[]{"المريض", patientName(o.getPatientId())},
      new String[]{"الفحص", o.getTestName() + " (" + o.getTestCode() + ")"},
      new String[]{"الأولوية", o.getPriority()},
      new String[]{"حالة الطلب", o.getStatus()}));
    for (var r : results) {
      rows.add(new String[]{"النتيجة", String.valueOf(r.getValue()) + " " + (r.getUnit() == null ? "" : r.getUnit())
        + " — المرجع: " + r.getRefLow() + " إلى " + r.getRefHigh() + (r.isCritical() ? " — قيمة حرجة" : "")});
    }
    return pdf("lab-report-" + orderId, pdf.render("تقرير معمل", orderId.toString(), rows,
      List.of("النتيجة المعتمدة هي أحدث إصدار موثق في سجل الطلب.")));
  }
  @GetMapping("/invoice/{id}")
  ResponseEntity<byte[]> invoice(@PathVariable UUID id) {
    principal();
    var inv = invoices.findById(id).orElseThrow(() -> BizException.notFound("invoice_not_found","الفاتورة غير موجودة"));
    var rows = new ArrayList<String[]>(List.of(
      new String[]{"المريض", patientName(inv.getPatientId())},
      new String[]{"جهة الدفع", inv.getPayer()},
      new String[]{"الحالة", inv.getStatus()}));
    for (var l : invoiceLines.findByInvoiceId(id))
      rows.add(new String[]{"بند", l.getDescription() + " × " + l.getQuantity() + " — " + l.getUnitPrice() + " (تغطية " + l.getCoveragePct() + "%)"});
    rows.add(new String[]{"الإجمالي", String.valueOf(inv.getTotalAmount())});
    rows.add(new String[]{"تحمل جهة التأمين", String.valueOf(inv.getInsurerAmount())});
    rows.add(new String[]{"تحمل المريض", String.valueOf(inv.getPatientAmount())});
    return pdf("invoice-" + id, pdf.render("فاتورة خدمات طبية", id.toString(), rows, List.of()));
  }
  @GetMapping("/discharge-summary/{id}")
  ResponseEntity<byte[]> discharge(@PathVariable UUID id) {
    principal();
    var s = summaries.findById(id).orElseThrow(() -> BizException.notFound("summary_not_found","الملخص غير موجود"));
    return pdf("discharge-" + id, pdf.render("ملخص خروج", id.toString(), List.of(
      new String[]{"التشخيصات", s.getDiagnoses()},
      new String[]{"الإجراءات", s.getProcedures()},
      new String[]{"تعليمات الخروج", s.getInstructions()},
      new String[]{"تحذيرات", s.getWarnings()},
      new String[]{"خطة الأدوية", s.getMedicationsPlan()},
      new String[]{"خطة المتابعة", s.getFollowupPlan()}),
      List.of("راجع المنشأة فورًا عند ظهور أي من التحذيرات أعلاه.")));
  }
}
