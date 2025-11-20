package com.aiqueen.controller;

import com.aiqueen.service.SchedulerService;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AIController {

    private final SchedulerService schedulerService;

    @Value("${stripe.key:}")
    private String stripeKey;

    @Value("${twilio.sid:}")
    private String twilioSid;

    @Value("${twilio.token:}")
    private String twilioToken;

    @Value("${twilio.from:+13233640769}")
    private String twilioFrom;

    public AIController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status","ok","time", System.currentTimeMillis()));
    }

    @PostMapping("/schedule")
    public ResponseEntity<?> scheduleCampaign(@RequestBody Map<String,Object> body) {
        // schedule a campaign via SchedulerService
        String id = schedulerService.scheduleCampaign(body);
        return ResponseEntity.ok(Map.of("ok", true, "scheduledId", id));
    }

    @PostMapping("/donate")
    public ResponseEntity<?> createDonation(@RequestBody Map<String,Object> body) throws Exception {
        if (stripeKey == null || stripeKey.isBlank()) {
            return ResponseEntity.status(500).body(Map.of("ok",false,"error","Stripe key not configured"));
        }
        Stripe.apiKey = stripeKey;
        long amount = Math.round(Double.parseDouble(String.valueOf(body.getOrDefault("amount","0"))) * 100);
        SessionCreateParams.LineItem.PriceData.ProductData p = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName((String)body.getOrDefault("description","Donation to AI Queen")).build();
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency((String)body.getOrDefault("currency","usd"))
                                .setUnitAmount(amount)
                                .setProductData(p)
                                .build())
                        .build())
                .setSuccessUrl((String)body.getOrDefault("success_url","https://example.com/success"))
                .setCancelUrl((String)body.getOrDefault("cancel_url","https://example.com/cancel"))
                .build();
        Session session = Session.create(params);
        return ResponseEntity.ok(Map.of("ok",true,"url",session.getUrl()));
    }

    @PostMapping("/call")
    public ResponseEntity<?> makeCall(@RequestBody Map<String,String> body) {
        if (twilioSid == null || twilioSid.isBlank() || twilioToken == null) {
            return ResponseEntity.status(500).body(Map.of("ok",false,"error","Twilio not configured"));
        }
        Twilio.init(twilioSid, twilioToken);
        String to = body.get("to");
        // replace twimlUrl with the /twiml endpoint of your server
        String twimlUrl = (String) body.getOrDefault("twimlUrl","https://your-server.example/twiml");
        Call call = Call.creator(new com.twilio.type.PhoneNumber(to), new com.twilio.type.PhoneNumber(twilioFrom),
                new com.twilio.type.Uri(twimlUrl))
                .create();
        return ResponseEntity.ok(Map.of("ok",true,"sid",call.getSid()));
    }

}
