package phitrading.phitradingexchangemain.web.controller;

import com.phitrading.exchange.domain.service.OrderViewService;
import com.phitrading.exchange.web.dto.OrderRowView;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class OrderExportController {

    private static final Logger log = LoggerFactory.getLogger(OrderExportController.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final OrderViewService orderViewService;

    public OrderExportController(OrderViewService orderViewService) {
        this.orderViewService = orderViewService;
    }

    @GetMapping("/orders/export/csv")
    public void exportCsv(Principal principal, HttpServletResponse response) throws IOException {
        String username = principal != null ? principal.getName() : "anonymous";
        List<OrderRowView> orders = orderViewService.getUserOrders(username);

        String safeFilename = "orders-" + (username != null ? URLEncoder.encode(username, StandardCharsets.UTF_8) : "anonymous") + ".csv";
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeFilename + "\"");

        try (PrintWriter writer = response.getWriter()) {
            // Write BOM via writer for Excel compatibility
            writer.write('\uFEFF');

            writer.println("Date,Symbol,Side,Quantity,Price,Status");
            for (OrderRowView o : orders) {
                String date = formatDate(o.getExecutedAt() != null ? o.getExecutedAt() : o.getCreatedAt());
                writer.println(String.join(",",
                        csv(date),
                        csv(o.getSymbol()),
                        csv(o.getSide()),
                        csv(o.getQuantity() != null ? o.getQuantity().toPlainString() : ""),
                        csv(o.getExecutionPrice() != null ? o.getExecutionPrice().toPlainString() : ""),
                        csv(o.getStatus())
                ));
            }
            writer.flush();
        }
        log.info("Exported {} orders to CSV for user={}", orders.size(), username);
    }

    @GetMapping("/orders/export/pdf")
    public void exportPdf(Principal principal, HttpServletResponse response) throws Exception {
        String username = principal != null ? principal.getName() : "anonymous";
        List<OrderRowView> orders = orderViewService.getUserOrders(username);

        String filename = "orders-" + username + ".pdf";
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        try (OutputStream os = response.getOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(document, os);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Paragraph title = new Paragraph("Order History for " + username, titleFont);
            title.setAlignment(Element.ALIGN_LEFT);
            title.setSpacingAfter(12f);
            document.add(title);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{18f, 16f, 12f, 16f, 16f, 12f});

            addHeaderCell(table, "Date");
            addHeaderCell(table, "Symbol");
            addHeaderCell(table, "Side");
            addHeaderCell(table, "Quantity");
            addHeaderCell(table, "Price");
            addHeaderCell(table, "Status");

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            for (OrderRowView o : orders) {
                String date = formatDate(o.getExecutedAt() != null ? o.getExecutedAt() : o.getCreatedAt());
                table.addCell(new Phrase(nullSafe(date), cellFont));
                table.addCell(new Phrase(nullSafe(o.getSymbol()), cellFont));
                table.addCell(new Phrase(nullSafe(o.getSide()), cellFont));
                table.addCell(new Phrase(o.getQuantity() != null ? o.getQuantity().toPlainString() : "", cellFont));
                table.addCell(new Phrase(o.getExecutionPrice() != null ? o.getExecutionPrice().toPlainString() : "", cellFont));
                table.addCell(new Phrase(nullSafe(o.getStatus()), cellFont));
            }

            document.add(table);
            document.close();
        }

        log.info("Exported {} orders to PDF for user={}", orders.size(), username);
    }

    private static void addHeaderCell(PdfPTable table, String text) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        table.addCell(cell);
    }

    private static String formatDate(LocalDateTime dt) {
        return dt != null ? DATE_FMT.format(dt) : "";
    }

    private static String csv(String v) {
        if (v == null) return "";
        String escaped = v.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private static String nullSafe(String v) { return v == null ? "" : v; }
}
