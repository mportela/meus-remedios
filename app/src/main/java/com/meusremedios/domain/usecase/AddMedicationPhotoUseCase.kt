package com.meusremedios.domain.usecase

import com.meusremedios.data.media.MedicationImageStore
import com.meusremedios.data.ml.FeatureExtractor
import com.meusremedios.data.repository.MedicationPhotoRepository
import com.meusremedios.domain.model.MedicationPhoto
import com.meusremedios.domain.model.PhotoSide
import javax.inject.Inject

/**
 * Adiciona uma foto a um medicamento: extrai as features da imagem temporária,
 * move o arquivo para o armazenamento definitivo e persiste o registro.
 *
 * @param tempPath caminho do arquivo temporário (de captura ou seleção).
 * @return id da foto persistida.
 */
class AddMedicationPhotoUseCase
    @Inject
    constructor(
        private val imageStore: MedicationImageStore,
        private val featureExtractor: FeatureExtractor,
        private val photoRepository: MedicationPhotoRepository,
    ) {
        suspend operator fun invoke(
            medicationId: Long,
            tempPath: String,
            side: PhotoSide,
        ): Long {
            val features = featureExtractor.extract(tempPath)
            val finalPath = imageStore.persist(tempPath, medicationId, side)
            return photoRepository.add(
                MedicationPhoto(
                    medicationId = medicationId,
                    filePath = finalPath,
                    side = side,
                    embedding = features.embedding,
                    dominantColorLab = features.dominantColorLab,
                    aspectRatio = features.aspectRatio,
                    imprintText = features.imprintText,
                ),
            )
        }
    }
