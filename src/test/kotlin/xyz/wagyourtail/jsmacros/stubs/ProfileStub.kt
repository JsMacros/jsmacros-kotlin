package xyz.wagyourtail.jsmacros.stubs

import org.slf4j.Logger
import xyz.wagyourtail.jsmacros.core.Core
import xyz.wagyourtail.jsmacros.core.EventLockWatchdog
import xyz.wagyourtail.jsmacros.core.config.BaseProfile
import xyz.wagyourtail.jsmacros.core.config.CoreConfigV2
import xyz.wagyourtail.jsmacros.core.event.BaseEvent
import xyz.wagyourtail.jsmacros.core.event.IEventListener
import xyz.wagyourtail.jsmacros.core.event.impl.EventCustom
import xyz.wagyourtail.jsmacros.core.library.impl.FJsMacros.ScriptEventListener
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class ProfileStub(runner: Core<*, *>, logger: Logger) : BaseProfile(runner, logger) {
    companion object {
        var th: Thread? = null
        private var runnables: LinkedBlockingQueue<Runnable> = LinkedBlockingQueue()

        init {
            thread(isDaemon = true) {
                try {
                    Thread.sleep(50)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
                while (true) {
                    try {
                        runnables.take().run()
                    } catch (e: InterruptedException) {
                        throw RuntimeException(e)
                    }
                }
            }
        }
    }

    init {
        joinedThreadStack.add(th)
    }

    override fun logError(ex: Throwable?) {
        runner.wrapException(ex)
        LOGGER.error("", ex)
    }

    override fun checkJoinedThreadStack(): Boolean {
        return joinedThreadStack.contains(Thread.currentThread())
    }

    override fun triggerEventJoin(event: BaseEvent) {
        val joinedMain = checkJoinedThreadStack()
        triggerEventJoinNoAnything(event)
        for (macro in runner.eventRegistry.getListeners("ANYTHING")) {
            runJoinedEventListener(event, joinedMain, macro)
        }
    }

    override fun triggerEventJoinNoAnything(event: BaseEvent) {
        val joinedMain = checkJoinedThreadStack()
        if (event is EventCustom) {
            for (macro in runner.eventRegistry.getListeners(event.eventName)) {
                runJoinedEventListener(event, joinedMain, macro)
            }
        } else {
            for (macro in runner.eventRegistry.getListeners(event.eventName)) {
                runJoinedEventListener(event, joinedMain, macro)
            }
        }
    }


    private fun runJoinedEventListener(event: BaseEvent, joinedMain: Boolean, macroListener: IEventListener) {
        if ((macroListener is ScriptEventListener) && macroListener.creator === Thread.currentThread() && macroListener.wrapper.preventSameThreadJoin()) {
            throw IllegalThreadStateException("Cannot join $macroListener on same thread as it's creation.")
        }
        val t = macroListener.trigger(event) ?: return
        try {
            if (joinedMain) {
                joinedThreadStack.add(t.lockThread)
                EventLockWatchdog.startWatchdog(
                    t, macroListener, Core.getInstance().config.getOptions(
                        CoreConfigV2::class.java
                    ).maxLockTime
                )
            }
            t.awaitLock { joinedThreadStack.remove(t.lockThread) }
        } catch (ignored: InterruptedException) {
            joinedThreadStack.remove(t.lockThread)
        }
    }
}