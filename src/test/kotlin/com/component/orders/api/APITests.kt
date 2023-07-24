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

    @BeforeEach
    fun setupBeforeEach() {
        stub = createStub(SPECMATIC_STUB_HOST, SPECMATIC_STUB_PORT)
    }

    @Test
    fun `test product search api returns a list of products`() {
        val expectationJsonString = File("./src/test/resources/expectation_for_search_products_api.json").readText()
        stub.setExpectation(expectationJsonString)

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

        assertThat(productList).isEqualTo(
            listOf(
                Product(1, "Iphone", 10, "gadget"),
                Product(2, "Macbook", 40, "gadget"),
                Product(31, "Ipad", 20, "gadget")
            )
        )

        jmsMock.awaitMessages(3)
        val result = jmsMock.verifyExpectations()
        assertThat(result.success).isTrue
        assertThat(result.errors).isEmpty()

        assertThat(jmsMock.objectMessageReceivedOnChannel(queueName, ProductMessage(1, "Iphone", 10))).isTrue
        assertThat(jmsMock.objectMessageReceivedOnChannel(queueName, ProductMessage(2, "Macbook", 40))).isTrue
        assertThat(jmsMock.objectMessageReceivedOnChannel(queueName, ProductMessage(31, "Ipad", 20))).isTrue
    }

    @Test
    fun `test create order api returns id of the order created`() {
        val expectationJsonString = File("./src/test/resources/expectation_for_create_orders_api.json").readText()
        stub.setExpectation(expectationJsonString)

        val order = OrderRequest(10, 1)
        val response = RestTemplate().exchange(
            orderApiUrl,
            HttpMethod.POST,
            HttpEntity(order),
            OrderResponse::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(OrderResponse(10, "success"))
    }

    @AfterEach
    fun tearDownAfterEach() {
        stub.close()
    }

    companion object {
        private var service: ConfigurableApplicationContext? = null
        private lateinit var stub: ContractStub
        private lateinit var jmsMock: JmsMock
        private const val SPECMATIC_STUB_HOST = "localhost"
        private const val SPECMATIC_STUB_PORT = 9000
        private const val queueName = "product-queries"
        private const val searchProductsApiUrl = "http://localhost:8080/findAvailableProducts?type=gadget"
        private const val orderApiUrl = "http://localhost:8080/orders"

        @BeforeAll
        @JvmStatic
        fun setUp() {
            jmsMock = JmsMock.create()
            jmsMock.start()
            jmsMock.setExpectations(listOf(Expectation(queueName, 3)))

            service = SpringApplication.run(Application::class.java)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            service?.close()
            jmsMock.stop()
        }
    }
}