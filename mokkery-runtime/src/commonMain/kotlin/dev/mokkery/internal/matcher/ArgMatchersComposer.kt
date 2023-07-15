package dev.mokkery.internal.matcher

import dev.mokkery.internal.MultipleMatchersForSingleArgException
import dev.mokkery.internal.arrayElementType
import dev.mokkery.internal.tracing.CallArg
import dev.mokkery.matcher.ArgMatcher

internal interface ArgMatchersComposer {

    fun compose(arg: CallArg, matchers: List<ArgMatcher<Any?>>): ArgMatcher<Any?>
}

internal fun ArgMatchersComposer(): ArgMatchersComposer = ArgMatchersComposerImpl()

private class ArgMatchersComposerImpl : ArgMatchersComposer {
    override fun compose(arg: CallArg, matchers: List<ArgMatcher<Any?>>): ArgMatcher<Any?> {
        return when {
            arg.isVararg -> compose(arg.name, matchers + CompositeVarArgMatcher(arg.value.arrayElementType()))
            matchers.isEmpty() -> ArgMatcher.Equals(arg.value)
            else -> compose(arg.name, matchers)
        }
    }

    private fun compose(name: String, matchers: List<ArgMatcher<Any?>>): ArgMatcher<Any?> {
        val stack = mutableListOf<ArgMatcher<Any?>>()
        for (it in matchers) {
            if (it !is ArgMatcher.Composite<Any?>) {
                stack += it
                continue
            }
            var composite: ArgMatcher.Composite<Any?> = it
            while (stack.isNotEmpty() && !composite.isFilled()) {
                composite = composite.compose(stack.removeLast())
            }
            composite.assertValid()
            stack += composite
        }
        return stack.singleOrNull() ?: throw MultipleMatchersForSingleArgException(name, stack)
    }
}
