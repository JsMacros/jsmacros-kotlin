package io.github.gaming32.jsmacros.kotlin.language.impl

import xyz.wagyourtail.jsmacros.core.Core
import xyz.wagyourtail.jsmacros.core.config.ScriptTrigger
import xyz.wagyourtail.jsmacros.core.event.BaseEvent
import xyz.wagyourtail.jsmacros.core.extensions.Extension
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage
import xyz.wagyourtail.jsmacros.core.language.EventContainer
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class KotlinLanguageDefinition(extension: Extension, runner: Core<*, *>) :
    BaseLanguage<BasicJvmScriptingHost, KotlinScriptContext>(extension, runner) {

    private fun internalExec(ctx: EventContainer<KotlinScriptContext>, event: BaseEvent?, callback: (BasicJvmScriptingHost, ScriptCompilationConfiguration, ScriptEvaluationConfiguration) -> Unit) {
        val vars = mapOf(
            "event" to event,
            "file" to ctx.ctx.file,
            "context" to ctx
        )
        val libs = retrieveLibs(ctx.ctx)

        val compConf = object : ScriptCompilationConfiguration({
            jvm {
                // Extract the whole classpath from context classloader and use it as dependencies
                dependenciesFromCurrentContext(wholeClasspath = true)
            }

            providedProperties.replaceOnlyDefault(mapOf(
                "event" to KotlinType(if (event == null) BaseEvent::class else event::class, isNullable = true),
                "file" to KotlinType(File::class, isNullable = true),
                "context" to KotlinType(EventContainer::class)
            ) + libs.mapValues { KotlinType(it.value::class) })
        }) {}
        val execConf = object : ScriptEvaluationConfiguration({
            providedProperties(vars + libs)
        }) {}

        ctx.ctx.context = BasicJvmScriptingHost()

        callback(ctx.ctx.context, compConf, execConf)
    }

    override fun exec(ctx: EventContainer<KotlinScriptContext>, script: ScriptTrigger, event: BaseEvent?) {
        internalExec(ctx, event) { host, compConf, evalConf ->
            val ret = host.eval(ctx.ctx.file!!.toScriptSource(), compConf, evalConf)
            ret.onFailure {
                throw KotlinCompileException(it)
            }
            val retVal = ret.valueOrThrow().returnValue
            if (retVal is ResultValue.Error) {
                throw KotlinRuntimeException(retVal.error, ctx.ctx.file)
            }
        }
    }

    override fun exec(ctx: EventContainer<KotlinScriptContext>, lang: String, script: String, event: BaseEvent?) {
        internalExec(ctx, event) { host, compConf, evalConf ->
            val ret = host.eval(script.toScriptSource(), compConf, evalConf)
            ret.onFailure {
                throw KotlinCompileException(it)
            }
            val retVal = ret.valueOrThrow().returnValue
            if (retVal is ResultValue.Error) {
                throw KotlinRuntimeException(retVal.error, ctx.ctx.file)
            }
        }
    }

    override fun createContext(p0: BaseEvent?, p1: File?): KotlinScriptContext {
        return KotlinScriptContext(p0, p1)
    }

    class KotlinCompileException(val resultWithDiagnostics: ResultWithDiagnostics<*>) : Exception()
    class KotlinRuntimeException(val error: Throwable, val file: File?) : Exception(error)
}