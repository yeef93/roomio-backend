package com.finpro.roomio_backend.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SignatureVerifier {
    @Value("${midtrans.server.key}")
    private  String serverKey;

    public String generateSignature(String orderId, String statusCode, String grossAmount) {
        String rawSignature = orderId + statusCode + grossAmount + serverKey;
        return DigestUtils.sha512Hex(rawSignature);
    }
}
