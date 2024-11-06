package com.example.payments.service;

import com.example.payments.client.PaymentClient;
import com.example.payments.model.Payment;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private PublishNotification publishNotification;
    private PaymentClient paymentClient;

    public PaymentService(PublishNotification publishNotification, PaymentClient paymentClient) {
        this.publishNotification = publishNotification;
        this.paymentClient = paymentClient;
    }

    public void cratePayment(Payment payment){
        //crear un payment
        paymentClient.createPayment(payment);
        publishNotification.sendMessageToTopic(payment.getClientId());
    }
}
