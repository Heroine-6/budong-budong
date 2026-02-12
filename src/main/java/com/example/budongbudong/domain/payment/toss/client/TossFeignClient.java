package com.example.budongbudong.domain.payment.toss.client;

import com.example.budongbudong.domain.payment.toss.config.TossFeignConfig;
import com.example.budongbudong.domain.payment.toss.dto.response.TossConfirmResponse;
import com.example.budongbudong.domain.payment.toss.dto.response.TossPaymentStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name = "tossFeignClient",
        url = "https://api.tosspayments.com/v1/payments",
        configuration = TossFeignConfig.class
)
public interface TossFeignClient {

    @PostMapping("/confirm")
    TossConfirmResponse confirm(@RequestBody Map<String, Object> body);

    @PostMapping("/{paymentKey}/cancel")
    void refund(@PathVariable("paymentKey") String paymentKey, @RequestBody Map<String, Object> body);

    @GetMapping("/{paymentKey}")
    TossPaymentStatusResponse getPayment(@PathVariable("paymentKey") String paymentKey);
}
