package net.gegy1000.hgk

import net.gegy1000.hgk.session.GameSession
import net.gegy1000.hgk.session.SessionBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.MessageReceivedEvent
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.kotlin.Discord4K
import sx.blah.discord.util.RateLimitException
import java.util.concurrent.LinkedBlockingDeque
import kotlin.concurrent.fixedRateTimer

object HGKDiscord {
    val hgk: HGK = HGKWebAPI

    val logger: Logger = LoggerFactory.getLogger(HGKDiscord::class.java)

    val activeSessions = HashSet<GameSession>()
    val sessionBuilders = HashMap<String, SessionBuilder>()

    val updateQueue = LinkedBlockingDeque<StatusUpdate>()

    @JvmStatic
    fun main(args: Array<String>) {
        val client = ClientBuilder()
                .withToken(args.getOrNull(0) ?: throw IllegalArgumentException("missing token argument"))
                .build()

        val discord4K = Discord4K()
        discord4K.enable(client)

        client.dispatcher.registerListener(this)

        client.login()

        fixedRateTimer(name = "Message Update", daemon = true, initialDelay = 1000, period = 1000) {
            while (updateQueue.isNotEmpty()) {
                val update = updateQueue.element()
                try {
                    client.getChannelByID(update.channelId)?.sendMessage(update.message)
                    updateQueue.poll()
                } catch (e: RateLimitException) {
                    Thread.sleep(e.retryDelay)
                }
            }
        }
    }

    @EventSubscriber
    fun onReady(event: ReadyEvent) {
        logger.info("HGK bot ready")
    }

    @EventSubscriber
    fun onMessage(event: MessageReceivedEvent) {
        val message = event.message
        if (!message.author.isBot) {
            if (message.content.startsWith(COMMAND_PREFIX)) {
                val arguments = message.content.substring(1).split(" ").toMutableList()
                val name = arguments.removeAt(0)
                COMMANDS.filter { name == it.name }.firstOrNull()?.let { command ->
                    try {
                        command.handle(message, arguments.toTypedArray())
                    } catch (e: Exception) {
                        if (e.message != null) {
                            message.channel.sendMessage("${e::class.qualifiedName}: ${e.message}")
                        } else {
                            message.channel.sendMessage(e::class.qualifiedName)
                        }
                        logger.error("Failed to run command $name with arguments: $arguments", e)
                    }
                }
            }
        }
    }

    class StatusUpdate(val channelId: String, val message: String)
}
