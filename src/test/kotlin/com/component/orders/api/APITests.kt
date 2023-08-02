package com.component.orders.api

import com.component.orders.Application
import com.component.orders.models.OrderRequest
import com.component.orders.models.OrderResponse
import com.component.orders.models.Product
import com.component.orders.models.messages.ProductMessage
import `in`.specmatic.jms.mock.JmsMock
import `in`.specmatic.jms.mock.models.Expectation
import `in`.specmatic.stub.ContractStub
import `in`.specmatic.stub.createStub
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import java.io.File


class APITests {

    @Test
    fun `test product search api returns a list of products`() {
        // Arrange
        // Set expectation on Specmatic JMS Mock
        jmsMock.setExpectations(listOf(Expectation(productQueries, 3)))

        // Set expectation on HTTP Stub
        val expectationJsonString = File("./src/test/resources/expectation_for_search_products_api.json").readText()
        httpStub.setExpectation(expectationJsonString)

        // Act
        val response = RestTemplate().getForEntity(searchProductsApiUrl, List::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val productList = response.body.map {
            val product = it as Map<*, *>
            Product(
                product["id"].toString().toInt(),
                product["name"].toString(),
                product["inventory"].toString().toInt(),
                product["type"].toString()
            )
        }

        // Assert
        // Assert API response
        assertThat(productList).isEqualTo(
            listOf(
                Product(1, "Iphone", 10, "gadget"),
                Product(2, "Macbook", 40, "gadget"),
                Product(31, "Ipad", 20, "gadget")
            )
        )

        // Verify Specmatic JMS Mock expectations
        jmsMock.awaitMessages(3)
        val result = jmsMock.verifyExpectations()
        assertThat(result.success).isTrue
        assertThat(result.errors).isEmpty()

        // Verify actual messages received by Specmatic JMS Mock
        assertThat(jmsMock.objectMessageReceivedOnChannel(productQueries, ProductMessage(1, "Iphone", 10))).isTrue
        assertThat(jmsMock.objectMessageReceivedOnChannel(productQueries, ProductMessage(2, "Macbook", 40))).isTrue
        assertThat(jmsMock.objectMessageReceivedOnChannel(productQueries, ProductMessage(31, "Ipad", 20))).isTrue
    }

    @Test
    fun `test create order api returns id of the order created`() {
        // Arrange
        val expectationJsonString = File("./src/test/resources/expectation_for_create_orders_api.json").readText()
        httpStub.setExpectation(expectationJsonString)

        // Act
        val order = OrderRequest(10, 1)
        val response = RestTemplate().exchange(
            orderApiUrl,
            HttpMethod.POST,
            HttpEntity(order),
            OrderResponse::class.java
        )

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(OrderResponse(10, "success"))
    }


    companion object {
        private lateinit var service: ConfigurableApplicationContext
        private lateinit var httpStub: ContractStub
        private lateinit var jmsMock: JmsMock
        private const val HTTP_STUB_HOST = "localhost"
        private const val HTTP_STUB_PORT = 9000
        private const val JMS_MOCK_HOST = "localhost"
        private const val JMS_MOCK_PORT = 9127
        private const val productQueries = "product-queries"
        private const val searchProductsApiUrl = "http://localhost:8080/findAvailableProducts?type=gadget"
        private const val orderApiUrl = "http://localhost:8080/orders"

        @BeforeAll
        @JvmStatic
        fun setUp() {
            // Start Specmatic Http Stub
            httpStub = createStub(HTTP_STUB_HOST, HTTP_STUB_PORT)

            // Start Specmatic JMS Mock
            jmsMock = JmsMock.create(JMS_MOCK_HOST, JMS_MOCK_PORT)
            jmsMock.start()

            // Start Springboot application
            service = SpringApplication.run(Application::class.java)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            // Shutdown Springboot application
            service.close()

            // Shutdown Specmatic Http Stub
            httpStub.close()

            // Shutdown Specmatic JMS Mock
            jmsMock.stop()
        }
    }
}