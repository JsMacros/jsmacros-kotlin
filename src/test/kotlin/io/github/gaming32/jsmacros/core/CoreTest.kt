package io.github.gaming32.jsmacros.core

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import xyz.wagyourtail.jsmacros.core.Core
import xyz.wagyourtail.jsmacros.core.event.impl.EventCustom
import xyz.wagyourtail.jsmacros.core.language.EventContainer
import xyz.wagyourtail.jsmacros.stubs.CoreInstanceCreator
import xyz.wagyourtail.jsmacros.stubs.EventRegistryStub
import xyz.wagyourtail.jsmacros.stubs.ProfileStub

import org.junit.jupiter.api.Assertions.assertEquals

class CoreTest {

    @Language("kts")
    private val TEST_SCRIPT: String = """
        event?.putString("rp1", "Hello World!")
        JavaWrapper.methodToJava { event?.putString("rp2", "Hello World!") }.run()
        JavaWrapper.methodToJavaAsync{ event?.putString("rp3", "Hello World!") }.run()
    """.trimIndent()

    @Test
    fun test() {
        println("Testing Core")
        val core: Core<ProfileStub, EventRegistryStub> = CoreInstanceCreator.createCore()
        val event = EventCustom("test")
        val ev: EventContainer<*>? = core.exec("kts", TEST_SCRIPT, null, event, null, null)
        ev?.awaitLock {}
        Thread.sleep(100)
        assertEquals("{rp1=Hello World!, rp3=Hello World!, rp2=Hello World!}", event.underlyingMap.toString())
    }
}