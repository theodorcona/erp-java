package com.example.erp

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestErpApplication::class)
@Disabled
class EntityControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc
    @Test
    fun test() {
        mockMvc.post("/entities") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "entityName": "User",
                    "schema": {
                        "properties": [
                            {
                                "key": "address",
                                "type": "OBJECT",
                                "objectProperties": [
                                    {
                                        "key": "street",
                                        "type": "STRING"
                                    },
                                    {
                                        "key": "city",
                                        "type": "STRING"
                                    },
                                    {
                                        "key": "number",
                                        "type": "LONG"
                                    }
                                ]
                            },
                            {
                                "key": "email",
                                "type": "STRING"
                            }
                        ]
                    }
                }
            """.trimIndent()
        }.andExpect { status { isOk() } }

        mockMvc.post("/entities/User") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "data": {
                        "address": {
                            "street": "zarquon",
                            "city": "zenquin",
                            "number": 42
                        },
                        "email": "zaphod@beeblebrox.com"
                    }
                }
            """.trimIndent()
        }.andExpect { status { isOk() } }.andReturn().response.contentAsString
        val content2 = mockMvc.post("/entities/User") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "data": {
                        "address": {
                            "street": "zarquon2",
                            "city": "zenquin2",
                            "number": 43
                        },
                        "email": "zaphod2@beeblebrox.com"
                    }
                }
            """.trimIndent()
        }.andExpect { status { isOk() } }.andReturn().response.contentAsString

        val expectedResponse2 = """
            {
                "nextCursor": null,
                "items": [
                    $content2
                ]
            }
        """.trimIndent()
        val firstPageResponse = mockMvc.get("/entities/User?pageSize=1")
            .andExpect {
                content {
                    jsonPath("items") {
                        isArray()
                    }
                }
            }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsString
        val nextCursor = ObjectMapper().readTree(firstPageResponse).get("nextCursor").textValue()
        mockMvc.get("/entities/User?pageSize=1&cursor=$nextCursor")
            .andExpect {
                content {
                    json(expectedResponse2)
                }
            }
            .andExpect { status { isOk() } }
    }
}
