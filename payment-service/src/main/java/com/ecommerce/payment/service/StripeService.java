package com.ecommerce.payment.service;

import com.ecommerce.sdk.request.ConfirmPaymentDTO;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Token;
import com.stripe.param.ChargeCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class StripeService {

    public static final String NUMBER = "number";
    public static final String EXP_MONTH = "exp_month";
    public static final String EXP_YEAR = "exp_year";
    public static final String CVC = "cvc";
    public static final String CARD = "card";
    public static final String USD = "usd";
    public static final String STATUS_UUID = "status_uuid";

    @Value("${stripe.apiKey}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = this.stripeApiKey;
    }

    public String proceedWithPayment(ConfirmPaymentDTO confirmPaymentDTO, UUID statusUuid) throws StripeException {
        Map<String, Object> card = new HashMap<>();
        card.put(NUMBER, confirmPaymentDTO.getCardNumber());
        card.put(EXP_MONTH, confirmPaymentDTO.getExpMonth());
        card.put(EXP_YEAR, confirmPaymentDTO.getExpYear());
        card.put(CVC, Integer.toString(confirmPaymentDTO.getCvc()));
        Map<String, Object> params = new HashMap<>();
        params.put(CARD, card);

        Token token = Token.create(params);

        ChargeCreateParams params2 =
                ChargeCreateParams.builder()
                        .setAmount(Math.round(confirmPaymentDTO.getAmount() * 100))
                        .setCurrency(USD)
                        // .setDescription("Example charge")
                        .setSource(token.getId())
                        .putMetadata(STATUS_UUID, statusUuid.toString())
                        .build();

        Charge charge = Charge.create(params2);
        return charge.getId();
    }
}
