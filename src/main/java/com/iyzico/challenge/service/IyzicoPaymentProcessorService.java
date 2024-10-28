package com.iyzico.challenge.service;

import com.iyzico.challenge.entity.Flight;
import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreatePaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iyzico.challenge.dto.IyzicoPaymentRequest;
import com.iyzico.challenge.exception.PaymentProcessingException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class IyzicoPaymentProcessorService {
    private static final Logger logger = LoggerFactory.getLogger(IyzicoPaymentProcessorService.class);
    
    private final Options options;

    private final FlightService flightService;

    private final SeatService seatService;

    public IyzicoPaymentProcessorService(Options options, FlightService flightService, SeatService seatService) {
        this.options = options;
        this.flightService = flightService;
        this.seatService = seatService;
    }

    public String payWithIyzico(IyzicoPaymentRequest paymentRequest) {
        try {
            Flight flight = flightService.getFlight(paymentRequest.getFlightId());
            if (flight == null) {
                throw new IllegalArgumentException("Flight not found");
            }
            
            if (!seatService.isSeatAvailable(paymentRequest.getFlightId(), paymentRequest.getSeatNumber())) {
                throw new IllegalArgumentException("Seat is not available");
            }

            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setLocale(Locale.TR.getValue());
            request.setConversationId(UUID.randomUUID().toString());
            request.setPrice(paymentRequest.getPrice());
            request.setPaidPrice(paymentRequest.getPrice());
            request.setCurrency(Currency.TRY.name());
            request.setInstallment(1);
            request.setBasketId("B" + System.currentTimeMillis());
            request.setPaymentChannel(PaymentChannel.WEB.name());
            request.setPaymentGroup(PaymentGroup.PRODUCT.name());

            PaymentCard paymentCard = new PaymentCard();
            paymentCard.setCardHolderName(paymentRequest.getCardHolderName());
            paymentCard.setCardNumber(paymentRequest.getCardNumber());
            paymentCard.setExpireMonth(paymentRequest.getExpireMonth());
            paymentCard.setExpireYear(paymentRequest.getExpireYear());
            paymentCard.setCvc(paymentRequest.getCvc());
            paymentCard.setRegisterCard(0);
            request.setPaymentCard(paymentCard);

            Buyer buyer = new Buyer();
            buyer.setId("BY" + System.currentTimeMillis());
            buyer.setName("John");
            buyer.setSurname("Doe");
            buyer.setGsmNumber("+905350000000");
            buyer.setEmail("email@email.com");
            buyer.setIdentityNumber("74300864791");
            buyer.setRegistrationAddress("Test Address");
            buyer.setIp("85.34.78.112");
            buyer.setCity("Istanbul");
            buyer.setCountry("Turkey");
            request.setBuyer(buyer);

            Address address = new Address();
            address.setContactName("Jane Doe");
            address.setCity("Istanbul");
            address.setCountry("Turkey");
            address.setAddress("Test Address");
            request.setShippingAddress(address);
            request.setBillingAddress(address);

            List<BasketItem> basketItems = new ArrayList<>();
            BasketItem item = new BasketItem();
            item.setId("FT" + System.currentTimeMillis());
            item.setName("Flight " + flight.getFlightNumber() + " - Seat " + paymentRequest.getSeatNumber());
            item.setCategory1("Flight");
            item.setItemType(BasketItemType.VIRTUAL.name());
            item.setPrice(paymentRequest.getPrice());
            basketItems.add(item);
            request.setBasketItems(basketItems);

            Payment response = Payment.create(request, options);
            
            if (response != null && "SUCCESS".equals(response.getStatus())) {
                seatService.reserveSeat(paymentRequest.getFlightId(), paymentRequest.getSeatNumber());
                logger.info("Payment processed and seat reserved successfully. Status: {}", response.getStatus());
                return response.getStatus();
            }
            
            return "Payment failed";
        } catch (Exception e) {
            logger.error("Payment processing failed: ", e);
            throw new PaymentProcessingException("Payment failed", e);
        }
    }
}
