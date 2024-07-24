package com.component.orders.contract

import com.component.orders.Application
import io.specmatic.jms.mock.JmsMock
import io.specmatic.jms.mock.models.Expectation
import io.specmatic.stub.ContractStub
import io.specmatic.stub.createStub
import io.specmatic.test.SpecmaticJUnitSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import java.io.File

class ContractTests: SpecmaticJUnitSupport() {

    companion object {
        private lateinit var context: ConfigurableApplicationContext
        private lateinit var httpStub: ContractStub
        private lateinit var jmsMock: JmsMock
        private const val APPLICATION_HOST = "localhost"
        private const val APPLICATION_PORT = "8080"
        private const val HTTP_STUB_HOST = "localhost"
        private const val HTTP_STUB_PORT = 9000
        private const val JMS_MOCK_HOST = "localhost"
        private const val JMS_MOCK_PORT = 9127
        private const val ACTUATOR_MAPPINGS_ENDPOINT = "http://$APPLICATION_HOST:$APPLICATION_PORT/actuator/mappings"
        private const val EXPECTED_NUMBER_OF_MESSAGES = 3

        @JvmStatic
        @BeforeAll
        fun setUp() {
            System.setProperty("host", APPLICATION_HOST)
            System.setProperty("port", APPLICATION_PORT)
            System.setProperty("endpointsAPI", ACTUATOR_MAPPINGS_ENDPOINT)

            // Start Specmatic JMS Mock and set the expectations
            jmsMock = JmsMock.create(JMS_MOCK_HOST, JMS_MOCK_PORT)
            jmsMock.start()
            jmsMock.setExpectations(listOf(Expectation("product-queries", EXPECTED_NUMBER_OF_MESSAGES)))

            // Start Specmatic Http Stub and set the expectations
            httpStub = createStub(HTTP_STUB_HOST, HTTP_STUB_PORT)
            val expectationJsonString = File("./src/test/resources/expectation_for_search_products_api.json").readText()
            httpStub.setExpectation(expectationJsonString)

            // Start Springboot application
            val springApp = SpringApplication(Application::class.java)
            context = springApp.run()
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            // Shutdown Springboot application
            context.close()

            // Shutdown Specmatic Http Stub
            httpStub.close()

            // Verify Specmatic JMS mock and shutdown
            jmsMock.awaitMessages(3)
            val result = jmsMock.verifyExpectations()
            assertThat(result.success).isTrue
            assertThat(result.errors).isEmpty()
            jmsMock.stop()
        }
    }
}

