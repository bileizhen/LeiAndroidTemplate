package io.github.bileizhen.leitemplate.data.update

import io.github.bileizhen.leitemplate.core.update.UpdateChannel

data class UpdateSettings(
    val autoCheckOnLaunch: Boolean = true,
    val channel: UpdateChannel = UpdateChannel.STABLE,
)
