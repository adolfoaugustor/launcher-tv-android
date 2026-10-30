package br.tematv.themebuilder

import br.tematv.themecore.ImageHeaderReader
import br.tematv.themecore.ThemeErrorCode
import br.tematv.themecore.ThemeLoadResult
import br.tematv.themecore.ThemePackageLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PackageBuilderTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val presets = Presets.load()
    private val store by lazy { ProjectStore(folder.newFolder("clientes"), presets) }
    private val themesDir by lazy { File(folder.root, "temas") }
    private val builder by lazy { PackageBuilder(themesDir, presets) }
    private val images = ImageProcessor()

    private fun projectWithLogo(type: ClientType, name: String): ClientProject {
        val project = store.create(type, name, null)
        val logo = images.save(ImageKind.LOGO, TestImages.png(200, 100), store.projectDir(project.themeId))
        return store.updateImage(project.themeId, ImageKind.LOGO, logo)
    }

    private fun loadBack(file: File) =
        (ThemePackageLoader().load(file, folder.newFolder()) as ThemeLoadResult.Loaded).theme.manifest

    @Test
    fun `pacote de igreja é aceito pelo launcher com os 20 versículos NVI`() {
        val project = projectWithLogo(ClientType.IGREJA, "Igreja Teste")

        val built = builder.build(project, store.projectDir(project.themeId))
        val manifest = loadBack(built.file)

        assertEquals(File(themesDir, "igreja-teste.tematv"), built.file)
        assertEquals(20, manifest.verses.size)
        assertTrue(manifest.verses.all { it.version == "NVI" })
        assertEquals(presets.nviAttribution, manifest.versesAttribution)
        assertEquals("frame-biblia.png", manifest.iconFrame.image)
        assertEquals(built.file.length(), built.sizeBytes)
        assertTrue(built.adbCommands.any { "content write --uri content://br.tematv.launcher.themes/igreja-teste.tematv" in it })
    }

    @Test
    fun `pacote de loja usa a versão LOJA e a atribuição do projeto`() {
        val project = projectWithLogo(ClientType.LOJA, "Hamburgueria Teste")
        val edited = store.save(
            project.themeId,
            project.copy(
                verses = listOf(ProjectVerse("Promo 1", "Terça tem batata grátis."), ProjectVerse("Promo 2", "Combo do dia.")),
                versesAttribution = "Avisos da casa",
            ),
        )

        val manifest = loadBack(builder.build(edited, store.projectDir(edited.themeId)).file)

        assertEquals(listOf("LOJA", "LOJA"), manifest.verses.map { it.version })
        assertEquals("Avisos da casa", manifest.versesAttribution)
        assertEquals("frame-hamburguer.png", manifest.iconFrame.image)
    }

    @Test
    fun `lista vazia é recusada sem deixar arquivo`() {
        val project = projectWithLogo(ClientType.IGREJA, "Igreja Teste").copy(verses = emptyList())

        val error = assertThrows(UserInputException::class.java) { builder.build(project, store.projectDir(project.themeId)) }

        assertEquals("verses", error.field)
        assertEquals(emptyList<String>(), themesDir.list().orEmpty().toList())
    }

    @Test
    fun `pacote que o launcher recusaria não é salvo`() {
        val project = projectWithLogo(ClientType.IGREJA, "Igreja Teste").copy(message = "a".repeat(200))

        val error = assertThrows(PackageRejectedException::class.java) { builder.build(project, store.projectDir(project.themeId)) }

        assertEquals(ThemeErrorCode.THEME_FIELD_INVALID, error.error.code)
        assertEquals("message", error.error.field)
        assertEquals(emptyList<String>(), themesDir.list().orEmpty().toList())
    }

    @Test
    fun `sem logotipo pede o envio`() {
        val project = store.create(ClientType.IGREJA, "Igreja Teste", null)

        val error = assertThrows(UserInputException::class.java) { builder.build(project, store.projectDir(project.themeId)) }

        assertEquals("logo", error.field)
    }

    @Test
    fun `fundo grande é reduzido e entra no pacote`() {
        val project = projectWithLogo(ClientType.IGREJA, "Igreja Teste")
        val projectDir = store.projectDir(project.themeId)
        val background = images.save(ImageKind.BACKGROUND, TestImages.jpeg(4000, 3000), projectDir)
        val withBackground = store.updateImage(project.themeId, ImageKind.BACKGROUND, background)

        val info = ImageHeaderReader.read(File(projectDir, background))!!
        val manifest = loadBack(builder.build(withBackground, projectDir).file)

        assertEquals("fundo.jpg", background)
        assertEquals(1440 to 1080, info.width to info.height)
        assertEquals("fundo.jpg", manifest.backgroundImage)
    }

    @Test
    fun `WEBP grande e logotipo que não é PNG são recusados`() {
        val projectDir = folder.newFolder("projeto")

        val webp = assertThrows(UserInputException::class.java) {
            images.save(ImageKind.BACKGROUND, TestImages.webpHeader(2500, 1200), projectDir)
        }
        val logo = assertThrows(UserInputException::class.java) {
            images.save(ImageKind.LOGO, TestImages.jpeg(100, 100), projectDir)
        }

        assertEquals("fundo", webp.field)
        assertEquals("logo", logo.field)
        assertFalse(projectDir.listFiles().orEmpty().any { it.name.startsWith("fundo") || it.name.startsWith("logo") })
    }

    @Test
    fun `WEBP pequeno é aceito e troca o fundo anterior`() {
        val projectDir = folder.newFolder("projeto")
        images.save(ImageKind.BACKGROUND, TestImages.png(800, 600), projectDir)

        val name = images.save(ImageKind.BACKGROUND, TestImages.webpHeader(1920, 1080), projectDir)

        assertEquals("fundo.webp", name)
        assertEquals(listOf("fundo.webp"), projectDir.list()!!.toList())
    }
}
