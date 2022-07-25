package xyz.wagyourtail.jsmacros.stubs

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.wagyourtail.jsmacros.core.Core
import java.io.File


object CoreInstanceCreator {
    private val LOGGER: Logger = LoggerFactory.getLogger("JsMacros")
    private val configFolder: File = File("run/config")
    private val macroFolder: File = File(configFolder, "macro")

    @Suppress("UNCHECKED_CAST")
    fun createCore(): Core<ProfileStub, EventRegistryStub> {
        var instance: Core<ProfileStub, EventRegistryStub>? = Core.getInstance() as Core<ProfileStub, EventRegistryStub>?
        if (instance == null) {
            instance = Core.createInstance(
                { EventRegistryStub(it) },
                { core, logger -> ProfileStub(core, logger) },
                configFolder,
                macroFolder,
                LOGGER
            )
            instance.deferredInit()
        }
        return instance!!
    }
}