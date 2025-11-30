package com.lucas_cm.bank_test.infrastructure.controllers;

import com.lucas_cm.bank_test.domain.services.PixService;
import com.lucas_cm.bank_test.infrastructure.dtos.PixTransferRequest;
import com.lucas_cm.bank_test.infrastructure.dtos.PixTransferResponse;
import com.lucas_cm.bank_test.infrastructure.dtos.PixWebhookRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/pix")
@Tag(name = "Pix")
public class PixController {

    private PixService pixService;

    @PostMapping("/transfers")
    public ResponseEntity<PixTransferResponse> transferPix(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody PixTransferRequest request) {

        PixTransferResponse response = pixService.transfer(
                idempotencyKey,
                request
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhook(@RequestBody PixWebhookRequest request) {

        pixService.processWebhook(request);

        // Sempre 200 OK, como webhooks reais
        return ResponseEntity.ok().build();
    }
}
