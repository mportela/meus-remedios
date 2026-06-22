package com.meusremedios.data.media

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.meusremedios.domain.model.PhotoSide
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementação de [MedicationImageStore] sobre o armazenamento privado do app.
 *
 * - Temporários ficam em `cacheDir/medication_photos_tmp/`.
 * - Definitivos ficam em `filesDir/medication_photos/`.
 */
@Singleton
class FileMedicationImageStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : MedicationImageStore {

    private val tempDir: File
        get() = File(context.cacheDir, TEMP_DIR).apply { mkdirs() }

    private val permanentDir: File
        get() = File(context.filesDir, PERMANENT_DIR).apply { mkdirs() }

    override suspend fun stage(uri: Uri): String = withContext(Dispatchers.IO) {
        val target = File(tempDir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Não foi possível abrir a imagem selecionada." }
            target.outputStream().use { output -> input.copyTo(output) }
        }
        target.absolutePath
    }

    override suspend fun createCameraTarget(): CameraTarget = withContext(Dispatchers.IO) {
        val file = File(tempDir, "${UUID.randomUUID()}.jpg").apply { createNewFile() }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        CameraTarget(tempPath = file.absolutePath, uri = uri)
    }

    override suspend fun persist(
        tempPath: String,
        medicationId: Long,
        side: PhotoSide,
    ): String = withContext(Dispatchers.IO) {
        val source = File(tempPath)
        val medDir = File(permanentDir, medicationId.toString()).apply { mkdirs() }
        val target = File(medDir, "${side.name.lowercase()}_${UUID.randomUUID()}.jpg")
        if (!source.renameTo(target)) {
            source.copyTo(target, overwrite = true)
            source.delete()
        }
        target.absolutePath
    }

    override suspend fun delete(path: String) {
        withContext(Dispatchers.IO) {
            File(path).takeIf { it.exists() }?.delete()
        }
    }

    private companion object {
        const val TEMP_DIR = "medication_photos_tmp"
        const val PERMANENT_DIR = "medication_photos"
    }
}
