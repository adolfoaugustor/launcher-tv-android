package br.tematv.themebuilder

import br.tematv.themecore.ThemeError
import br.tematv.themecore.ThemeLoadResult
import br.tematv.themecore.ThemePackageLoader
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Serializable
private data class PackageVerse(val reference: String, val text: String, val version: String)

@Serializable
private data class PackageIconFrame(
    val image: String,
    @SerialName("icon_slot") val iconSlot: Slot,
    @SerialName("label_slot") val labelSlot: Slot,
)

/** `theme.json` v1 (`_reversa_forward/001-theme-engine/data-delta.md` §1.1). */
@Serializable
private data class PackageManifest(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("theme_id") val themeId: String,
    @SerialName("display_name") val displayName: String,
    val colors: Colors,
    @SerialName("background_image") val backgroundImage: String?,
    @SerialName("logo_image") val logoImage: String,
    val message: String,
    @SerialName("icon_frame") val iconFrame: PackageIconFrame,
    val verses: List<PackageVerse>,
    @SerialName("verses_attribution") val versesAttribution: String,
)

data class BuiltPackage(val file: File, val sizeBytes: Long, val adbCommands: List<String>)

/** O launcher recusaria o pacote; nada foi salvo em `temas/`. */
class PackageRejectedException(val error: ThemeError) :
    Exception("O pacote não passou na conferência do launcher: $error")

/**
 * Monta o `.tematv` num arquivo temporário, valida com o mesmo `ThemePackageLoader` do launcher
 * e só então o renomeia para `<theme_id>.tematv` (D-05, RN-01, RF-08).
 */
class PackageBuilder(
    private val themesDir: File,
    private val presets: Presets,
    private val loader: ThemePackageLoader = ThemePackageLoader(),
) {

    fun build(project: ClientProject, projectDir: File): BuiltPackage {
        val logoFile = project.logoFile?.let { File(projectDir, it) }?.takeIf { it.isFile }
            ?: throw UserInputException("logo", "Envie o logotipo (PNG) antes de gerar o pacote.")
        if (project.verses.isEmpty()) {
            throw UserInputException("verses", "Acrescente pelo menos um item à lista antes de gerar o pacote.")
        }
        val frame = frameEntry(project, projectDir)
        val backgroundFile = project.backgroundFile?.let { File(projectDir, it) }?.takeIf { it.isFile }
        val manifest = PackageManifest(
            schemaVersion = 1,
            themeId = project.themeId,
            displayName = project.displayName,
            colors = project.colors,
            backgroundImage = backgroundFile?.name,
            logoImage = logoFile.name,
            message = project.message,
            iconFrame = PackageIconFrame(frame.name, frame.iconSlot, frame.labelSlot),
            verses = project.verses.map { PackageVerse(it.reference, it.text, versionFor(project.type)) },
            versesAttribution = attributionFor(project),
        )

        themesDir.mkdirs()
        val temporary = File.createTempFile(".${project.themeId}-", ".tmp", themesDir)
        val extractionDir = Files.createTempDirectory("tematv-conferencia-").toFile()
        try {
            ZipOutputStream(temporary.outputStream().buffered()).use { zip ->
                zip.add(MANIFEST_FILE_NAME, ProjectStore.json.encodeToString(PackageManifest.serializer(), manifest).toByteArray())
                zip.add(logoFile.name, logoFile.readBytes())
                backgroundFile?.let { zip.add(it.name, it.readBytes()) }
                zip.add(frame.name, frame.bytes)
            }
            when (val result = loader.load(temporary, extractionDir)) {
                is ThemeLoadResult.Rejected -> throw PackageRejectedException(result.error)
                is ThemeLoadResult.Loaded -> Unit
            }
            val target = File(themesDir, "${project.themeId}.tematv")
            Files.move(temporary.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            return BuiltPackage(target, target.length(), adbCommands(target))
        } finally {
            temporary.delete()
            extractionDir.deleteRecursively()
        }
    }

    private class FrameEntry(val name: String, val bytes: ByteArray, val iconSlot: Slot, val labelSlot: Slot)

    private fun frameEntry(project: ClientProject, projectDir: File): FrameEntry {
        val choice = project.iconFrame
        if (choice.preset != null) {
            val preset = presets.frame(choice.preset)
            return FrameEntry(preset.imageName, preset.imageBytes, preset.iconSlot, preset.labelSlot)
        }
        val image = File(projectDir, CUSTOM_FRAME_FILE_NAME).takeIf { it.isFile }
            ?: throw UserInputException("moldura", "Envie a imagem da moldura própria (PNG) ou escolha uma moldura pronta.")
        val iconSlot = choice.iconSlot ?: throw UserInputException("icon_frame.icon_slot", "Informe a área do ícone.")
        val labelSlot = choice.labelSlot ?: throw UserInputException("icon_frame.label_slot", "Informe a área do nome.")
        return FrameEntry(image.name, image.readBytes(), iconSlot, labelSlot)
    }

    private fun versionFor(type: ClientType): String = when (type) {
        ClientType.IGREJA -> "NVI"
        ClientType.LOJA -> "LOJA"
    }

    private fun attributionFor(project: ClientProject): String = when (project.type) {
        ClientType.IGREJA -> presets.nviAttribution
        ClientType.LOJA -> project.versesAttribution.orEmpty()
    }

    private fun ZipOutputStream.add(name: String, bytes: ByteArray) {
        putNextEntry(ZipEntry(name))
        write(bytes)
        closeEntry()
    }

    companion object {
        const val MANIFEST_FILE_NAME = "theme.json"
        const val CUSTOM_FRAME_FILE_NAME = "moldura.png"
        private const val IMPORT_URI = "content://br.tematv.launcher.themes"

        /** Comandos da importação pelo ADB (`_reversa_forward/001-theme-engine/interfaces/importacao-adb.md`). */
        fun adbCommands(packageFile: File): List<String> = listOf(
            "adb connect <IP-DA-TV>:5555",
            "adb -s <IP-DA-TV>:5555 shell \"content write --uri $IMPORT_URI/${packageFile.name}\" < \"${packageFile.absolutePath}\"",
            "echo ${packageFile.name} | adb -s <IP-DA-TV>:5555 shell \"content write --uri $IMPORT_URI/active_theme.txt\"",
            "adb -s <IP-DA-TV>:5555 shell am broadcast -f 32 -a br.tematv.launcher.action.RELOAD_THEME -n br.tematv.launcher/.theme.ReloadThemeReceiver",
        )
    }
}
