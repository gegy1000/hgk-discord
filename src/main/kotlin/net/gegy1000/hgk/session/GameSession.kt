package net.gegy1000.hgk.session

import net.gegy1000.hgk.HGKDiscord
import net.gegy1000.hgk.HGKDiscord.StatusUpdate
import net.gegy1000.hgk.model.SessionDataModel
import net.gegy1000.hgk.model.SessionSetupModel
import kotlin.concurrent.fixedRateTimer

class GameSession(val channelId: String, val setupInfo: SessionSetupModel) {
    val identifier = setupInfo.identifier

    var updateIndex: Int = 0

    var cancel: Boolean = false

    val updateTimer = fixedRateTimer(name = "Session $identifier", daemon = true, initialDelay = 100, period = setupInfo.tickRateMillis) {
        try {
            val updates = update(HGKDiscord.hgk.getSessionUpdate(identifier, updateIndex, 5))
            HGKDiscord.updateQueue += updates.map { StatusUpdate(channelId, it) }
            if (cancel) {
                HGKDiscord.activeSessions.remove(this@GameSession)
                cancel()
            }
        } catch (e: Exception) {
            HGKDiscord.activeSessions.remove(this@GameSession)
            cancel = true
            cancel()
            HGKDiscord.updateQueue += StatusUpdate(channelId, "${e::class.qualifiedName}: ${e.message ?: "Failed to get session update"}")
            HGKDiscord.logger.error("Failed to get session update for $identifier", e)
        }
    }

    fun update(updateData: SessionDataModel): List<String> {
        val statusUpdates = ArrayList<String>()
        updateIndex = updateData.currentUpdate
        updateData.snapshots.forEach { statusUpdates += it.statusUpdates }
        return statusUpdates
    }

    override fun equals(other: Any?) = other is GameSession && other.identifier == identifier

    override fun hashCode() = identifier.hashCode()
}
