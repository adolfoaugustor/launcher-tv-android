package br.tematv.themebuilder

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class FramePreset(
    val id: String,
    val displayName: String,
    val imageName: String,
    val iconSlot: Slot,
    val labelSlot: Slot,
    val imageBytes: ByteArray,
)

/**
 * Molduras prontas, lista NVI e cores iniciais, copiadas na compilação do tema padrão do app
 * e dos temas de exemplo (D-06).
 */
class Presets(
    val frames: Map<String, FramePreset>,
    val nviVerses: List<ProjectVerse>,
    val nviAttribution: String,
    val churchColors: Colors,
    val storeColors: Colors,
) {
    fun frame(presetId: String): FramePreset =
        frames[presetId] ?: throw UserInputException("icon_frame", "Moldura pronta desconhecida: $presetId")

    fun suggestedFrameId(type: ClientType): String = when (type) {
        ClientType.IGREJA -> "biblia"
        ClientType.LOJA -> "hamburguer"
    }

    companion object {
        private val FRAME_NAMES = linkedMapOf("biblia" to "Bíblia", "hamburguer" to "Hambúrguer")

        fun load(): Presets {
            val nviTheme = readJson("nvi.json")
            val frames = FRAME_NAMES.map { (id, displayName) ->
                val theme = readJson("$id.json")
                val iconFrame = theme.getValue("icon_frame").jsonObject
                val imageName = iconFrame.text("image")
                FramePreset(
                    id = id,
                    displayName = displayName,
                    imageName = imageName,
                    iconSlot = iconFrame.slot("icon_slot"),
                    labelSlot = iconFrame.slot("label_slot"),
                    imageBytes = readResource(imageName),
                )
            }
            return Presets(
                frames = frames.associateBy { it.id },
                nviVerses = nviTheme.getValue("verses").jsonArray.map {
                    ProjectVerse(it.jsonObject.text("reference"), it.jsonObject.text("text"))
                },
                nviAttribution = nviTheme.text("verses_attribution"),
                churchColors = nviTheme.colors(),
                storeColors = readJson("hamburguer.json").colors(),
            )
        }

        private fun readResource(name: String): ByteArray =
            Presets::class.java.getResourceAsStream("/presets/$name")?.use { it.readBytes() }
                ?: error("Recurso /presets/$name ausente: rode a compilação do :theme-builder")

        private fun readJson(name: String): JsonObject =
            Json.parseToJsonElement(readResource(name).decodeToString()).jsonObject

        private fun JsonObject.text(key: String): String = getValue(key).jsonPrimitive.content

        private fun JsonObject.slot(key: String): Slot = getValue(key).jsonObject.let {
            Slot(it.number("x"), it.number("y"), it.number("w"), it.number("h"))
        }

        private fun JsonObject.number(key: String): Double = getValue(key).jsonPrimitive.double

        private fun JsonObject.colors(): Colors = getValue("colors").jsonObject.let {
            Colors(it.text("background"), it.text("text"), it.text("accent"))
        }
    }
}
