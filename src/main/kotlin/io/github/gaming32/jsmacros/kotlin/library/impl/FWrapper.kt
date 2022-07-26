package io.github.gaming32.jsmacros.kotlin.library.impl

import io.github.gaming32.jsmacros.kotlin.language.impl.KotlinLanguageDefinition
import io.github.gaming32.jsmacros.kotlin.language.impl.KotlinScriptContext
import xyz.wagyourtail.jsmacros.core.Core
import xyz.wagyourtail.jsmacros.core.MethodWrapper
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage
import xyz.wagyourtail.jsmacros.core.library.IFWrapper
import xyz.wagyourtail.jsmacros.core.library.Library
import xyz.wagyourtail.jsmacros.core.library.PerExecLanguageLibrary
import kotlin.concurrent.thread
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

@Library(value = "JavaWrapper", languages = [KotlinLanguageDefinition::class])
class FWrapper(context: KotlinScriptContext, language: Class<out BaseLanguage<BasicJvmScriptingHost, KotlinScriptContext>>) :
    PerExecLanguageLibrary<BasicJvmScriptingHost, KotlinScriptContext>(context, language), IFWrapper<Function<*>> {

    override fun <A : Any, B : Any, R : Any> methodToJava(p0: Function<*>): MethodWrapper<A, B, R, *> {
        return KotlinMethodWrapper(ctx, true, p0)
    }

    override fun <A : Any, B : Any, R : Any> methodToJavaAsync(p0: Function<*>): MethodWrapper<A, B, R, *> {
        return KotlinMethodWrapper(ctx, false, p0)
    }

    fun methodToJava(p0: () -> Unit) : MethodWrapper<*, *, *, *> {
        return KotlinMethodWrapper<Unit, Unit, Unit>(ctx, true, p0)
    }

    fun methodToJavaAsync(p0: () -> Unit) : MethodWrapper<*, *, *, *> {
        return KotlinMethodWrapper<Unit, Unit, Unit>(ctx, false, p0)
    }

    @JvmName("methodToJavaReturning")
    fun <R: Any> methodToJava(p0: () -> R) : MethodWrapper<*, *, R, *> {
        return KotlinMethodWrapper<Unit, Unit, R>(ctx, true, p0)
    }

    @JvmName("methodToJavaReturningAsync")
    fun <R: Any> methodToJavaAsync(p0: () -> R) : MethodWrapper<*, *, R, *> {
        return KotlinMethodWrapper<Unit, Unit, R>(ctx, false, p0)
    }

    fun <A: Any> methodToJava(p0: (A) -> Unit) : MethodWrapper<A, Unit, Unit, *> {
        return KotlinMethodWrapper<A, Unit, Unit>(ctx, true, p0)
    }

    fun <A: Any> methodToJavaAsync(p0: (A) -> Unit) : MethodWrapper<A, Unit, Unit, *> {
        return KotlinMethodWrapper<A, Unit, Unit>(ctx, false, p0)
    }

    @JvmName("methodToJavaReturning")
    fun <A: Any, R: Any> methodToJava(p0: (A) -> R) : MethodWrapper<A, Unit, R, *> {
        return KotlinMethodWrapper<A, Unit, R>(ctx, true, p0)
    }

    @JvmName("methodToJavaReturningAsync")
    fun <A: Any, R: Any> methodToJavaAsync(p0: (A) -> R) : MethodWrapper<A, Unit, R, *> {
        return KotlinMethodWrapper<A, Unit, R>(ctx, false, p0)
    }

    fun <A: Any, B: Any> methodToJava(p0: (A, B) -> Unit) : MethodWrapper<A, B, Unit, *> {
        return KotlinMethodWrapper<A, B, Unit>(ctx, true, p0)
    }

    fun <A: Any, B: Any> methodToJavaAsync(p0: (A, B) -> Unit) : MethodWrapper<A, B, Unit, *> {
        return KotlinMethodWrapper<A, B, Unit>(ctx, false, p0)
    }

    @JvmName("methodToJavaReturning")
    fun <A: Any, B: Any, R: Any> methodToJava(p0: (A, B) -> R) : MethodWrapper<A, B, R, *> {
        return KotlinMethodWrapper<A, B, R>(ctx, true, p0)
    }

    @JvmName("methodToJavaReturningAsync")
    fun <A: Any, B: Any, R: Any> methodToJavaAsync(p0: (A, B) -> R) : MethodWrapper<A, B, R, *> {
        return KotlinMethodWrapper<A, B, R>(ctx, false, p0)
    }

    override fun stop() {
        ctx.closeContext()
    }
}

@Suppress("UNCHECKED_CAST")
private class KotlinMethodWrapper<T : Any, U : Any, R : Any>(ctx: KotlinScriptContext, val await: Boolean, val fn: Any) :
    MethodWrapper<T, U, R, KotlinScriptContext>(ctx) {

    override fun accept(p0: T) {
        if (await) {
            apply(p0)
            return
        }

        thread {
            ctx.bindThread(Thread.currentThread())
            try {
                (fn as (T) -> Unit)(p0)
            } catch (e: Throwable) {
                Core.getInstance().profile.logError(KotlinLanguageDefinition.KotlinRuntimeException(e, ctx.file))
            } finally {
                ctx.releaseBoundEventIfPresent(Thread.currentThread())
                ctx.unbindThread(Thread.currentThread())

                Core.getInstance().profile.joinedThreadStack.remove(Thread.currentThread())
            }
        }
    }

    override fun accept(p0: T, p1: U) {
        if (await) {
            apply(p0, p1)
            return
        }

        thread {
            ctx.bindThread(Thread.currentThread())
            try {
                (fn as (T, U) -> Unit)(p0, p1)
            } catch (e: Throwable) {
                Core.getInstance().profile.logError(KotlinLanguageDefinition.KotlinRuntimeException(e, ctx.file))
            } finally {
                ctx.releaseBoundEventIfPresent(Thread.currentThread())
                ctx.unbindThread(Thread.currentThread())

                Core.getInstance().profile.joinedThreadStack.remove(Thread.currentThread())
            }
        }
    }

    override fun apply(p0: T): R {
        if (ctx.boundThreads.contains(Thread.currentThread())) {
            return (fn as (T) -> R)(p0)
        }

        try {
            ctx.bindThread(Thread.currentThread())
            if (Core.getInstance().profile.checkJoinedThreadStack()) {
                Core.getInstance().profile.joinedThreadStack.add(Thread.currentThread())
            }
            return (fn as (T) -> R)(p0)
        } catch (e: Throwable) {
            throw KotlinLanguageDefinition.KotlinRuntimeException(e, ctx.file)
        } finally {
            ctx.releaseBoundEventIfPresent(Thread.currentThread())
            ctx.unbindThread(Thread.currentThread())
            Core.getInstance().profile.joinedThreadStack.remove(Thread.currentThread())
        }
    }

    override fun apply(p0: T, p1: U): R {
        if (ctx.boundThreads.contains(Thread.currentThread())) {
            return (fn as (T, U) -> R)(p0, p1)
        }

        try {
            ctx.bindThread(Thread.currentThread())
            if (Core.getInstance().profile.checkJoinedThreadStack()) {
                Core.getInstance().profile.joinedThreadStack.add(Thread.currentThread())
            }
            return (fn as (T, U) -> R)(p0, p1)
        } catch (e: Throwable) {
            throw KotlinLanguageDefinition.KotlinRuntimeException(e, ctx.file)
        } finally {
            ctx.releaseBoundEventIfPresent(Thread.currentThread())
            ctx.unbindThread(Thread.currentThread())
            Core.getInstance().profile.joinedThreadStack.remove(Thread.currentThread())
        }
    }

    override fun test(p0: T): Boolean {
        if (ctx.boundThreads.contains(Thread.currentThread())) {
            return (fn as (T) -> Boolean)(p0)
        }

        try {
            ctx.bindThread(Thread.currentThread())
            if (Core.getInstance().profile.checkJoinedThreadStack()) {
                Core.getInstance().profile.joinedThreadStack.add(Thread.currentThread())
            }
            return (fn as (T) -> Boolean)(p0)
        } catch (e: Throwable) {
            throw KotlinLanguageDefinition.KotlinRuntimeException(e, ctx.file)
        } finally {
            ctx.releaseBoundEventIfPresent(Thread.currentThread())
            ctx.unbindThread(Thread.currentThread())
            Core.getInstance().profile.joinedThreadStack.remove(Thread.currentThread())
        }
    }

    override fun test(p0: T, p1: U): Boolean {
        if (ctx.boundThreads.contains(Thread.currentThread())) {
            return (fn as (T, U) -> Boolean)(p0, p1)
        }

        try {
            ctx.bindThread(Thread.currentThread())
            if (Core.getInstance().profile.checkJoinedThreadStack()) {
                Core.getInstance().profile.joinedThreadStack.add(Thread.currentThread())
            }
            return (fn as (T, U) -> Boolean)(p0, p1)
        } catch (e: Throwable) {
            throw KotlinLanguageDefinition.KotlinRuntimeException(e, ctx.file)
        } finally {
            ctx.releaseBoundEventIfPresent(Thread.currentThread())
            ctx.unbindThread(Thread.currentThread())
            Core.getInstance().profile.joinedThreadStack.remove(Thread.currentThread())
        }
    }

    override fun run() {
        if (await) {
            get()
            return
        }

        thread {
            ctx.bindThread(Thread.currentThread())
            try {
                (fn as () -> Unit)()
            } catch (e: Throwable) {
                Core.getInstance().profile.logError(KotlinLanguageDefinition.KotlinRuntimeException(e, ctx.file))
            } finally {
                ctx.releaseBoundEventIfPresent(Thread.currentThread())
                ctx.unbindThread(Thread.currentThread())

                Core.getInstance().profile.joinedThreadStack.remove(Thread.currentThread())
            }
        }
    }

    override fun get(): R {
        if (ctx.boundThreads.contains(Thread.currentThread())) {
            return (fn as () -> R)()
        }

        try {
            ctx.bindThread(Thread.currentThread())
            if (Core.getInstance().profile.checkJoinedThreadStack()) {
                Core.getInstance().profile.joinedThreadStack.add(Thread.currentThread())
            }
            return (fn as () -> R)()
        } catch (e: Throwable) {
            throw KotlinLanguageDefinition.KotlinRuntimeException(e, ctx.file)
        } finally {
            ctx.releaseBoundEventIfPresent(Thread.currentThread())
            ctx.unbindThread(Thread.currentThread())
            Core.getInstance().profile.joinedThreadStack.remove(Thread.currentThread())
        }
    }

    override fun compare(o1: T, o2: T): Int {
        if (ctx.boundThreads.contains(Thread.currentThread())) {
            return (fn as (T,  T) -> Int)(o1, o2)
        }

        try {
            ctx.bindThread(Thread.currentThread())
            if (Core.getInstance().profile.checkJoinedThreadStack()) {
                Core.getInstance().profile.joinedThreadStack.add(Thread.currentThread())
            }
            return (fn as (T, T) -> Int)(o1, o2)
        } catch (e: Throwable) {
            throw KotlinLanguageDefinition.KotlinRuntimeException(e, ctx.file)
        } finally {
            ctx.releaseBoundEventIfPresent(Thread.currentThread())
            ctx.unbindThread(Thread.currentThread())
            Core.getInstance().profile.joinedThreadStack.remove(Thread.currentThread())
        }
    }
}