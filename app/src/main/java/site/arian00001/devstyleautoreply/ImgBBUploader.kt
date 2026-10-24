package site.arian00001.devstyleautoreply

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Uploads an existing public image URL to ImgBB and returns the hosted image URL. */
object ImgBBUploader {
    fun uploadImageUrl(apiKey: String, imageUrl: String): String {
        require(apiKey.isNotBlank()) { "Enter your ImgBB API key in Settings." }
        require(imageUrl.startsWith("https://") || imageUrl.startsWith("http://")) { "Enter a valid public image URL." }
        val body = "key=${enc(apiKey)}&image=${enc(imageUrl)}".toByteArray(Charsets.UTF_8)
        val connection = (URL("https://api.imgbb.com/1/upload").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 20000
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
        }
        try {
            connection.outputStream.use { it.write(body) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val json = JSONObject(response)
            if (code !in 200..299 || !json.optBoolean("success", false)) {
                throw IllegalStateException(json.optJSONObject("error")?.optString("message") ?: "ImgBB upload failed (HTTP $code).")
            }
            return json.getJSONObject("data").getString("url")
        } finally {
            connection.disconnect()
        }
    }
    private fun enc(value: String) = URLEncoder.encode(value, "UTF-8")
}
