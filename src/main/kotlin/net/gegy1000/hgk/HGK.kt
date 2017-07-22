package net.gegy1000.hgk

import net.gegy1000.hgk.model.PlayerInfoModel
import net.gegy1000.hgk.model.SessionDataModel
import net.gegy1000.hgk.session.GameSession

interface HGK {
    @Throws(RequestErrorException::class)
    fun createSession(channelId: String, players: List<PlayerInfoModel>): GameSession

    @Throws(RequestErrorException::class)
    fun getSessionUpdate(identifier: String, updateIndex: Int, maxSnapshots: Int): SessionDataModel
}
