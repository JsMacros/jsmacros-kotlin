package io.github.gaming32.jsmacros.kotlin

import io.github.gaming32.jsmacros.kotlin.language.impl.KotlinLanguageDefinition
import io.github.gaming32.jsmacros.kotlin.library.impl.FWrapper
import xyz.wagyourtail.jsmacros.core.Core
import xyz.wagyourtail.jsmacros.core.extensions.Extension
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage
import xyz.wagyourtail.jsmacros.core.language.BaseWrappedException
import xyz.wagyourtail.jsmacros.core.library.BaseLibrary
import java.io.File
import kotlin.concurrent.thread

class KotlinExtension : Extension {
    private var languageDefinition: KotlinLanguageDefinition? = null

    override fun init() {
        thread {
        }
    }

    override fun getPriority(): Int = 0

    override fun getLanguageImplName(): String = "kotlin"

    override fun defaultFileExtension(): String = "kts"

    override fun extensionMatch(file: File): Extension.ExtMatch =
        if (file.name.endsWith(".kts")) {
            if (file.name.contains(languageImplName)) {
                Extension.ExtMatch.MATCH_WITH_NAME
            }
            Extension.ExtMatch.MATCH
        } else Extension.ExtMatch.NOT_MATCH

    override fun getLanguage(core: Core<*, *>): BaseLanguage<*, *> {
        if (languageDefinition == null) {
            val classLoader: ClassLoader = Thread.currentThread().contextClassLoader
            Thread.currentThread().contextClassLoader = KotlinExtension::class.java.classLoader
            languageDefinition = KotlinLanguageDefinition(this, core)
            Thread.currentThread().contextClassLoader = classLoader
        }
        return languageDefinition!!
    }

    override fun getLibraries(): MutableSet<Class<out BaseLibrary>> = mutableSetOf(FWrapper::class.java)

    override fun wrapException(p0: Throwable?): BaseWrappedException<*>? = null

    override fun isGuestObject(p0: Any?): Boolean = false
}