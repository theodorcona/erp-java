package com.example.erp.common

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class JacksonConfig {
    @Bean
    fun kotlinModule(): Module {
        return KotlinModule.Builder().build()
    }
    @Bean
    fun jodaModule(): Module {
        return JodaModule()
    }
}