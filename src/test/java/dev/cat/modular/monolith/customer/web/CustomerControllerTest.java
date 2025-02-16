package dev.cat.modular.monolith.customer.web;

import dev.cat.modular.monolith.calculator.CalculatorAPI;
import dev.cat.modular.monolith.customer.service.CustomerService;
import dev.cat.modular.monolith.customer.repository.CustomerRepository;
import dev.cat.modular.monolith.dto.calculator.CalculatorRequest;
import dev.cat.modular.monolith.dto.customer.CustomerRequest;
import dev.cat.modular.monolith.dto.customer.CustomerResponse;
import dev.cat.modular.monolith.dto.shipment.ShipmentRequest;
import dev.cat.modular.monolith.dto.shipment.ShipmentResponse;
import dev.cat.modular.monolith.shipment.ShipmentAPI;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShipmentAPI shipmentAPI;

    @MockBean
    private CalculatorAPI calculatorAPI;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerController customerController;

    private CustomerRequest customerRequest;
    private CustomerResponse customerResponse;
    private ShipmentRequest shipmentRequest;
    private ShipmentResponse shipmentResponse;
    private CalculatorRequest calculatorRequest;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        customerRequest = new CustomerRequest(
            "John",
            "Doe",
            "USA",
            "+12124567890",
            "john.doe@example.com"
        );
        customerResponse = new CustomerResponse(1L, "John", "Doe", "USA", "+12124567890", "john.doe@example.com");
        shipmentRequest = new ShipmentRequest(10.5, "New York", "Los Angeles", 105.0);
        shipmentResponse = new ShipmentResponse(1L, 1L, 10.5, "New York", "Los Angeles", 105.0, "NEW");
        calculatorRequest = new CalculatorRequest(10.5, "New York", "Los Angeles");
    }

    @Nested
    class UnitTests {
        @Test
        void createCustomer_ShouldReturnCustomerResponse() {
            when(customerService.saveCustomer(any(CustomerRequest.class)))
                    .thenReturn(customerResponse);

            ResponseEntity<CustomerResponse> response = customerController.createCustomer(customerRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(customerResponse);
        }

        @Test
        void createShipmentOrder_ShouldReturnShipmentResponse() {
            // 模擬客戶存在
            when(customerRepository.existsById(anyLong())).thenReturn(true);
            
            // 模擬價格計算
            when(calculatorAPI.calculatePrice(any(CalculatorRequest.class)))
                    .thenReturn(105.0);
            
            // 模擬創建運送訂單
            when(shipmentAPI.createOrder(any(ShipmentRequest.class), anyLong()))
                    .thenReturn(shipmentResponse);

            ResponseEntity<ShipmentResponse> response = customerController.createShipmentOrder(shipmentRequest, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(shipmentResponse);
        }

        @Test
        void calculatePrice_ShouldReturnCalculatedPrice() {
            Double expectedPrice = 100.0;
            when(calculatorAPI.calculatePrice(any(CalculatorRequest.class)))
                    .thenReturn(expectedPrice);

            ResponseEntity<Double> response = customerController.calculatePrice(calculatorRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(expectedPrice);
        }

        @Test
        void getAllOrdersForCustomer_ShouldReturnListOfShipments() {
            // 模擬客戶存在
            when(customerRepository.existsById(anyLong())).thenReturn(true);
            
            List<ShipmentResponse> expectedShipments = Arrays.asList(shipmentResponse);
            when(shipmentAPI.findOrdersByCustomerId(anyLong()))
                    .thenReturn(expectedShipments);

            List<ShipmentResponse> actualShipments = customerController.getAllOrdersForCustomer(1L);

            assertThat(actualShipments).isEqualTo(expectedShipments);
        }
    }

    @Nested
    class IntegrationTests {
        @Test
        void shouldReturnBadRequest_whenRequestBodyIsNull() throws Exception {
            mockMvc.perform(post("/api/quote")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenRequestBodyIsEmptyJson() throws Exception {
            mockMvc.perform(post("/api/quote")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnOk_whenRequestBodyIsValid() throws Exception {
            when(calculatorAPI.calculatePrice(any(CalculatorRequest.class))).thenReturn(105.0);

            mockMvc.perform(post("/api/quote")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(calculatorRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(105.0));
        }
    }

    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}