package io.github.gaming32.jsmacros.kotlin.language.impl

import xyz.wagyourtail.jsmacros.core.event.BaseEvent
import xyz.wagyourtail.jsmacros.core.language.BaseScriptContext
import java.io.File
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class KotlinScriptContext(event: BaseEvent?, file: File?) : BaseScriptContext<BasicJvmScriptingHost>(event, file) {

    override fun isMultiThreaded(): Boolean = true
}