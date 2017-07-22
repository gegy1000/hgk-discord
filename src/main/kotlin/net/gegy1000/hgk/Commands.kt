package net.gegy1000.hgk

import net.gegy1000.hgk.model.PlayerInfoModel
import net.gegy1000.hgk.model.PlayerInfoModel.Pronoun
import net.gegy1000.hgk.session.SessionBuilder
import sx.blah.discord.handle.obj.IMessage
import java.util.Locale

const val COMMAND_PREFIX = "."

val BUILD_GAME = Command("build") { message, args ->
    val channelId = message.channel.id
    if (!HGKDiscord.sessionBuilders.containsKey(channelId)) {
        HGKDiscord.sessionBuilders.put(channelId, SessionBuilder())
        message.channel.sendMessage("Created session builder for ${message.channel.mention()}")
    } else {
        message.channel.sendMessage("A session is already being built in ${message.channel.mention()}")
    }
}

val JOIN_SESSION = Command("join", "join [gender]") { message, args ->
    val channelId = message.channel.id
    val builder = HGKDiscord.sessionBuilders[channelId]
    if (builder != null) {
        val pronoun = when (args.getOrNull(0)?.toLowerCase(Locale.ENGLISH)) {
            "male" -> Pronoun.MALE
            "female" -> Pronoun.FEMALE
            else -> Pronoun.NEUTRAL
        }
        builder.players.add(PlayerInfoModel(message.author.name, pronoun))
        message.channel.sendMessage("${message.author.mention()} joined session in ${message.channel.mention()}")
    } else {
        message.channel.sendMessage("No session currently being built in ${message.channel.mention()}")
    }
}

val START = Command("start") { message, args ->
    val channelId = message.channel.id
    val builder = HGKDiscord.sessionBuilders[channelId]
    if (builder != null) {
        val session = HGKDiscord.hgk.createSession(channelId, builder.players)
        HGKDiscord.sessionBuilders.remove(channelId)
        HGKDiscord.activeSessions.add(session)
        message.channel.sendMessage("${message.channel.mention()} game has begun!")
    } else {
        message.channel.sendMessage("No session built in ${message.channel.mention()}")
    }
}

val STOP = Command("stop") { message, args ->
    val channelId = message.channel.id
    val session = HGKDiscord.activeSessions.filter { it.channelId == channelId }.firstOrNull()
    if (session != null) {
        session.cancel = true
        message.channel.sendMessage("${message.channel.mention()} game has been canceled.")
    } else {
        message.channel.sendMessage("No session active in ${message.channel.mention()}")
    }
}

val COMMANDS = arrayOf(BUILD_GAME, JOIN_SESSION, START, STOP)

class Command(val name: String, val usage: String = name, val handle: (message: IMessage, args: Array<String>) -> Unit)
