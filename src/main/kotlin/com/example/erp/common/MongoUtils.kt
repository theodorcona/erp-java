package com.example.erp.common

import com.example.erp.rest.PageDTO
import com.example.erp.rest.RangeQuery
import com.fasterxml.jackson.databind.ObjectMapper
import org.joda.time.DateTime
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.domain.Page
import org.springframework.data.mongodb.core.convert.MongoCustomConversions


class DateTimeReadConverter : Converter<Long, DateTime> {
    override fun convert(source: Long): DateTime {
        return DateTime(source)
    }
}

class DateTimeWriteConverter : Converter<DateTime, Long> {
    override fun convert(source: DateTime): Long {
        return source.millis
    }
}

@Configuration
class MyMongoConfiguration {

    @Bean
    fun mongoCustomConversions(objectMapper: ObjectMapper): MongoCustomConversions {
        return MongoCustomConversions(
            listOf(
                DateTimeWriteConverter(),
                DateTimeReadConverter()
            )
        )
    }
}

val firstUuid = "00000000-0000-0000-0000-000000000000"
val firstDate = "1970-01-01T00:00:00.000Z"

fun <T> Page<T>.toPagedResponse(
    rangeQuery: RangeQuery,
    cursorMapping: (T) -> String
): PageDTO<T> {
    if (content.isEmpty()) {
        return PageDTO(nextCursor = null, items = emptyList())
    } else if (content.size <= rangeQuery.pageSize) {
        return PageDTO(nextCursor = null, items = content)
    } else {
        return PageDTO(
            nextCursor = cursorMapping(last()),
            items = content.toList().subList(0, content.size - 1)
        )
    }

}