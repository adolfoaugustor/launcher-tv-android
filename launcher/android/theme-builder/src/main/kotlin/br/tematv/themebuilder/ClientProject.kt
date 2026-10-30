package br.tematv.themebuilder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ClientType {
    @SerialName("igreja") IGREJA,
    @SerialName("loja") LOJA,
}

@Serializable
data class Colors(val background: String, val text: String, val accent: String)

/** Área em frações (0 a 1) da imagem da moldura, como no `theme.json` v1. */
@Serializable
data class Slot(val x: Double, val y: Double, val w: Double, val h: Double)

/** Moldura pronta (`preset`) ou própria (`moldura.png` do projeto com as duas áreas). */
@Serializable
data class IconFrameChoice(
    val preset: String? = null,
    @SerialName("icon_slot") val iconSlot: Slot? = null,
    @SerialName("label_slot") val labelSlot: Slot? = null,
)

@Serializable
data class ProjectVerse(val reference: String, val text: String)

/** Projeto do cliente em `~/tematv/clientes/<theme_id>/projeto.json` (data-delta §1.1). */
@Serializable
data class ClientProject(
    @SerialName("schema_version") val schemaVersion: Int = 1,
    val type: ClientType,
    @SerialName("theme_id") val themeId: String,
    @SerialName("display_name") val displayName: String,
    val colors: Colors,
    val message: String,
    @SerialName("logo_file") val logoFile: String? = null,
    @SerialName("background_file") val backgroundFile: String? = null,
    @SerialName("icon_frame") val iconFrame: IconFrameChoice,
    val verses: List<ProjectVerse>,
    @SerialName("verses_attribution") val versesAttribution: String? = null,
    @SerialName("updated_at") val updatedAt: String,
)

/** Erro de entrada mostrado ao lado do campo da página; [httpStatus] vai para a resposta da API local. */
class UserInputException(val field: String?, message: String, val httpStatus: Int = 400) : Exception(message)
