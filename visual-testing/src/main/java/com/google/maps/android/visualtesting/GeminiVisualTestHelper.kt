package com.google.maps.android.visualtesting

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

/**
 * Helper class to interact with the Gemini API for visual verification and action.
 *
 * To use this class, you need a Gemini API key. You can get one from Google AI Studio:
 * https://makersuite.google.com/app/apikey
 */
class GeminiVisualTestHelper {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    /**
     * Executes a UI action based on a natural language prompt.
     * It analyzes the current UI hierarchy and asks Gemini to determine the best action.
     */
    suspend fun performActionFromPrompt(prompt: String, uiDevice: UiDevice, apiKey: String) {
        val hierarchyStream = ByteArrayOutputStream()
        uiDevice.dumpWindowHierarchy(hierarchyStream)
        val hierarchyXml = hierarchyStream.toString("UTF-8")

        val systemPrompt = """
            You are an expert Android QA automaton. Your task is to translate a natural language command 
            into a specific action to be performed on a UI. Given a UI hierarchy (in XML format), 
            determine the correct action and selector.

            The available actions are: "click", "longClick", "setText".

            Your response MUST be a single, well-formed JSON object with "action" and "selector" keys.
            The "selector" object must contain exactly one of "text", "contentDescription", or "resourceId".
            If the action is "setText", you must also include a "textValue" field at the top level.

            Example for a click:
            { "action": "click", "selector": { "text": "Login" } }

            Example for setting text:
            { "action": "setText", "selector": { "resourceId": "com.example.app:id/email_input" }, "textValue": "test@example.com" }
        """.trimIndent()

        val fullPrompt = "$systemPrompt\n\nCommand: \"$prompt\"\n\nUI Hierarchy:\n$hierarchyXml"

        // Use a simpler text-only model for this task as no image is involved.
        // The user mentioned "gemini-flash 2.5" works, but we should use a standard name.
        val modelName = "gemini-2.5-flash"
        val request = GeminiRequest(contents = listOf(Content(parts = listOf(Part(text = fullPrompt)))))

        val response: HttpResponse = client.post("https://generativelanguage.googleapis.com/v1/models/$modelName:generateContent?key=$apiKey") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        if (response.status != HttpStatusCode.OK) {
            val errorBody = response.bodyAsText()
            Log.e("GeminiVisualTestHelper", "Action API Error: ${response.status} $errorBody")
            throw Exception("Gemini Action API returned an error: ${response.status}\n$errorBody")
        }

        val geminiResponse: GeminiResponse = response.body()
        val actionJson = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Gemini returned no action JSON.")

        // Remove markdown code block delimiters if present
        val cleanedActionJson = actionJson.removePrefix("```json\n").removeSuffix("\n```")

        Log.d("GeminiVisualTestHelper", "Received Action JSON: $cleanedActionJson")

        try {
            val aiAction = json.decodeFromString<AiAction>(cleanedActionJson)
            val selector = aiAction.selector.let {
                when {
                    it.text != null -> By.text(it.text)
                    it.contentDescription != null -> By.desc(it.contentDescription)
                    it.resourceId != null -> By.res(it.resourceId)
                    else -> throw IllegalArgumentException("Selector must have text, contentDescription, or resourceId.")
                }
            }

            val uiObject = uiDevice.wait(Until.findObject(selector), 10000)
                ?: throw Exception("Could not find UI element for selector: $selector")

            when (aiAction.action.lowercase()) {
                "click" -> uiObject.click()
                "longclick" -> uiObject.longClick()
                "settext" -> {
                    val textToSet = aiAction.textValue ?: throw Exception("Action 'setText' requires a 'textValue' field.")
                    uiObject.text = textToSet
                }
                else -> throw UnsupportedOperationException("Action '${aiAction.action}' is not supported.")
            }
        } catch (e: Exception) {
            Log.e("GeminiVisualTestHelper", "Failed to parse or execute AI action", e)
            throw e // Re-throw to fail the test
        }
    }


    /**
     * Fetches and logs the list of available Gemini models for the given API key.
     */
    suspend fun listAvailableModels(apiKey: String) {
        try {
            val response: ModelsListResponse = client.get("https://generativelanguage.googleapis.com/v1/models?key=$apiKey").body()
            val modelNames = response.models.joinToString("\n") { " - ${it.name} (Display Name: ${it.displayName})" }
            Log.i("GeminiVisualTestHelper", "Available Gemini Models:\n$modelNames")
        } catch (e: Exception) {
            Log.e("GeminiVisualTestHelper", "Failed to list available models", e)
        }
    }

    /**
     * Analyzes an image with a given prompt using the Gemini API.
     */
    suspend fun analyzeImage(
        bitmap: Bitmap,
        prompt: String,
        apiKey: String
    ): String? {
        // Log available models first for easier debugging.
        listAvailableModels(apiKey)

        val base64Image = bitmap.toBase64EncodedJpeg()
        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(
                            mimeType = "image/jpeg",
                            data = base64Image
                        ))
                    )
                )
            )
        )

        val response: HttpResponse = client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=$apiKey") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        if (response.status != HttpStatusCode.OK) {
            val errorBody = response.bodyAsText()
            Log.e("GeminiVisualTestHelper", "API Error: ${response.status} $errorBody")
            throw Exception("Gemini API returned an error: ${response.status}\n$errorBody")
        }

        val geminiResponse: GeminiResponse = response.body()

        if (geminiResponse.candidates.isEmpty()) {
            val rawBody = response.bodyAsText()
            Log.w("GeminiVisualTestHelper", "Gemini API returned empty candidates. Full response: $rawBody")
            throw Exception("Gemini API returned no candidates. This might be due to safety settings or an issue with the prompt.")
        }

        return geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
    }

    private fun Bitmap.toBase64EncodedJpeg(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}

// --- Data classes for AI Action ---
@Serializable
data class AiAction(
    val action: String,
    val selector: AiSelector,
    val textValue: String? = null
)

@Serializable
data class AiSelector(
    val text: String? = null,
    val contentDescription: String? = null,
    val resourceId: String? = null
)


// --- Data classes for Model Listing ---
@Serializable
data class ModelsListResponse(
    val models: List<ModelInfo> = emptyList()
)

@Serializable
data class ModelInfo(
    val name: String,
    @SerialName("displayName")
    val displayName: String
)

// --- Data classes for Gemini Request/Response ---
@Serializable
data class GeminiRequest(
    val contents: List<Content>
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null,
    @SerialName("inline_data")
    val inlineData: InlineData? = null
)

@Serializable
data class InlineData(
    @SerialName("mime_type")
    val mimeType: String,
    val data: String
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate> = emptyList()
)

@Serializable
data class Candidate(
    val content: Content
)
