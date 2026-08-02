package com.ecommerce.service;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * InvoicePdfService — generates PDF invoices using iText 7.
 */
@Service
@Slf4j
public class InvoicePdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    /**
     * Generates a styled PDF invoice for the given Order.
     *
     * @param order the order entity
     * @return byte array containing the PDF binary data
     */
    public byte[] generateInvoicePdf(Order order) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Title Header
            Paragraph title = new Paragraph("INVOICE")
                    .setFontSize(24)
                    .setBold()
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.LEFT);
            document.add(title);

            Paragraph companyName = new Paragraph("E-Commerce Store Inc.\nSupport: support@ecommerce.com")
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY);
            document.add(companyName);

            document.add(new Paragraph("\n"));

            // Meta Info Table (Order # & Date)
            Table metaTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .useAllAvailableWidth();

            metaTable.addCell(createCleanCell("Order Number: " + order.getOrderNumber() + "\nDate: " +
                    (order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FORMATTER) : "N/A"), TextAlignment.LEFT));
            metaTable.addCell(createCleanCell("Customer: " + order.getUser().getFullName() + "\nEmail: " +
                    order.getUser().getEmail(), TextAlignment.RIGHT));

            document.add(metaTable);
            document.add(new Paragraph("\n"));

            // Shipping Address Snapshot
            Paragraph addressHeader = new Paragraph("Shipping Address:")
                    .setBold()
                    .setFontSize(11);
            document.add(addressHeader);

            Paragraph addressBody = new Paragraph(order.getShippingAddressSnapshot() != null ? order.getShippingAddressSnapshot() : "N/A")
                    .setFontSize(10)
                    .setFontColor(ColorConstants.DARK_GRAY);
            document.add(addressBody);

            document.add(new Paragraph("\n"));

            // Line Items Table
            Table itemTable = new Table(UnitValue.createPercentArray(new float[]{40, 20, 15, 25}))
                    .useAllAvailableWidth();

            // Header row
            itemTable.addHeaderCell(createHeaderCell("Product"));
            itemTable.addHeaderCell(createHeaderCell("Unit Price"));
            itemTable.addHeaderCell(createHeaderCell("Qty"));
            itemTable.addHeaderCell(createHeaderCell("Total"));

            for (OrderItem item : order.getItems()) {
                itemTable.addCell(new Cell().add(new Paragraph(item.getProductName())).setFontSize(10));
                itemTable.addCell(new Cell().add(new Paragraph("₹" + item.getUnitPrice())).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));
                itemTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity()))).setFontSize(10).setTextAlignment(TextAlignment.CENTER));
                itemTable.addCell(new Cell().add(new Paragraph("₹" + item.getTotalPrice())).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));
            }

            document.add(itemTable);
            document.add(new Paragraph("\n"));

            // Financial Breakdown Summary
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                    .useAllAvailableWidth();

            summaryTable.addCell(createCleanCell("", TextAlignment.LEFT));

            Table totalsInner = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
            totalsInner.addCell(createCleanCell("Subtotal:", TextAlignment.LEFT));
            totalsInner.addCell(createCleanCell("₹" + order.getSubtotal(), TextAlignment.RIGHT));

            totalsInner.addCell(createCleanCell("Tax (GST 18%):", TextAlignment.LEFT));
            totalsInner.addCell(createCleanCell("₹" + order.getTaxAmount(), TextAlignment.RIGHT));

            totalsInner.addCell(createCleanCell("Shipping:", TextAlignment.LEFT));
            totalsInner.addCell(createCleanCell("₹" + order.getShippingAmount(), TextAlignment.RIGHT));

            if (order.getDiscountAmount() != null && order.getDiscountAmount().doubleValue() > 0) {
                totalsInner.addCell(createCleanCell("Discount:", TextAlignment.LEFT));
                totalsInner.addCell(createCleanCell("-₹" + order.getDiscountAmount(), TextAlignment.RIGHT));
            }

            totalsInner.addCell(createCleanCell("Grand Total:", TextAlignment.LEFT));
            totalsInner.addCell(createCleanCell("₹" + order.getGrandTotal(), TextAlignment.RIGHT));

            summaryTable.addCell(new Cell().add(totalsInner));
            document.add(summaryTable);

            // Footer
            document.add(new Paragraph("\n\nThank you for shopping with us!")
                    .setFontSize(11)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY));

            document.close();
            log.info("Successfully generated PDF invoice for Order #: {}", order.getOrderNumber());

        } catch (Exception e) {
            log.error("Error generating PDF invoice for Order #: {}", order.getOrderNumber(), e);
            throw new RuntimeException("Failed to generate PDF invoice", e);
        }

        return outputStream.toByteArray();
    }

    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold().setFontSize(10).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private Cell createCleanCell(String text, TextAlignment alignment) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(10))
                .setBorder(null)
                .setTextAlignment(alignment);
    }
}
