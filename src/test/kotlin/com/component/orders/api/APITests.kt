package com.component.orders.api

import com.component.orders.Application
import com.intuit.karate.junit5.Karate
import `in`.specmatic.jms.mock.JmsMock
import `in`.specmatic.jms.mock.models.Expectation
import `in`.specmatic.stub.ContractStub
import `in`.specmatic.stub.createStub
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class APITests {
    @Karate.Test
    @Order(1)
    fun apiTests(): Karate {
        return Karate().path("apiTests.feature").relativeTo(this::class.java)
    }

    @Test
    @Order(2)
    fun `test expectations set on the jms mock are met`() {
        jmsMock.awaitMessages(3)
        val result = jmsMock.verifyExpectations()
        assertThat(result.success).isTrue
        assertThat(result.errors).isEmpty()
    }

    companion object {
        private var service: ConfigurableApplicationContext? = null
        private lateinit var stub: ContractStub
        private lateinit var jmsMock: JmsMock

        @BeforeAll
        @JvmStatic
        fun setUp() {
            stub = createStub()
            jmsMock = JmsMock.create()
            jmsMock.start()
            jmsMock.setExpectations(listOf(Expectation("product-queries", 3)))
            service = SpringApplication.run(Application::class.java)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            service?.close()
            stub.close()
            jmsMock.stop()
        }
    }
}