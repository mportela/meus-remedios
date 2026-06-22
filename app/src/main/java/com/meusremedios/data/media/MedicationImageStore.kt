package com.meusremedios.data.media

import android.net.Uri
import com.meusremedios.domain.model.PhotoSide

/**
 * Armazenamento de arquivos de imagem das fotos de medicamentos em área privada
 * do app. Os caminhos retornados são absolutos e referenciados no banco.
 */
interface MedicationImageStore {

    /**
     * Copia o conteúdo de uma [uri] selecionada (ex.: galeria) para um arquivo
     * temporário privado e retorna seu caminho. Use [persist] para torná-lo
     * definitivo ou [delete] para descartá-lo.
     */
    suspend fun stage(uri: Uri): String

    /**
     * Cria um arquivo temporário de saída para a câmera e retorna o par
     * (caminho do arquivo, content [Uri] via FileProvider) a ser entregue ao
     * app de câmera.
     */
    suspend fun createCameraTarget(): CameraTarget

    /**
     * Move um arquivo temporário (de [stage] ou [createCameraTarget]) para o
     * diretório definitivo do medicamento e retorna o caminho final.
     */
    suspend fun persist(tempPath: String, medicationId: Long, side: PhotoSide): String

    /** Apaga o arquivo no [path] informado, se existir. */
    suspend fun delete(path: String)
}

/** Alvo de captura de câmera: arquivo temporário e seu content URI. */
data class CameraTarget(
    val tempPath: String,
    val uri: Uri,
)
