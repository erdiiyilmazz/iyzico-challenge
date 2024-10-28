package com.iyzico.challenge.service;

import com.iyzico.challenge.entity.Payment;
import com.iyzico.challenge.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;

@Service
// Removed class layer @Transactional annotation
public class IyzicoPaymentService {

    private Logger logger = LoggerFactory.getLogger(IyzicoPaymentService.class);

    private BankService bankService;
    private PaymentRepository paymentRepository;

    public IyzicoPaymentService(BankService bankService, PaymentRepository paymentRepository) {
        this.bankService = bankService;
        this.paymentRepository = paymentRepository;
    }

    public void pay(BigDecimal price) {
        BankPaymentResponse response = bankPayment(price);
        if (response != null) {
            savePayment(response.getResultCode(), price);
            logger.info("Payment saved successfully!");
        }
    }

    private BankPaymentResponse bankPayment(BigDecimal price) {
        BankPaymentRequest request = new BankPaymentRequest();
        request.setPrice(price);
        return bankService.pay(request);
    }

    // Here we use method level @Transactional annotation
    // This is to make the payment is saved even if the bank payment fails
    // only two concurrent save operations are allowed with existing pool size = 2
    // other threads are waiting for the first two threads to finish, it was causing timeout
    // with Propagation.REQUIRES_NEW, each thread will get its own transaction
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savePayment(String bankResultCode, BigDecimal price) {
        Payment payment = new Payment();
        payment.setBankResponse(bankResultCode);
        payment.setPrice(price);
        paymentRepository.save(payment);
    }
}
