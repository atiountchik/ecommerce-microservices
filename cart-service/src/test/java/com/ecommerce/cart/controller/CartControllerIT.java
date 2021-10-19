package com.ecommerce.cart.controller;

import com.ecommerce.cart.repository.CartItemsRepository;
import com.ecommerce.cart.repository.ItemAvailabilityRepository;
import com.ecommerce.cart.repository.dbo.CartItemDBO;
import com.ecommerce.cart.repository.dbo.ItemAvailabilityRequestDBO;
import com.ecommerce.sdk.domain.CartItemDTO;
import com.ecommerce.sdk.enums.KafkaTopics;
import com.ecommerce.sdk.enums.ProductAvailabilityEnum;
import com.ecommerce.sdk.exceptions.dto.ExceptionReasonBodyDTO;
import com.ecommerce.sdk.response.ProductAvailabilityResponseDTO;
import com.ecommerce.sdk.response.RequestTokenDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Timed;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9093", "port=9093"}, topics = {KafkaTopics.CHECK_ITEM_AVAILABILITY_RESPONSE_TOPIC})
@ActiveProfiles("test")
public class CartControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ItemAvailabilityRepository itemAvailabilityRepository;
    @Autowired
    private CartItemsRepository cartItemsRepository;

    @Autowired
    private KafkaProducer<String, ProductAvailabilityResponseDTO> kafkaProducerKafkaAvailability;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void clearDatabase() {
        if (this.cartItemsRepository.findAll().size() > 0) {
            this.cartItemsRepository.deleteAll();
        }
    }

    @Test
    @WithMockUser(username = "e3137784-496f-44b6-9bdb-4a20243fd1de", roles = {"BUYER_USER"})
    @Timed(millis = 10_000)
    public void should_addItem_and_reactToAvailability_when_buyerInsertsNewItem_and_itIsAvailable() throws Exception {
        insertItemAndReturnItsSku(ProductAvailabilityEnum.AVAILABLE);
    }

    @Test
    @WithMockUser(username = "e3137784-496f-44b6-9bdb-4a20243fd1de", roles = {"BUYER_USER"})
    @Timed(millis = 10_000)
    public void should_notAddItem_and_reactToAvailability_when_buyerInsertsNewItem_and_itIsUnavailable() throws Exception {
        UUID requestId = insertItem(ProductAvailabilityEnum.UNAVAILABLE);

        ItemAvailabilityRequestDBO itemAvailabilityRequestDB;
        do {
            itemAvailabilityRequestDB = this.itemAvailabilityRepository.findById(requestId).orElse(null);
        } while (itemAvailabilityRequestDB != null);

        MvcResult mvcResult = this.mvc.perform(get("/v1/item/" + requestId))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorMessageId").isString()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ExceptionReasonBodyDTO exceptionReasonBodyDTO = deserializeRequestErrorMessage(contentAsString);
        Assertions.assertEquals(exceptionReasonBodyDTO.getErrorMessageId(), "cart.api.checkIfItemAdditionOrUpdateWasSuccessful.unavailable");
    }

    @Test
    @WithMockUser(username = "e3137784-496f-44b6-9bdb-4a20243fd1de", roles = {"BUYER_USER"})
    @Timed(millis = 10_000)
    public void should_notAddItem_and_reactToAvailability_when_buyerInsertsNewItem_and_itsCapacityHasExceeded() throws Exception {
        UUID requestId = insertItem(ProductAvailabilityEnum.EXCEEDS_AVAILABLE_QUANTITY);

        ItemAvailabilityRequestDBO itemAvailabilityRequestDB;
        do {
            itemAvailabilityRequestDB = this.itemAvailabilityRepository.findById(requestId).orElse(null);
        } while (itemAvailabilityRequestDB != null);

        MvcResult mvcResult = this.mvc.perform(get("/v1/item/" + requestId))
                .andExpect(status().isInsufficientStorage())
                .andExpect(jsonPath("$.errorMessageId").isString()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ExceptionReasonBodyDTO exceptionReasonBodyDTO = deserializeRequestErrorMessage(contentAsString);
        Assertions.assertEquals(exceptionReasonBodyDTO.getErrorMessageId(), "cart.api.checkIfItemAdditionOrUpdateWasSuccessful.quantityExceeded");
    }

    @Test
    @WithMockUser(username = "e3137784-496f-44b6-9bdb-4a20243fd1de", roles = {"BUYER_USER"})
    @Timed(millis = 10_000)
    public void should_notAddItem_and_reactToAvailability_when_buyerInsertsNewItem_and_itIsNotFound() throws Exception {
        UUID requestId = insertItem(ProductAvailabilityEnum.NOT_FOUND);

        ItemAvailabilityRequestDBO itemAvailabilityRequestDB;
        do {
            itemAvailabilityRequestDB = this.itemAvailabilityRepository.findById(requestId).orElse(null);
        } while (itemAvailabilityRequestDB != null);

        MvcResult mvcResult = this.mvc.perform(get("/v1/item/" + requestId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessageId").isString()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ExceptionReasonBodyDTO exceptionReasonBodyDTO = deserializeRequestErrorMessage(contentAsString);
        Assertions.assertEquals(exceptionReasonBodyDTO.getErrorMessageId(), "cart.api.checkIfItemAdditionOrUpdateWasSuccessful.invalidItemSKU");
    }

    @Test
    @WithMockUser(username = "e3137784-496f-44b6-9bdb-4a20243fd1de", roles = {"BUYER_USER"})
    @Timed(millis = 10_000)
    public void should_updateItem_and_reactToAvailability_when_buyerInsertsNewItem_and_thenUpdatesIt_and_receivesAvailability() throws Exception {
        UUID sku = insertItemAndReturnItsSku(ProductAvailabilityEnum.AVAILABLE);
        int newQuantity = 5;
        MvcResult mvcResultUpdate = this.mvc.perform(put("/v1/item").content(asJsonString(new CartItemDTO(sku, newQuantity))).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists()).andReturn();
        String contentAsString2 = mvcResultUpdate.getResponse().getContentAsString();
        RequestTokenDTO requestTokenDTOUpdate = deserializeRequestTokenDTO(contentAsString2);
        UUID requestIdUpdate = requestTokenDTOUpdate.getToken();

        this.kafkaProducerKafkaAvailability.send(new ProducerRecord<>(KafkaTopics.CHECK_ITEM_AVAILABILITY_RESPONSE_TOPIC, new ProductAvailabilityResponseDTO(requestIdUpdate, ProductAvailabilityEnum.AVAILABLE)));
        this.kafkaProducerKafkaAvailability.flush();

        String productAvailability = null;
        while (productAvailability == null) {
            productAvailability = this.itemAvailabilityRepository.findById(requestIdUpdate).orElse(null).getProductAvailability();
        }

        MvcResult mvcResultUpdateResult = this.mvc.perform(get("/v1/item/" + requestIdUpdate))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.sku").exists())
                .andExpect(jsonPath("$.quantity").exists()).andReturn();
        String responseAsStringUpdateResult = mvcResultUpdateResult.getResponse().getContentAsString();

        CartItemDTO cartItemDTO = deserializeResponseCartDTO(responseAsStringUpdateResult);
        Assertions.assertEquals(cartItemDTO.getQuantity(), newQuantity);
    }

    @Test
    @WithMockUser(username = "e3137784-496f-44b6-9bdb-4a20243fd1de", roles = {"BUYER_USER"})
    @Timed(millis = 10_000)
    public void should_deleteItem_when_buyerClearsAnItemFromCart() throws Exception {
        UUID sku1 = insertItemAndReturnItsSku(ProductAvailabilityEnum.AVAILABLE);
        UUID sku2 = insertItemAndReturnItsSku(ProductAvailabilityEnum.AVAILABLE);
        this.mvc.perform(delete("/v1/item/" + sku1))
                .andExpect(status().isOk());
        List<CartItemDBO> byBuyerId = this.cartItemsRepository.findByBuyerId(UUID.fromString("e3137784-496f-44b6-9bdb-4a20243fd1de"));
        Assertions.assertEquals(byBuyerId.size(), 1);
        Assertions.assertEquals(byBuyerId.get(0).getSku(), sku2);
    }

    @Test
    @WithMockUser(username = "e3137784-496f-44b6-9bdb-4a20243fd1de", roles = {"BUYER_USER"})
    @Timed(millis = 10_000)
    public void should_deleteAllItems_when_buyerClearsCart() throws Exception {
        insertItemAndReturnItsSku(ProductAvailabilityEnum.AVAILABLE);
        this.mvc.perform(get("/v1/clear"))
                .andExpect(status().isOk()).andReturn();
        List<CartItemDBO> byBuyerId = this.cartItemsRepository.findByBuyerId(UUID.fromString("e3137784-496f-44b6-9bdb-4a20243fd1de"));
        Assertions.assertEquals(byBuyerId.size(), 0);
    }

    @Test
    @WithMockUser(username = "e3137784-496f-44b6-9bdb-4a20243fd1de", roles = {"BUYER_USER"})
    @Timed(millis = 10_000)
    public void should_getRequestId_when_placingAnOrder() throws Exception {
        MvcResult mvcResult = this.mvc.perform(get("/v1/place"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        RequestTokenDTO requestTokenDTO = deserializeRequestTokenDTO(contentAsString);
        Assertions.assertNotNull(requestTokenDTO);
    }

    @Test
    @WithMockUser(username = "e3137784-496f-44b6-9bdb-4a20243fd1de", roles = {"BUYER_USER"})
    @Timed(millis = 10_000)
    public void should_getRequestId_when_gettingCartItems() throws Exception {
        MvcResult mvcResult = this.mvc.perform(get("/v1/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        RequestTokenDTO requestTokenDTO = deserializeRequestTokenDTO(contentAsString);
        Assertions.assertNotNull(requestTokenDTO);
    }

    private UUID insertItemAndReturnItsSku(ProductAvailabilityEnum productAvailabilityEnum) throws Exception {
        UUID requestId = insertItem(productAvailabilityEnum);

        String productAvailability = null;
        while (productAvailability == null) {
            productAvailability = this.itemAvailabilityRepository.findById(requestId).orElse(null).getProductAvailability();
        }

        MvcResult mvcResult1 = this.mvc.perform(get("/v1/item/" + requestId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.sku").exists())
                .andExpect(jsonPath("$.quantity").exists()).andReturn();
        String responseAsString = mvcResult1.getResponse().getContentAsString();

        CartItemDTO cartItemDTO = deserializeResponseCartDTO(responseAsString);
        Assertions.assertEquals(cartItemDTO.getQuantity(), 2);
        return cartItemDTO.getSku();
    }

    private UUID insertItem(ProductAvailabilityEnum productAvailabilityEnum) throws Exception {
        MvcResult mvcResult = this.mvc.perform(post("/v1/item").content(asJsonString(new CartItemDTO(UUID.randomUUID(), 2))).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        RequestTokenDTO requestTokenDTO = deserializeRequestTokenDTO(contentAsString);
        UUID requestId = requestTokenDTO.getToken();

        this.mvc.perform(get("/v1/item/" + requestId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.sku").doesNotExist())
                .andExpect(jsonPath("$.quantity").doesNotExist());

        this.kafkaProducerKafkaAvailability.send(new ProducerRecord<>(KafkaTopics.CHECK_ITEM_AVAILABILITY_RESPONSE_TOPIC, new ProductAvailabilityResponseDTO(requestId, productAvailabilityEnum)));
        this.kafkaProducerKafkaAvailability.flush();

        return requestId;
    }

    private CartItemDTO deserializeResponseCartDTO(String responseAsString) throws JsonProcessingException {
        return this.objectMapper.readValue(responseAsString, CartItemDTO.class);
    }

    private RequestTokenDTO deserializeRequestTokenDTO(String contentAsString) throws JsonProcessingException {
        return this.objectMapper.readValue(contentAsString, RequestTokenDTO.class);
    }

    private String asJsonString(Object object) throws JsonProcessingException {
        return this.objectMapper.writeValueAsString(object);
    }

    private ExceptionReasonBodyDTO deserializeRequestErrorMessage(String contentAsString) throws JsonProcessingException {
        return this.objectMapper.readValue(contentAsString, ExceptionReasonBodyDTO.class);
    }
}
