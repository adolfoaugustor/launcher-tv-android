package br.tematv.themebuilder

import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.Normalizer
import java.time.Clock
import java.time.Instant

/** Projetos de cliente em `<clientsDir>/<theme_id>/` (D-09, RF-02, RF-03, RF-05, RF-11). */
class ProjectStore(
    private val clientsDir: File,
    private val presets: Presets,
    private val clock: Clock = Clock.systemUTC(),
) {

    fun list(): List<ClientProject> =
        clientsDir.listFiles { file -> File(file, PROJECT_FILE_NAME).isFile }.orEmpty()
            .map { read(it.name) }
            .sortedBy { it.displayName.lowercase() }

    fun exists(themeId: String): Boolean = File(projectDir(themeId), PROJECT_FILE_NAME).isFile

    fun get(themeId: String): ClientProject {
        if (!themeIdPattern.matches(themeId) || !exists(themeId)) {
            throw UserInputException(null, "Cliente não encontrado: $themeId", 404)
        }
        return read(themeId)
    }

    fun create(type: ClientType, displayName: String, requestedThemeId: String?): ClientProject {
        val themeId = requestedThemeId?.takeIf { it.isNotBlank() } ?: suggestThemeId(displayName)
        val name = displayName.trim()
        val project = ClientProject(
            type = type,
            themeId = themeId,
            displayName = name,
            colors = if (type == ClientType.IGREJA) presets.churchColors else presets.storeColors,
            message = DEFAULT_MESSAGE,
            iconFrame = IconFrameChoice(preset = presets.suggestedFrameId(type)),
            verses = when (type) {
                ClientType.IGREJA -> presets.nviVerses
                ClientType.LOJA -> listOf(ProjectVerse("Aviso 1", "Escreva aqui uma mensagem da loja."))
            },
            versesAttribution = if (type == ClientType.LOJA) "Mensagens da $name".take(MAX_ATTRIBUTION_LENGTH) else null,
            updatedAt = now(),
        )
        validate(project)
        if (exists(themeId)) {
            throw UserInputException("theme_id", "Já existe um cliente com o identificador \"$themeId\". Escolha outro ou abra o existente.", 409)
        }
        projectDir(themeId).mkdirs()
        write(project)
        return project
    }

    /** Grava os campos editáveis; tipo, identificador e imagens continuam os do projeto salvo. */
    fun save(themeId: String, edited: ClientProject): ClientProject {
        val current = get(themeId)
        val project = edited.copy(
            schemaVersion = current.schemaVersion,
            type = current.type,
            themeId = current.themeId,
            displayName = edited.displayName.trim(),
            logoFile = current.logoFile,
            backgroundFile = current.backgroundFile,
            versesAttribution = if (current.type == ClientType.LOJA) edited.versesAttribution?.trim() else null,
            updatedAt = now(),
        )
        validate(project)
        write(project)
        return project
    }

    fun updateImage(themeId: String, kind: ImageKind, fileName: String?): ClientProject {
        val current = get(themeId)
        val project = when (kind) {
            ImageKind.LOGO -> current.copy(logoFile = fileName)
            ImageKind.BACKGROUND -> current.copy(backgroundFile = fileName)
            ImageKind.FRAME -> current
        }.copy(updatedAt = now())
        write(project)
        return project
    }

    fun projectDir(themeId: String): File = File(clientsDir, themeId)

    private fun validate(project: ClientProject) {
        if (!themeIdPattern.matches(project.themeId) || project.themeId.length !in 3..40) {
            throw UserInputException("theme_id", "Identificador com 3 a 40 letras minúsculas, números e hífens (ex.: igreja-batista-x).")
        }
        requireLength("display_name", "O nome do cliente", project.displayName, 1..60)
        listOf("background" to project.colors.background, "text" to project.colors.text, "accent" to project.colors.accent)
            .forEach { (key, value) ->
                if (!colorPattern.matches(value)) throw UserInputException("colors.$key", "Cor no formato #RRGGBB.")
            }
        requireLength("message", "A mensagem", project.message, 1..MAX_MESSAGE_LENGTH)
        validateFrame(project.iconFrame)
        if (project.verses.size > MAX_VERSES) {
            throw UserInputException("verses", "A lista aceita no máximo $MAX_VERSES itens.")
        }
        project.verses.forEachIndexed { index, verse ->
            requireLength("verses[$index].reference", "A referência do item ${index + 1}", verse.reference, 1..40)
            requireLength("verses[$index].text", "O texto do item ${index + 1}", verse.text, 1..600)
        }
        if (project.type == ClientType.LOJA) {
            requireLength("verses_attribution", "A atribuição", project.versesAttribution.orEmpty(), 1..MAX_ATTRIBUTION_LENGTH)
        }
    }

    private fun validateFrame(frame: IconFrameChoice) {
        if (frame.preset != null) {
            presets.frame(frame.preset)
            return
        }
        listOf("icon_slot" to frame.iconSlot, "label_slot" to frame.labelSlot).forEach { (key, slot) ->
            val valid = slot != null && slot.w > 0 && slot.h > 0 && slot.x >= 0 && slot.y >= 0 &&
                slot.x + slot.w <= 1 + SLOT_TOLERANCE && slot.y + slot.h <= 1 + SLOT_TOLERANCE
            if (!valid) throw UserInputException("icon_frame.$key", "Área com valores de 0 a 1, dentro da moldura.")
        }
    }

    private fun requireLength(field: String, label: String, value: String, range: IntRange) {
        if (value.isBlank() || value.length !in range) {
            throw UserInputException(field, "$label deve ter de ${range.first} a ${range.last} caracteres.")
        }
    }

    private fun read(themeId: String): ClientProject =
        json.decodeFromString(ClientProject.serializer(), File(projectDir(themeId), PROJECT_FILE_NAME).readText())

    private fun write(project: ClientProject) {
        val target = File(projectDir(project.themeId), PROJECT_FILE_NAME)
        val temporary = File(target.parentFile, "$PROJECT_FILE_NAME.tmp")
        temporary.writeText(json.encodeToString(ClientProject.serializer(), project))
        Files.move(temporary.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    }

    private fun now(): String = Instant.now(clock).toString()

    companion object {
        const val PROJECT_FILE_NAME = "projeto.json"
        const val MAX_MESSAGE_LENGTH = 120
        const val MAX_VERSES = 366
        const val MAX_ATTRIBUTION_LENGTH = 120
        private const val DEFAULT_MESSAGE = "Seja bem-vindo!"
        private const val SLOT_TOLERANCE = 1e-9
        private val themeIdPattern = Regex("^[a-z0-9]+(-[a-z0-9]+)*$")
        private val colorPattern = Regex("^#[0-9A-Fa-f]{6}$")
        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
        }

        /** `Igreja Batista Ágape` → `igreja-batista-agape` (até 40 caracteres). */
        fun suggestThemeId(displayName: String): String =
            Normalizer.normalize(displayName.lowercase(), Normalizer.Form.NFD)
                .replace(Regex("\\p{M}"), "")
                .replace(Regex("[^a-z0-9]+"), "-")
                .trim('-')
                .take(40)
                .trimEnd('-')
    }
}
