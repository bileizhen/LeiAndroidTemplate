package io.github.bileizhen.leitemplate.core.update

fun interface ReleaseChecker {
    fun check(channel: UpdateChannel): AppRelease?
}
