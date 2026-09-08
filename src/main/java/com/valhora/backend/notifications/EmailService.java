package com.valhora.backend.notifications;

import com.valhora.backend.orders.Order;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String INK = "#0d0d0f";
    private static final String IVORY = "#f7f3ea";
    private static final String GOLD = "#9a9a9a";
    private static final String LOGO_CID = "valhoraLogo";

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String adminEmail;
    private final String frontendBaseUrl;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromAddress,
            @Value("${app.notifications.admin-email}") String adminEmail,
            @Value("${app.frontend-base-url}") String frontendBaseUrl) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.adminEmail = adminEmail;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public void sendNewOrderToAdmin(Order order) {
        String subject = "Nuevo pedido Valhora #VH-" + order.getOrderNumber();
        String body = wrap(
                "Nuevo pedido",
                "Pedido #VH-" + order.getOrderNumber(),
                infoRow("Cliente", order.getCustomerName())
                        + infoRow("Teléfono", order.getCustomerPhone())
                        + infoRow("Correo", order.getCustomerEmail())
                        + infoRow("Ubicación", order.getDistrict() + ", " + order.getCanton() + ", " + order.getProvince())
                        + spacer()
                        + itemsTable(order)
                        + totalsTable(order)
                        + spacer()
                        + infoRow("Entrega", describeDelivery(order))
                        + infoRow("Pago", describePayment(order))
                        + infoRow("Estado", "Pendiente de pago")
                        + spacer()
                        + button("Ver pedido en el panel", frontendBaseUrl + "/admin/pedidos/" + order.getId()));
        send(adminEmail, subject, body);
    }

    public void sendOrderConfirmationToCustomer(Order order) {
        String subject = "Pedido #VH-" + order.getOrderNumber() + " confirmado - Valhora";
        String body = wrap(
                "¡Gracias por tu compra!",
                "Pedido #VH-" + order.getOrderNumber(),
                "<p style=\"margin:0 0 20px;font-size:14px;color:#4a4a4a;\">Recibimos tu pedido y lo estamos preparando.</p>"
                        + itemsTable(order)
                        + totalsTable(order)
                        + spacer()
                        + infoRow("Método de pago", describePayment(order))
                        + infoRow("Entrega", describeDelivery(order))
                        + spacer()
                        + "<p style=\"margin:20px 0 0;font-size:13px;color:#6b6b6b;\">Te contactaremos para confirmar tu pago y coordinar la entrega.</p>");
        send(order.getCustomerEmail(), subject, body);
    }

    public void sendProofReceivedToAdmin(Order order) {
        String subject = "Comprobante recibido - Pedido #VH-" + order.getOrderNumber();
        String body = wrap(
                "Comprobante recibido",
                "Pedido #VH-" + order.getOrderNumber(),
                "<p style=\"margin:0 0 20px;font-size:14px;color:#4a4a4a;\">"
                        + "El cliente " + order.getCustomerName()
                        + " subió un comprobante de pago. Revísalo para confirmar la compra.</p>"
                        + button("Ver comprobante", order.getPaymentProofUrl())
                        + spacer()
                        + button("Ver pedido en el panel", frontendBaseUrl + "/admin/pedidos/" + order.getId()));
        send(adminEmail, subject, body);
    }

    public void sendPaymentConfirmedToCustomer(Order order) {
        String subject = "Pago confirmado - Pedido #VH-" + order.getOrderNumber() + " - Valhora";
        String body = wrap(
                "¡Tu pago fue confirmado!",
                "Pedido #VH-" + order.getOrderNumber(),
                "<p style=\"margin:0;font-size:14px;color:#4a4a4a;\">"
                        + "Tu pedido ahora está en preparación. Te avisaremos cuando sea enviado.</p>");
        send(order.getCustomerEmail(), subject, body);
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, "Valhora");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(stripHtml(htmlBody), htmlBody);
            helper.addInline(LOGO_CID, new ClassPathResource("email/logo.png"));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo a {}: {}", to, e.getMessage());
        }
    }

    private String wrap(String heading, String subheading, String contentHtml) {
        return "<!DOCTYPE html><html><body style=\"margin:0;padding:32px 16px;background:" + IVORY
                + ";font-family:Arial,Helvetica,sans-serif;\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">"
                + "<tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"480\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:480px;width:100%;background:#ffffff;border:1px solid #e5e0d5;\">"
                + "<tr><td style=\"padding:32px 32px 20px;text-align:center;border-bottom:1px solid #eee;\">"
                + "<img src=\"cid:" + LOGO_CID + "\" alt=\"Valhora\" width=\"56\" style=\"display:block;margin:0 auto 16px;\" />"
                + "<p style=\"margin:0;font-size:11px;letter-spacing:2px;text-transform:uppercase;color:" + GOLD + ";\">" + heading + "</p>"
                + "<h1 style=\"margin:6px 0 0;font-size:20px;color:" + INK + ";font-weight:600;\">" + subheading + "</h1>"
                + "</td></tr>"
                + "<tr><td style=\"padding:28px 32px 32px;\">" + contentHtml + "</td></tr>"
                + "<tr><td style=\"padding:20px 32px;background:" + IVORY + ";text-align:center;\">"
                + "<p style=\"margin:0;font-size:11px;color:#9a9a9a;letter-spacing:1px;text-transform:uppercase;\">Valhora Collection</p>"
                + "</td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    private String infoRow(String label, String value) {
        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom:6px;\">"
                + "<tr>"
                + "<td style=\"font-size:11px;text-transform:uppercase;letter-spacing:0.5px;color:#9a9a9a;padding-right:12px;white-space:nowrap;\">" + label + "</td>"
                + "<td style=\"font-size:14px;color:" + INK + ";text-align:right;\">" + escape(value) + "</td>"
                + "</tr></table>";
    }

    private String spacer() {
        return "<div style=\"height:20px;\"></div>";
    }

    private String button(String label, String url) {
        return "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\">"
                + "<tr><td style=\"background:" + INK + ";\">"
                + "<a href=\"" + url + "\" style=\"display:inline-block;padding:12px 24px;font-size:12px;letter-spacing:1px;text-transform:uppercase;color:#ffffff;text-decoration:none;\">" + label + "</a>"
                + "</td></tr></table>";
    }

    private String itemsTable(Order order) {
        String rows = order.getItems().stream()
                .map(item -> "<tr>"
                        + "<td style=\"padding:10px 0;border-bottom:1px solid #f0f0f0;font-size:13px;color:" + INK + ";\">"
                        + escape(item.getProductName()) + "<br/><span style=\"color:#9a9a9a;font-size:11px;\">"
                        + escape(item.getSku()) + " · " + item.getQuantity() + " x " + formatCurrency(item.getUnitPrice()) + "</span></td>"
                        + "<td style=\"padding:10px 0;border-bottom:1px solid #f0f0f0;font-size:13px;color:" + INK + ";text-align:right;white-space:nowrap;\">"
                        + formatCurrency(item.getLineSubtotal()) + "</td>"
                        + "</tr>")
                .collect(Collectors.joining());
        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom:12px;\">" + rows + "</table>";
    }

    private String totalsTable(Order order) {
        StringBuilder rows = new StringBuilder();
        rows.append(totalRow("Subtotal", formatCurrency(order.getItemsSubtotal()), false));
        if (order.getShippingCost() != null) {
            rows.append(totalRow("Envío", formatCurrency(order.getShippingCost()), false));
        }
        rows.append(totalRow("Total", formatCurrency(order.getTotal()), true));
        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">" + rows + "</table>";
    }

    private String totalRow(String label, String value, boolean emphasis) {
        String size = emphasis ? "15px" : "13px";
        String weight = emphasis ? "600" : "400";
        String color = emphasis ? INK : "#6b6b6b";
        String borderTop = emphasis ? "border-top:1px solid #eee;padding-top:8px;" : "";
        return "<tr>"
                + "<td style=\"" + borderTop + "padding:4px 0;font-size:" + size + ";font-weight:" + weight + ";color:" + color + ";\">" + label + "</td>"
                + "<td style=\"" + borderTop + "padding:4px 0;font-size:" + size + ";font-weight:" + weight + ";color:" + color + ";text-align:right;\">" + value + "</td>"
                + "</tr>";
    }

    private String stripHtml(String html) {
        return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private String escape(String value) {
        return value == null
                ? ""
                : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String formatCurrency(BigDecimal amount) {
        return "₡" + amount.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    private String describeDelivery(Order order) {
        return switch (order.getDeliveryMethod()) {
            case CORREOS_CR -> "Correos de Costa Rica";
            case MENSAJERIA_PRIVADA -> "Mensajería privada";
            case PICKUP -> "Retiro en tienda";
        };
    }

    private String describePayment(Order order) {
        return switch (order.getPaymentMethod()) {
            case SINPE -> "SINPE Móvil";
            case BANK_TRANSFER -> "Transferencia bancaria";
            case CASH_ON_DELIVERY -> "Pago contraentrega";
        };
    }
}
