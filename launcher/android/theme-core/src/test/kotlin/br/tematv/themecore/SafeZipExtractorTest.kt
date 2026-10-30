package br.tematv.themecore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SafeZipExtractorTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun zipFileWith(entries: List<Pair<String, ByteArray>>): File =
        temporaryFolder.newFile("pacote.tematv").apply { writeBytes(TestPackages.zipBytes(entries)) }

    private fun assertRejected(expectedCode: ThemeErrorCode, block: () -> Unit) {
        try {
            block()
            fail("era esperada a recusa $expectedCode")
        } catch (exception: ThemeRejectedException) {
            assertEquals(expectedCode, exception.error.code)
        }
    }

    @Test
    fun extractsRegularEntries() {
        val zip = zipFileWith(listOf("theme.json" to "{}".toByteArray(), "imagens/logo.png" to byteArrayOf(1, 2, 3)))
        val destination = File(temporaryFolder.root, "destino")

        SafeZipExtractor().extract(zip, destination)

        assertEquals("{}", File(destination, "theme.json").readText())
        assertTrue(File(destination, "imagens/logo.png").isFile)
    }

    @Test
    fun rejectsParentTraversal() {
        val zip = zipFileWith(listOf("../../fora.txt" to "x".toByteArray()))
        val destination = File(temporaryFolder.root, "a/b/destino")

        assertRejected(ThemeErrorCode.THEME_ARCHIVE_UNSAFE) { SafeZipExtractor().extract(zip, destination) }
        assertFalse(File(temporaryFolder.root, "a/fora.txt").exists())
        assertFalse(File(temporaryFolder.root, "fora.txt").exists())
    }

    @Test
    fun rejectsAbsolutePath() {
        val zip = zipFileWith(listOf("/absoluto.txt" to "x".toByteArray()))

        assertRejected(ThemeErrorCode.THEME_ARCHIVE_UNSAFE) {
            SafeZipExtractor().extract(zip, File(temporaryFolder.root, "destino"))
        }
    }

    @Test
    fun rejectsTooManyEntries() {
        val zip = zipFileWith((1..201).map { "arquivo-$it.txt" to byteArrayOf(1) })

        assertRejected(ThemeErrorCode.THEME_ARCHIVE_UNSAFE) {
            SafeZipExtractor().extract(zip, File(temporaryFolder.root, "destino"))
        }
    }

    @Test
    fun acceptsExactlyTheEntryLimit() {
        val zip = zipFileWith((1..200).map { "arquivo-$it.txt" to byteArrayOf(1) })

        SafeZipExtractor().extract(zip, File(temporaryFolder.root, "destino"))
    }

    @Test
    fun rejectsUncompressedTotalAboveLimit() {
        val zip = zipFileWith(listOf("grande.bin" to ByteArray(2_000)))

        assertRejected(ThemeErrorCode.THEME_ARCHIVE_UNSAFE) {
            SafeZipExtractor(maxTotalUncompressedBytes = 1_000).extract(zip, File(temporaryFolder.root, "destino"))
        }
    }

    @Test
    fun rejectsTruncatedZip() {
        val bytes = TestPackages.zipBytes(TestPackages.themeEntries())
        val zip = temporaryFolder.newFile("truncado.tematv").apply { writeBytes(bytes.copyOf(bytes.size / 2)) }

        assertRejected(ThemeErrorCode.THEME_ARCHIVE_CORRUPT) {
            SafeZipExtractor().extract(zip, File(temporaryFolder.root, "destino"))
        }
    }

    @Test
    fun rejectsFileThatIsNotZip() {
        val zip = temporaryFolder.newFile("texto.tematv").apply { writeText("isto não é um zip") }

        assertRejected(ThemeErrorCode.THEME_ARCHIVE_CORRUPT) {
            SafeZipExtractor().extract(zip, File(temporaryFolder.root, "destino"))
        }
    }
}
