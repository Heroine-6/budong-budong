package com.example.budongbudong.domain.auth.service;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.common.utils.SmsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsUtil smsUtil;
    private final DefaultMessageService messageService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${coolsms.from-number}")
    private String fromNumber;

    public void sendAuthCode(String toNumber) {

        String authCode = SmsUtil.generateAuthCode();
        String messageText = smsUtil.makeAuthMessage(authCode);

        String redisKey = "SMS:AUTH:" + toNumber;
        redisTemplate.opsForValue().set(redisKey, authCode, 3, TimeUnit.MINUTES);

        Message message = new Message();
        message.setFrom(fromNumber);
        message.setTo(toNumber);
        message.setText(messageText);

        SingleMessageSendingRequest request = new SingleMessageSendingRequest(message);

        try {
            SingleMessageSentResponse response = messageService.sendOne(request);
            log.info("[SMS] 인증번호 발송 - to: {}", toNumber);
        } catch (Exception e) {
            redisTemplate.delete(redisKey);
            log.error("[SMS] 인증번호 발송 실패 - to: {}", toNumber);
            throw new CustomException(ErrorCode.SMS_SEND_FAILED);
        }
    }
}
