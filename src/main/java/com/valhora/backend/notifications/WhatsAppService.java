package com.valhora.backend.notifications;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valhora.backend.orders.Order;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Notificaciones al admin por WhatsApp Business Cloud API (Meta). No hace nada si
 * WHATSAPP_PHONE_NUMBER_ID o WHATSAPP_ACCESS_TOKEN no están configurados, para que el
 * arranque de la app y la creación de pedidos nunca dependan de esta integración.
 */
@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);
    private static final String GRAPH_API_VERSION = "v21.0";

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String phoneNumberId;
    private final String accessToken;
    private final String adminNumber;
    private final String templateName;
    private final String languageCode;

    public WhatsAppService(
            @Value("${app.whatsapp.phone-number-id}") String phoneNumberId,
            @Value("${app.whatsapp.access-token}") String accessToken,
            @Value("${app.whatsapp.admin-number}") String adminNumber,
            @Value("${app.whatsapp.template-name}") String templateName,
            @Value("${app.whatsapp.language-code}") String languageCode) {
        this.phoneNumberId = phoneNumberId;
        this.accessToken = accessToken;
        this.adminNumber = adminNumber;
        this.templateName = templateName;
        this.languageCode = languageCode;
    }

    public void notifyAdminNewOrder(Order order) {
        notifyAdmin(order, "Nuevo pedido, pendiente de pago");
    }

    public void notifyAdminProofUploaded(Order order) {
        notifyAdmin(order, "Comprobante recibido, revisar");
    }

    private void notifyAdmin(Order order, String statusText) {
        if (phoneNumberId.isBlank() || accessToken.isBlank()) {
            return;
        }

        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("messaging_product", "whatsapp");
            payload.put("to", adminNumber);
            payload.put("type", "template");

            ObjectNode template = payload.putObject("template");
            template.put("name", templateName);
            template.putObject("language").put("code", languageCode);

            ArrayNode parameters = objectMapper.createArrayNode();
            parameters.add(textParam("VH-" + order.getOrderNumber()));
            parameters.add(textParam(order.getCustomerName()));
            parameters.add(textParam(formatCurrency(order.getTotal())));
            parameters.add(textParam(statusText));

            ObjectNode bodyComponent = objectMapper.createObjectNode();
            bodyComponent.put("type", "body");
            bodyComponent.set("parameters", parameters);
            template.putArray("components").add(bodyComponent);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://graph.facebook.com/" + GRAPH_API_VERSION + "/" + phoneNumberId + "/messages"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.error("WhatsApp Cloud API respondió {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("No se pudo enviar la notificación de WhatsApp: {}", e.getMessage());
        }
    }

    private ObjectNode textParam(String value) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", "text");
        node.put("text", value);
        return node;
    }

    private String formatCurrency(BigDecimal amount) {
        return "₡" + amount.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }
}
