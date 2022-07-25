package xyz.wagyourtail.jsmacros.stubs

import xyz.wagyourtail.jsmacros.core.Core
import xyz.wagyourtail.jsmacros.core.config.ScriptTrigger
import xyz.wagyourtail.jsmacros.core.event.BaseEventRegistry


class EventRegistryStub(runner: Core<*, *>) : BaseEventRegistry(runner) {
    override fun addScriptTrigger(rawmacro: ScriptTrigger) {
        // no-op
    }

    override fun removeScriptTrigger(rawmacro: ScriptTrigger): Boolean {
        throw AssertionError("not implemented")
    }

    override fun getScriptTriggers(): List<ScriptTrigger?> {
        return listOf()
    }
}