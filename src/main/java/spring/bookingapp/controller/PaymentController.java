package spring.bookingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring.bookingapp.dto.PaymentDto;
import spring.bookingapp.dto.PaymentRequestDto;
import spring.bookingapp.service.PaymentService;

@Tag(name = "Payment management",
        description = "Endpoints for managing payments via Stripe")
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @Operation(summary = "Create payment session",
            description = "Generates a link for a Stripe payment session based on booking ID.")
    public PaymentDto createPaymentSession(@RequestBody @Valid PaymentRequestDto requestDto) {
        return paymentService.createPaymentSession(requestDto);
    }

    @GetMapping("/success")
    @Operation(summary = "Handle successful payment",
            description = "Callback endpoint for successful Stripe payment.")
    public PaymentDto checkSuccessfulPayment(@RequestParam String sessionId) {
        return paymentService.handleSuccessfulPayment(sessionId);
    }

    @GetMapping("/cancel")
    @Operation(summary = "Handle canceled payment",
            description = "Callback endpoint for canceled Stripe payment.")
    public PaymentDto checkCanceledPayment(@RequestParam String sessionId) {
        return paymentService.handleCanceledPayment(sessionId);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @Operation(summary = "Get all payments",
            description = "Get a page of payments. "
                    + "Managers can see all or filter by user ID. Customers see only their own.")
    public Page<PaymentDto> findAll(@RequestParam(required = false) Long userId,
                                    Pageable pageable) {
        return paymentService.findAll(userId, pageable);
    }
}
