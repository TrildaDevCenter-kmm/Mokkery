package dev.mokkery

import dev.mokkery.annotations.InternalMokkeryApi

@InternalMokkeryApi
public object MokkeryConfig {

    public const val GROUP: String = BuildConfig.GROUP
    public const val KOTLIN_VERSION: String = BuildConfig.KOTLIN_VERSION
    public const val VERSION: String = BuildConfig.VERSION
    public const val RUNTIME_DEPENDENCY: String = "$GROUP:${BuildConfig.RUNTIME}:$VERSION"
    public const val PLUGIN_ID: String = BuildConfig.PLUGIN_ID
    public const val PLUGIN_ARTIFACT_ID: String = BuildConfig.PLUGIN_ARTIFACT_ID
    public val SUPPORTED_KOTLIN_VERSIONS: List<String> = BuildConfig.SUPPORTED_KOTLIN_VERSIONS.split(", ")
}