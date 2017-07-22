package net.gegy1000.hgk

import com.google.gson.Gson
import net.gegy1000.hgk.model.PlayerInfoModel
import net.gegy1000.hgk.model.SessionCreationModel
import net.gegy1000.hgk.model.SessionDataModel
import net.gegy1000.hgk.model.SessionSetupModel
import net.gegy1000.hgk.session.GameSession
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException
import java.util.Random

object HGKWebAPI : HGK {
    const val ENDPOINT = "http://localhost:8080/v1"

    val JSON = MediaType.parse("application/json")

    val client: OkHttpClient = OkHttpClient.Builder().addInterceptor {
        it.proceed(it.request().newBuilder().header("User-Agent", "hgk-discord").header("Accept", "*/*").build())
    }.build()

    val gson = Gson()

    @Throws(RequestErrorException::class)
    override fun createSession(channelId: String, players: List<PlayerInfoModel>): GameSession {
        val body = SessionCreationModel(players.toTypedArray(), Random().nextLong())
        val request = Request.Builder()
                .url("$ENDPOINT/create")
                .post(RequestBody.create(JSON, gson.toJson(body).toByteArray(Charsets.UTF_8)))
                .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body().string()
                val data = gson.fromJson(responseBody, SessionDataModel::class.java)
                return GameSession(channelId, getSetupInfo(data.identifier))
            }
        } catch (e: IOException) {
            throw RequestErrorException(cause = e)
        }
    }

    @Throws(RequestErrorException::class)
    private fun getSetupInfo(session: String): SessionSetupModel {
        val request = Request.Builder()
                .url("$ENDPOINT/session/$session")
                .get()
                .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body().string()
                return gson.fromJson(body, SessionSetupModel::class.java)
            }
        } catch (e: IOException) {
            throw RequestErrorException(cause = e)
        }
    }

    @Throws(RequestErrorException::class)
    override fun getSessionUpdate(identifier: String, updateIndex: Int, maxSnapshots: Int): SessionDataModel {
        val request = Request.Builder()
                .url("$ENDPOINT/snapshot/$identifier/$updateIndex?maxSnapshots=$maxSnapshots")
                .get()
                .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body().string()
                return gson.fromJson(body, SessionDataModel::class.java)
            }
        } catch (e: IOException) {
            throw RequestErrorException(cause = e)
        }
    }
}
