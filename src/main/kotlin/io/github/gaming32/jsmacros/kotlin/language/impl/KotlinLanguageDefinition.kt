package io.github.gaming32.jsmacros.kotlin.language.impl

import xyz.wagyourtail.jsmacros.core.Core
import xyz.wagyourtail.jsmacros.core.config.ScriptTrigger
import xyz.wagyourtail.jsmacros.core.event.BaseEvent
import xyz.wagyourtail.jsmacros.core.extensions.Extension
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage
import xyz.wagyourtail.jsmacros.core.language.EventContainer
import java.io.File
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.providedProperties
import kotlin.script.experimental.host.toScriptSource
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
            providedProperties.replaceOnlyDefault(mapOf(
                "event" to KotlinType(BaseEvent::class, isNullable = true),
                "file" to KotlinType(File::class, isNullable = true),
                "context" to KotlinType(EventContainer::class)
            ))
            providedProperties.replaceOnlyDefault(libs.mapValues { KotlinType(it::class) })
        }) {}
        val execConf = object : ScriptEvaluationConfiguration({
            providedProperties(vars + libs)
        }) {}

        ctx.ctx.context = BasicJvmScriptingHost()

        callback(ctx.ctx.context, compConf, execConf)
    }

    override fun exec(ctx: EventContainer<KotlinScriptContext>, script: ScriptTrigger, event: BaseEvent?) {
        internalExec(ctx, event) { host, compConf, evalConf ->
            host.eval(ctx.ctx.file!!.toScriptSource(), compConf, evalConf)
        }
    }

    override fun exec(ctx: EventContainer<KotlinScriptContext>, lang: String, script: String, event: BaseEvent?) {
        internalExec(ctx, event) { host, compConf, evalConf ->
            host.eval(script.toScriptSource(), compConf, evalConf)
        }
    }

    override fun createContext(p0: BaseEvent?, p1: File?): KotlinScriptContext {
        return KotlinScriptContext(p0, p1)
    }
}