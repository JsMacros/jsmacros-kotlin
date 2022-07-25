package io.github.gaming32.jsmacros.kotlin

import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.providedProperties
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

@KotlinScript(
    compilationConfiguration = JsMacrosScriptConfiguration::class,
    evaluationConfiguration = JsMacrosEvaluationConfiguration::class
)
abstract class JsMacrosScript

object JsMacrosScriptConfiguration : ScriptCompilationConfiguration({
    jvm {
        // Extract the whole classpath from context classloader and use it as dependencies
        dependenciesFromCurrentContext(wholeClasspath = true)
    }
    providedProperties.replaceOnlyDefault(mapOf(
        "test" to KotlinType(String::class)
    ))
})

val propertyValues = mutableMapOf<String, Any?>()

object JsMacrosEvaluationConfiguration : ScriptEvaluationConfiguration({
    providedProperties(propertyValues)
})
