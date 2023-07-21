package dev.mokkery.matcher.capture

import dev.mokkery.internal.MissingMatchersForComposite
import dev.mokkery.matcher.ArgMatcher

/**
 * Matches an argument with [matcher] and captures matching arguments into [capture].
 */
public class CaptureMatcher<T>(
    public val capture: Capture<T>,
    public val matcher: ArgMatcher<T>? = null,
) : ArgMatcher.Composite<T> {

    override fun matches(arg: T): Boolean {
        if (matcher!!.matches(arg)) {
            capture.capture(arg)
            return true
        }
        return false
    }

    override fun compose(matcher: ArgMatcher<T>): ArgMatcher.Composite<T> = CaptureMatcher(capture, matcher)

    override fun isFilled(): Boolean = matcher != null

    override fun assertFilled() {
        if (matcher == null) {
            throw MissingMatchersForComposite("capture", 1, listOfNotNull(matcher))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CaptureMatcher<*>

        return matcher == other.matcher
    }

    override fun hashCode(): Int {
        return matcher?.hashCode() ?: 0
    }

    override fun toString(): String = "capture($capture, $matcher)"
}
