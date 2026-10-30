package br.tematv.themebuilder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class ProjectStoreTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val presets = Presets.load()
    private var now = Instant.parse("2026-10-30T12:00:00Z")
    private val clock = object : Clock() {
        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?) = this
        override fun instant() = now
    }
    private val store by lazy { ProjectStore(folder.root, presets, clock) }

    @Test
    fun `igreja nova começa com a lista NVI e a moldura de Bíblia`() {
        val project = store.create(ClientType.IGREJA, "Igreja Batista Ágape", null)

        assertEquals("igreja-batista-agape", project.themeId)
        assertEquals(20, project.verses.size)
        assertEquals(presets.nviVerses, project.verses)
        assertEquals("biblia", project.iconFrame.preset)
        assertNull(project.versesAttribution)
    }

    @Test
    fun `loja nova começa com uma mensagem, moldura de hambúrguer e atribuição da loja`() {
        val project = store.create(ClientType.LOJA, "Hamburgueria do Zé", "hamburgueria-ze")

        assertEquals(1, project.verses.size)
        assertEquals("hamburguer", project.iconFrame.preset)
        assertEquals("Mensagens da Hamburgueria do Zé", project.versesAttribution)
    }

    @Test
    fun `gravar e reler preserva os campos e atualiza a data`() {
        val created = store.create(ClientType.IGREJA, "Igreja Teste", null)
        now = now.plusSeconds(60)

        store.save("igreja-teste", created.copy(message = "Mensagem nova"))
        val reloaded = ProjectStore(folder.root, presets, clock).get("igreja-teste")

        assertEquals("Mensagem nova", reloaded.message)
        assertEquals(created.verses, reloaded.verses)
        assertNotEquals(created.updatedAt, reloaded.updatedAt)
        assertEquals(listOf("igreja-teste"), store.list().map { it.themeId })
    }

    @Test
    fun `gravar não muda tipo, identificador nem imagens`() {
        store.create(ClientType.IGREJA, "Igreja Teste", null)
        store.updateImage("igreja-teste", ImageKind.LOGO, "logo.png")

        val saved = store.save(
            "igreja-teste",
            store.get("igreja-teste").copy(type = ClientType.LOJA, themeId = "outro", logoFile = null, versesAttribution = "x"),
        )

        assertEquals(ClientType.IGREJA, saved.type)
        assertEquals("igreja-teste", saved.themeId)
        assertEquals("logo.png", saved.logoFile)
        assertNull(saved.versesAttribution)
    }

    @Test
    fun `identificador repetido é recusado`() {
        store.create(ClientType.IGREJA, "Igreja Teste", null)

        val error = assertThrows(UserInputException::class.java) { store.create(ClientType.LOJA, "Outra", "igreja-teste") }

        assertEquals(409, error.httpStatus)
        assertEquals("theme_id", error.field)
    }

    @Test
    fun `identificador fora do formato é recusado`() {
        listOf("Igreja", "ab", "igreja_x", "-igreja").forEach { themeId ->
            val error = assertThrows(UserInputException::class.java) { store.create(ClientType.IGREJA, "Igreja", themeId) }
            assertEquals("theme_id", error.field)
        }
    }

    @Test
    fun `mensagem acima de 120 caracteres é recusada`() {
        val project = store.create(ClientType.IGREJA, "Igreja Teste", null)

        val error = assertThrows(UserInputException::class.java) { store.save("igreja-teste", project.copy(message = "a".repeat(121))) }

        assertEquals("message", error.field)
    }

    @Test
    fun `o item 367 é recusado`() {
        val project = store.create(ClientType.IGREJA, "Igreja Teste", null)
        val verses = List(367) { ProjectVerse("Ref $it", "Texto $it") }

        val error = assertThrows(UserInputException::class.java) { store.save("igreja-teste", project.copy(verses = verses)) }

        assertEquals("verses", error.field)
        store.save("igreja-teste", project.copy(verses = verses.take(366)))
    }

    @Test
    fun `moldura própria exige áreas dentro da imagem`() {
        val project = store.create(ClientType.IGREJA, "Igreja Teste", null)
        val frame = IconFrameChoice(iconSlot = Slot(0.5, 0.1, 0.6, 0.3), labelSlot = Slot(0.1, 0.7, 0.8, 0.1))

        val error = assertThrows(UserInputException::class.java) { store.save("igreja-teste", project.copy(iconFrame = frame)) }

        assertEquals("icon_frame.icon_slot", error.field)
    }

    @Test
    fun `cliente inexistente responde 404`() {
        val error = assertThrows(UserInputException::class.java) { store.get("nao-existe") }
        assertEquals(404, error.httpStatus)
    }

    @Test
    fun `identificador sugerido tira acentos, símbolos e limita a 40`() {
        assertEquals("igreja-sao-joao-batista", ProjectStore.suggestThemeId("  Igreja São João — Batista! "))
        assertEquals(40, ProjectStore.suggestThemeId("a".repeat(30) + " " + "b".repeat(30)).length)
    }
}
