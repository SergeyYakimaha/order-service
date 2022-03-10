package com.example.orderservice.service;

import com.example.orderservice.common.Payment;
import com.example.orderservice.common.TransactionRequest;
import com.example.orderservice.common.TransactionResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final RestTemplate template;

    @Autowired
    public OrderService(OrderRepository repository, RestTemplate template) {
        this.repository = repository;
        this.template = template;
    }

    public TransactionResponse saveOrder(TransactionRequest request) {
        Order order = request.getOrder();
        Payment payment = request.getPayment();
        payment.setOrderId(order.getId());
        payment.setAmount(order.getPrice());

        Payment paymentResponse = template.postForObject("http://localhost:9191/payment/doPayment", payment, Payment.class);
        String response = paymentResponse.getPaymentStatus().equals("success")
                ?   "payment processing successful and order placed"
                :   "there is a failure in payment api";

        repository.save(order);
        return  TransactionResponse.builder()
                .order(order)
                .amount(paymentResponse.getAmount())
                .transactionId(paymentResponse.getTransactionId())
                .message(response)
                .build();
    }
}
