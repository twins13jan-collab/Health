package eg.gov.health.platform.printdocs;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.*; import java.util.List;
/** Official printable documents (completion batch 3). Renders
 *  REAL stored records — prescriptions, lab reports, invoices,
 *  discharge summaries — as Arabic RTL PDFs with the bundled
 *  Noto Naskh Arabic font, a facility header, the document's
 *  own data, and a footer carrying the document id: what
 *  leaves the system on paper is the same record the system
 *  holds, not a retyped copy. */
@Service
public class PdfDocumentService {
  private BaseFont font;
  private Font titleFont, headFont, bodyFont, smallFont;
  @jakarta.annotation.PostConstruct
  void init() throws Exception {
    // Bundled font first; on hosts without it (e.g. a snapshot of the
    // sources that cannot carry the binary font), fall back to a system
    // Noto Naskh Arabic before giving up.
    byte[] bytes = null;
    try { bytes = new ClassPathResource("fonts/arabic.ttf").getContentAsByteArray(); } catch (Exception ignored) {}
    if (bytes != null) {
      font = BaseFont.createFont("arabic.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);
    } else {
      String[] candidates = {
        "/usr/share/fonts/truetype/noto/NotoNaskhArabic-Regular.ttf",
        "/usr/share/fonts/truetype/noto/NotoKufiArabic-Regular.ttf" };
      String found = null;
      for (String c : candidates) if (new java.io.File(c).exists()) { found = c; break; }
      if (found == null) throw new IllegalStateException("no Arabic font available (bundled or system)");
      font = BaseFont.createFont(found, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    }
    titleFont = new Font(font, 18, Font.BOLD);
    headFont = new Font(font, 12, Font.BOLD);
    bodyFont = new Font(font, 11, Font.NORMAL);
    smallFont = new Font(font, 8, Font.NORMAL);
  }
  public byte[] render(String title, String docId, List<String[]> rows, List<String> notes) {
    try {
      var out = new ByteArrayOutputStream();
      var doc = new Document(PageSize.A4, 48, 48, 54, 54);
      var writer = PdfWriter.getInstance(doc, out);
      doc.open();
      var header = new PdfPTable(1); header.setRunDirection(PdfWriter.RUN_DIRECTION_RTL); header.setWidthPercentage(100);
      header.addCell(cell("المنظومة الوطنية لإدارة الأزمات الصحية والطوارئ الطبية", titleFont, Element.ALIGN_CENTER));
      header.addCell(cell("مستند رسمي صادر من المنظومة — " + title, headFont, Element.ALIGN_CENTER));
      doc.add(header);
      doc.add(new Paragraph(" ", bodyFont));
      var table = new PdfPTable(2); table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL); table.setWidthPercentage(100);
      table.setWidths(new float[]{1f, 2.2f});
      for (String[] r : rows) {
        table.addCell(cell(r[0], headFont, Element.ALIGN_RIGHT));
        table.addCell(cell(r.length > 1 && r[1] != null ? r[1] : "—", bodyFont, Element.ALIGN_RIGHT));
      }
      doc.add(table);
      for (String n : notes) { doc.add(new Paragraph(" ", bodyFont)); doc.add(new Paragraph(new Phrase(n, bodyFont))); }
      doc.add(new Paragraph(" ", bodyFont));
      var footer = new PdfPTable(1); footer.setRunDirection(PdfWriter.RUN_DIRECTION_RTL); footer.setWidthPercentage(100);
      footer.addCell(cell("معرف المستند: " + docId + " — يُتحقق من صحة هذا المستند بمطابقة المعرف مع سجل المنظومة", smallFont, Element.ALIGN_CENTER));
      doc.add(footer);
      doc.close();
      return out.toByteArray();
    } catch (Exception e) {
      throw new IllegalStateException("pdf_render_failed: " + e.getMessage(), e);
    }
  }
  private PdfPCell cell(String text, Font f, int align) {
    var c = new PdfPCell(new Phrase(text, f));
    c.setHorizontalAlignment(align);
    c.setPadding(6f);
    c.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
    return c;
  }
}
