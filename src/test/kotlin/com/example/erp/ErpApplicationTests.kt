package com.example.erp

import com.example.erp.common.Operators
import com.example.erp.common.SchemaProperties
import com.example.erp.entity.EntityServiceFactory
import com.example.erp.entitymeta.EntityMetadata
import com.example.erp.entitymeta.EntityMetadataService
import com.example.erp.event.ApplicationUser
import com.example.erp.event.Event
import com.example.erp.event.EventDescriptor
import com.example.erp.event.EventHandlerRegistry
import com.example.erp.eventlink.EVENT_LINK_COLLECTION
import com.example.erp.eventlink.EventLink
import com.example.erp.eventmeta.EVENT_METADATA_COLLECTION
import com.example.erp.eventmeta.EventMetadata
import com.example.erp.kafka.ErpKafkaProducer
import com.example.erp.logic.ApplyInput
import com.example.erp.logic.MonoApply
import com.example.erp.logic.OtherDataInput
import com.example.erp.logic.StaticInput
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.modulith.core.ApplicationModules
import java.util.*


@SpringBootTest
@Import(TestErpApplication::class)
@Disabled
class ErpApplicationTests {

    @Autowired
    private lateinit var kafkaProducer: ErpKafkaProducer

    @Autowired
    private lateinit var eventHandlerRegistry: EventHandlerRegistry

    @Autowired
    private lateinit var entityServiceFactory: EntityServiceFactory

    @Autowired
    private lateinit var entityMetadataService: EntityMetadataService

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @AfterEach
    fun clearDB() {
        mongoTemplate.db.drop()
    }

    @BeforeEach
    fun setup() {
        registerNecessaryEntities()
    }

    @Test
    @Disabled
    fun applicationModulesVerify() {
        ApplicationModules.of(ErpApplication::class.java).verify()
    }

    @Test
    fun testSimpleEventHandler() {
        val eventMetadataService = entityServiceFactory.getServiceForEntity(EVENT_METADATA_COLLECTION)
        eventMetadataService.insert(EventMetadata("TEST", SchemaProperties.Schema(emptyMap())))
        val receivedEvents = mutableListOf<Event>()
        eventHandlerRegistry.registerEventHandler(EventDescriptor(TestData::class.java, "TEST")) { _, event ->
            receivedEvents.add(event)
        } shouldBe true
        runBlocking {
            kafkaProducer.send(
                "events", Event(
                    "TEST",
                    ApplicationUser(UUID.randomUUID(), ApplicationUser.Type.USER),
                    "test_data"
                )
            )
            waitUntil { receivedEvents.size == 1 }
        }
    }

    @Test
    fun testStaticEventLink() {
        val eventMetadataService = entityServiceFactory.getServiceForEntity(EVENT_METADATA_COLLECTION)
        val eventLinkService = entityServiceFactory.getServiceForEntity(EVENT_LINK_COLLECTION)
        eventMetadataService.insert(
            EventMetadata(
                "INPUT",
                SchemaProperties.Schema(
                    mapOf(
                        "name" to SchemaProperties.String().toIndexable(false)
                    )
                )
            )
        )
        eventMetadataService.insert(
            EventMetadata(
                "OUTPUT",
                SchemaProperties.Schema(
                    mapOf(
                        "name" to SchemaProperties.String().toIndexable(false)
                    )
                )
            )
        )
        eventLinkService.insert(
            EventLink(
                inputEvent = "INPUT",
                outputEvent = "OUTPUT",
                properties = mapOf(
                    "name" to StaticInput("beeblebrox", SchemaProperties.PropertyType.STRING)
                )
            )
        )

        val receivedEvents = mutableListOf<Event>()
        eventHandlerRegistry.registerEventHandler(EventDescriptor(TestData::class.java, "OUTPUT")) { _, event ->
            receivedEvents.add(event)
        } shouldBe true
        runBlocking {
            kafkaProducer.send("events", Event(
                    "INPUT",
                    ApplicationUser(UUID.randomUUID(), ApplicationUser.Type.USER),
                    """{"name":"zaphod"}"""
                )
            )
            waitUntil { receivedEvents.size == 1 }
            receivedEvents[0].data shouldBe """{"name":"beeblebrox"}"""
        }
    }

    @Test
    fun testOtherEventLink() {
        val eventMetadataService = entityServiceFactory.getServiceForEntity(EVENT_METADATA_COLLECTION)
        val eventLinkService = entityServiceFactory.getServiceForEntity(EVENT_LINK_COLLECTION)
        eventMetadataService.insert(
            EventMetadata(
                "INPUT",
                SchemaProperties.Schema(
                    mapOf(
                        "name" to SchemaProperties.String().toIndexable(false)
                    )
                )
            )
        )
        eventMetadataService.insert(
            EventMetadata(
                "OUTPUT",
                SchemaProperties.Schema(
                    mapOf(
                        "name" to SchemaProperties.String().toIndexable(false)
                    )
                )
            )
        )
        eventLinkService.insert(
            EventLink(
                inputEvent = "INPUT",
                outputEvent = "OUTPUT",
                properties = mapOf(
                    "name" to OtherDataInput("name", SchemaProperties.PropertyType.STRING)
                )
            )
        )

        val receivedEvents = mutableListOf<Event>()
        eventHandlerRegistry.registerEventHandler(EventDescriptor(TestData::class.java, "OUTPUT")) { _, event ->
            receivedEvents.add(event)
        } shouldBe true
        runBlocking {
            kafkaProducer.send("events", Event(
                "INPUT",
                ApplicationUser(UUID.randomUUID(), ApplicationUser.Type.USER),
                """{"name":"zaphod"}"""
            )
            )
            waitUntil { receivedEvents.size == 1 }
            receivedEvents[0].data shouldBe """{"name":"zaphod"}"""
        }
    }

    @Test
    fun testDynamicEventLink() {
        val eventMetadataService = entityServiceFactory.getServiceForEntity(EVENT_METADATA_COLLECTION)
        val eventLinkService = entityServiceFactory.getServiceForEntity(EVENT_LINK_COLLECTION)
        eventMetadataService.insert(
            EventMetadata(
                "INPUT",
                SchemaProperties.Schema(
                    mapOf(
                        "name" to SchemaProperties.String().toIndexable(false)
                    )
                )
            )
        )
        // EVENT METADATA: UserClick { userName: String, timestamp: Date }
        eventMetadataService.insert(
            EventMetadata(
                "OUTPUT",
                SchemaProperties.Schema(
                    mapOf(
                        "name" to SchemaProperties.String().toIndexable(false)
                    )
                )
            )
        )
        eventLinkService.insert(
            EventLink(
                inputEvent = "INPUT",
                outputEvent = "OUTPUT",
                properties = mapOf(
                    "name" to ApplyInput(MonoApply(
                        Operators.UpperCaseOperator(),
                        OtherDataInput("name", SchemaProperties.PropertyType.STRING))
                    )
                )
            )
        )

        val receivedEvents = mutableListOf<Event>()
        eventHandlerRegistry.registerEventHandler(EventDescriptor(TestData::class.java, "OUTPUT")) { _, event ->
            receivedEvents.add(event)
        } shouldBe true
        runBlocking {
            kafkaProducer.send("events", Event(
                "INPUT",
                ApplicationUser(UUID.randomUUID(), ApplicationUser.Type.USER),
                """{"name":"zaphod"}"""
            )
            )
            waitUntil { receivedEvents.size == 1 }
            receivedEvents[0].data shouldBe """{"name":"ZAPHOD"}"""
        }
    }

    private fun registerNecessaryEntities() {
        try {
            entityMetadataService.insertEntityMetadata(
                EntityMetadata(
                    collection = EVENT_METADATA_COLLECTION.collectionName,
                    schema = SchemaProperties.Schema(
                        mapOf(
                            "eventName" to SchemaProperties.String().toIndexable(true),
                            "schema" to SchemaProperties.DynamicObject().toIndexable()
                        )
                    )
                )
            )
            entityMetadataService.insertEntityMetadata(
                EntityMetadata(
                    collection = EVENT_LINK_COLLECTION.collectionName,
                    schema = SchemaProperties.Schema(
                        mapOf(
                            EVENT_LINK_COLLECTION.PROPERTIES.inputEvent.path to SchemaProperties.String()
                                .toIndexable(true),
                            "outputEvent" to SchemaProperties.String().toIndexable(),
                            "properties" to SchemaProperties.DynamicObject().toIndexable()
                        )
                    )
                )
            )
        } catch (_: Throwable) {
        }
    }

    data class TestData(
        val name: String
    )

}
