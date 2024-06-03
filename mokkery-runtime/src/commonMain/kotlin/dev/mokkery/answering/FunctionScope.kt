package dev.mokkery.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.MissingArgsForSuperMethodException
import dev.mokkery.internal.MissingSuperMethodException
import dev.mokkery.internal.ObjectNotMockedException
import dev.mokkery.internal.SuperTypeMustBeSpecifiedException
import dev.mokkery.internal.bestName
import dev.mokkery.internal.dynamic.MokkeryScopeLookup
import dev.mokkery.internal.unsafeCast
import dev.mokkery.internal.unsafeCastOrNull
import kotlin.reflect.KClass


/**
 * Provides a set of mocked function related operations that might be required for implementing [Answer].
 */
@DelicateMokkeryApi
@Poko
public class FunctionScope internal constructor(
    /**
     * Return type of mocked method.
     */
    public val returnType: KClass<*>,
    /**
     * Args passed to mocked method. If method has extension receiver it is passed at the start of this list.
     */
    public val args: List<Any?>,
    /**
     * Reference to this mock.
     */
    public val self: Any?,

    /**
     * This map contains available super calls as lambdas of type `(List<Any?>) -> Any?` or
     * `suspend (List<Any?>) -> Any?` depending on a function type.
     *
     * Use [callSuper], [callSuspendSuper], [callOriginal], [callSuspendOriginal] for convenience.
     */
    public val supers: Map<KClass<*>, Function<Any?>>
) {

    /**
     * Returns argument with [index] from [args] and expects that it is an instance of type [T].
     */
    public inline fun <reified T> arg(index: Int): T = args[index] as T

    /**
     * Calls super method of [superType] with given [args]
     */
    public fun callSuper(superType: KClass<*>, args: List<Any?>): Any? {
        if (this.args.size != args.size) {
            throw MissingArgsForSuperMethodException(this.args.size, args.size)
        }
        return supers[superType]
            .unsafeCastOrNull<(List<Any?>) -> Any?>()
            .let { it ?: throw MissingSuperMethodException(superType) }
            .invoke(args)
    }

    /**
     * Just like [callSuper] but for suspend calls.
     */
    public suspend fun callSuspendSuper(superType: KClass<*>, args: List<Any?>): Any? {
        if (this.args.size != args.size) {
            throw MissingArgsForSuperMethodException(this.args.size, args.size)
        }
        return supers[superType]
            .unsafeCastOrNull<suspend (List<Any?>) -> Any?>()
            .let { it ?: throw MissingSuperMethodException(superType) }
            .invoke(args)
    }

    /**
     * Calls original method implementation with given [args].
     */
    public fun callOriginal(args: List<Any?>): Any? = callOriginal(MokkeryScopeLookup.current, args)

    /**
     * Just like [callOriginal] but for suspend calls.
     */
    public suspend fun callSuspendOriginal(args: List<Any?>): Any? = callSuspendOriginal(MokkeryScopeLookup.current, args)

    internal fun callOriginal(lookup: MokkeryScopeLookup, args: List<Any?>): Any? {
        checkArgs(args)
        val superType = resolveOriginalSupertype(lookup)
        return supers
            .getValue(superType)
            .unsafeCast<(List<Any?>) -> Any?>()
            .invoke(args)
    }

    internal suspend fun callSuspendOriginal(lookup: MokkeryScopeLookup, args: List<Any?>): Any? {
        checkArgs(args)
        val superType = resolveOriginalSupertype(lookup)
        return supers
            .getValue(superType)
            .unsafeCast<suspend (List<Any?>) -> Any?>()
            .invoke(args)
    }

    private fun resolveOriginalSupertype(lookup: MokkeryScopeLookup): KClass<*> {
        val selfScope = lookup.resolve(self) ?: throw ObjectNotMockedException(self)
        val superCandidates = selfScope.interceptedTypes.filter(supers::contains)
        if (superCandidates.isEmpty()) throw MissingSuperMethodException(selfScope.interceptedTypes)
        val superType = superCandidates
            .singleOrNull()
            ?: throw SuperTypeMustBeSpecifiedException(
                "Multiple original super calls available ${superCandidates.map(KClass<*>::bestName)}!"
            )
        return superType
    }

    private fun checkArgs(args: List<Any?>) {
        if (this.args.size != args.size) {
            throw MissingArgsForSuperMethodException(this.args.size, args.size)
        }
    }
}
