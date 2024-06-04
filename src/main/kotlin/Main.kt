package com.example

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.time.LocalDateTime
import kotlin.math.pow
import java.time.Duration
import java.util.*
import kotlin.time.*

class Bot: TelegramLongPollingBot() {
    private var latitude: Double = .0
    private var longitude: Double = .0
    private var timeOfPreviousPosition: LocalDateTime? = null


    override fun getBotUsername() = ResourceBundle.getBundle("bot").getString("bot.username")
    override fun getBotToken() = ResourceBundle.getBundle("bot").getString("bot.token")

    private var l: Double = .0


    override fun onUpdateReceived(update: Update) {
        if (!update.hasMessage()) return

        val message = update.message
        val sendMessage = SendMessage().also { it.setChatId(message.chatId)}

        when {
            message.hasText() -> sendMessage.text = when(message.text) {
                "/start" -> "Welcome to my Bot"
                else -> "Unknown command"
            }

            message.hasVoice() -> sendMessage.text = "Your voice message duration: ${message.voice.duration}"

            message.hasLocation() -> {
                val location = message.location

                timeOfPreviousPosition?.let {
                    sendMessage.text = "Your position changed on: ${getDistance(location.latitude, location.longitude, latitude, longitude)} for ${getTime()}"
                    execute(sendMessage)
                }

                latitude = location.latitude
                longitude = location.longitude
                timeOfPreviousPosition = LocalDateTime.now()

                sendMessage.text = "Your current position: ${getCoordinates()}"
                // execute(sendMessage)
            }
        }

        if (sendMessage.text.isNotEmpty()) execute(sendMessage)
    }

    private fun getCoordinates(x: Double = latitude, y: Double = longitude): String = "($x; $y)"

    private fun getDistance( x: Double, y: Double, x_pre: Double, y_pre: Double): Double = ((x - x_pre).pow(2) + (y - y_pre).pow(2)).pow(.5)

    private fun getTime(): String = Duration
        .between(timeOfPreviousPosition, LocalDateTime.now())
        .toKotlinDuration()
        .toComponents { hours, minutes, seconds, _ -> "${hours}h:${minutes}m:${seconds}s" }
}

fun main() {
    val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
    telegramBotsApi.registerBot(Bot())
}