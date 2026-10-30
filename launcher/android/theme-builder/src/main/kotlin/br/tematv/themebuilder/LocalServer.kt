package br.tematv.themebuilder

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import java.io.File
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.Base64

@Serializable
private data class ErrorBody(val error: String, val field: String? = null, val code: String? = null)

@Serializable
private data class FramePresetBody(
    val id: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("icon_slot") val iconSlot: Slot,
    @SerialName("label_slot") val labelSlot: Slot,
    @SerialName("image_url") val imageUrl: String,
)

@Serializable
private data class PresetsBody(
    val frames: List<FramePresetBody>,
    @SerialName("nvi_attribution") val nviAttribution: String,
)

@Serializable
private data class ClientSummary(
    @SerialName("theme_id") val themeId: String,
    @SerialName("display_name") val displayName: String,
    val type: ClientType,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("package_exists") val packageExists: Boolean,
)

@Serializable
private data class CreateClientBody(
    val type: ClientType,
    @SerialName("display_name") val displayName: String,
    @SerialName("theme_id") val themeId: String? = null,
)

@Serializable
private data class ImageUploadBody(@SerialName("file_name") val fileName: String, val base64: String)

@Serializable
private data class ImageSavedBody(val file: String)

@Serializable
private data class PackageBody(
    val path: String,
    @SerialName("size_bytes") val sizeBytes: Long,
    @SerialName("adb_commands") val adbCommands: List<String>,
)

@Serializable
private data class ActivationStatusBody(
    @SerialName("secret_available") val secretAvailable: Boolean,
    @SerialName("secret_path") val secretPath: String,
)

@Serializable
private data class ActivationRequestBody(@SerialName("device_code") val deviceCode: String, val customer: String = "")

@Serializable
private data class ActivationBody(
    @SerialName("device_code") val deviceCode: String,
    @SerialName("activation_code") val activationCode: String,
)

/** Página e API local (`_reversa_forward/005-theme-builder/interfaces/api-local.md`), só em `127.0.0.1` (D-02 a D-04). */
class LocalServer(
    private val store: ProjectStore,
    private val images: ImageProcessor,
    private val builder: PackageBuilder,
    private val activation: ResellerActivation,
    private val presets: Presets,
    private val themesDir: File,
) {

    /** Abre a porta em `127.0.0.1`; lança `java.net.BindException` se estiver ocupada. */
    fun start(port: Int): HttpServer {
        val guard = LocalRequestGuard(port)
        val server = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0)
        server.createContext("/") { exchange -> handle(exchange, guard) }
        server.start()
        return server
    }

    private fun handle(exchange: HttpExchange, guard: LocalRequestGuard) {
        exchange.use {
            try {
                val headers = exchange.requestHeaders
                val rejection = guard.rejectionReason(
                    exchange.requestMethod,
                    headers.getFirst("Host"),
                    headers.getFirst("Origin"),
                    headers.getFirst("Content-Type"),
                )
                if (rejection != null) {
                    sendJson(exchange, 403, ErrorBody.serializer(), ErrorBody(rejection))
                } else {
                    route(exchange)
                }
            } catch (exception: UserInputException) {
                sendJson(exchange, exception.httpStatus, ErrorBody.serializer(), ErrorBody(exception.message.orEmpty(), exception.field))
            } catch (exception: PackageRejectedException) {
                val error = exception.error
                val body = ErrorBody(
                    "O launcher recusaria este pacote (${listOfNotNull(error.code.name, error.field, error.detail).joinToString(" ")}). Confira o campo indicado.",
                    error.field?.let(::formFieldFor),
                    error.code.name,
                )
                sendJson(exchange, 422, ErrorBody.serializer(), body)
            } catch (exception: SerializationException) {
                sendJson(exchange, 400, ErrorBody.serializer(), ErrorBody("Dados inválidos: ${exception.message}"))
            } catch (exception: IllegalArgumentException) {
                sendJson(exchange, 400, ErrorBody.serializer(), ErrorBody("Dados inválidos: ${exception.message}"))
            } catch (exception: Exception) {
                System.err.println("Erro em ${exception.javaClass.simpleName} ao atender ${exchange.requestMethod} ${exchange.requestURI.path}")
                exception.printStackTrace()
                sendJson(exchange, 500, ErrorBody.serializer(), ErrorBody("Erro inesperado na ferramenta: ${exception.message}"))
            }
        }
    }

    private fun route(exchange: HttpExchange) {
        val method = exchange.requestMethod
        val segments = exchange.requestURI.path.trim('/').split('/').filter { it.isNotEmpty() }
        when {
            segments.firstOrNull() != "api" -> serveStatic(exchange, segments)
            segments == listOf("api", "presets") && method == "GET" ->
                sendJson(exchange, 200, PresetsBody.serializer(), presetsBody())
            segments.size == 4 && segments[1] == "presets" && segments[3] == "moldura" && method == "GET" ->
                sendBytes(exchange, 200, "image/png", presets.frame(segments[2]).imageBytes)
            segments == listOf("api", "clientes") && method == "GET" ->
                sendJson(exchange, 200, ListSerializer(ClientSummary.serializer()), store.list().map(::summary))
            segments == listOf("api", "clientes") && method == "POST" -> {
                val body = readJson(exchange, CreateClientBody.serializer())
                sendJson(exchange, 201, ClientProject.serializer(), store.create(body.type, body.displayName, body.themeId))
            }
            segments.size >= 3 && segments[1] == "clientes" -> routeClient(exchange, method, segments[2], segments.drop(3))
            segments == listOf("api", "ativacao") && method == "GET" ->
                sendJson(
                    exchange, 200, ActivationStatusBody.serializer(),
                    ActivationStatusBody(activation.isSecretAvailable, activation.secretFile.path),
                )
            segments == listOf("api", "ativacao") && method == "POST" -> {
                val body = readJson(exchange, ActivationRequestBody.serializer())
                val generated = activation.generate(body.deviceCode, body.customer)
                sendJson(exchange, 200, ActivationBody.serializer(), ActivationBody(generated.deviceCode, generated.activationCode))
            }
            else -> throw UserInputException(null, "Caminho não encontrado: $method ${exchange.requestURI.path}", 404)
        }
    }

    private fun routeClient(exchange: HttpExchange, method: String, themeId: String, rest: List<String>) {
        when {
            rest.isEmpty() && method == "GET" -> sendJson(exchange, 200, ClientProject.serializer(), store.get(themeId))
            rest.isEmpty() && method == "PUT" -> {
                val edited = readJson(exchange, ClientProject.serializer())
                sendJson(exchange, 200, ClientProject.serializer(), store.save(themeId, edited))
            }
            rest.size == 2 && rest[0] == "imagens" -> routeImage(exchange, method, themeId, imageKind(rest[1]))
            rest == listOf("pacote") && method == "POST" -> {
                val built = builder.build(store.get(themeId), store.projectDir(themeId))
                println("Pacote gerado: ${built.file} (${built.sizeBytes} bytes)")
                sendJson(exchange, 200, PackageBody.serializer(), PackageBody(built.file.path, built.sizeBytes, built.adbCommands))
            }
            else -> throw UserInputException(null, "Caminho não encontrado: $method ${exchange.requestURI.path}", 404)
        }
    }

    private fun routeImage(exchange: HttpExchange, method: String, themeId: String, kind: ImageKind) {
        val project = store.get(themeId)
        val projectDir = store.projectDir(themeId)
        when (method) {
            "PUT" -> {
                val upload = readJson(exchange, ImageUploadBody.serializer())
                val fileName = images.save(kind, Base64.getDecoder().decode(upload.base64), projectDir)
                store.updateImage(themeId, kind, fileName)
                sendJson(exchange, 200, ImageSavedBody.serializer(), ImageSavedBody(fileName))
            }
            "GET" -> {
                val file = imageFile(project, kind, projectDir)
                    ?: throw UserInputException(kind.field, "Imagem ainda não enviada.", 404)
                sendBytes(exchange, 200, contentTypeFor(file.name), file.readBytes())
            }
            "DELETE" -> {
                if (kind != ImageKind.BACKGROUND) throw UserInputException(kind.field, "Só o fundo pode ser removido.", 405)
                imageFile(project, kind, projectDir)?.delete()
                store.updateImage(themeId, kind, null)
                exchange.sendResponseHeaders(204, -1)
            }
            else -> throw UserInputException(null, "Método não permitido.", 405)
        }
    }

    private fun imageFile(project: ClientProject, kind: ImageKind, projectDir: File): File? = when (kind) {
        ImageKind.LOGO -> project.logoFile
        ImageKind.BACKGROUND -> project.backgroundFile
        ImageKind.FRAME -> PackageBuilder.CUSTOM_FRAME_FILE_NAME
    }?.let { File(projectDir, it) }?.takeIf { it.isFile }

    private fun imageKind(segment: String): ImageKind =
        ImageKind.entries.firstOrNull { it.field == segment } ?: throw UserInputException(null, "Imagem desconhecida: $segment", 404)

    private fun serveStatic(exchange: HttpExchange, segments: List<String>) {
        val name = if (segments.isEmpty()) "index.html" else segments.single()
        val bytes = name.takeIf { staticNamePattern.matches(it) }
            ?.let { LocalServer::class.java.getResourceAsStream("/web/$it")?.use { stream -> stream.readBytes() } }
            ?: throw UserInputException(null, "Página não encontrada: ${exchange.requestURI.path}", 404)
        sendBytes(exchange, 200, contentTypeFor(name), bytes)
    }

    private fun presetsBody() = PresetsBody(
        frames = presets.frames.values.map {
            FramePresetBody(it.id, it.displayName, it.iconSlot, it.labelSlot, "/api/presets/${it.id}/moldura")
        },
        nviAttribution = presets.nviAttribution,
    )

    private fun summary(project: ClientProject) = ClientSummary(
        project.themeId,
        project.displayName,
        project.type,
        project.updatedAt,
        File(themesDir, "${project.themeId}.tematv").isFile,
    )

    /** Campo do `theme.json` recusado → campo do formulário da página. */
    private fun formFieldFor(themeField: String): String = when (themeField) {
        "logo_image" -> "logo"
        "background_image" -> "fundo"
        "icon_frame.image" -> "moldura"
        else -> themeField
    }

    private fun <T> readJson(exchange: HttpExchange, serializer: KSerializer<T>): T {
        val bytes = exchange.requestBody.readNBytes(MAX_REQUEST_BYTES + 1)
        if (bytes.size > MAX_REQUEST_BYTES) throw UserInputException(null, "Envio maior que 30 MB.", 413)
        return ProjectStore.json.decodeFromString(serializer, bytes.decodeToString())
    }

    private fun <T> sendJson(exchange: HttpExchange, status: Int, serializer: KSerializer<T>, value: T) =
        sendBytes(exchange, status, "application/json; charset=utf-8", ProjectStore.json.encodeToString(serializer, value).toByteArray())

    private fun sendBytes(exchange: HttpExchange, status: Int, contentType: String, bytes: ByteArray) {
        exchange.responseHeaders.apply {
            set("Content-Type", contentType)
            set("Cache-Control", "no-store")
            set("X-Content-Type-Options", "nosniff")
        }
        exchange.sendResponseHeaders(status, bytes.size.toLong().takeIf { it > 0 } ?: -1)
        if (bytes.isNotEmpty()) exchange.responseBody.write(bytes)
    }

    private fun contentTypeFor(name: String): String = when (name.substringAfterLast('.').lowercase()) {
        "html" -> "text/html; charset=utf-8"
        "css" -> "text/css; charset=utf-8"
        "js" -> "text/javascript; charset=utf-8"
        "png" -> "image/png"
        "jpg" -> "image/jpeg"
        "webp" -> "image/webp"
        else -> "application/octet-stream"
    }

    private companion object {
        const val MAX_REQUEST_BYTES = 30 * 1024 * 1024
        val staticNamePattern = Regex("^[a-z]+\\.(html|css|js)$")
    }
}
