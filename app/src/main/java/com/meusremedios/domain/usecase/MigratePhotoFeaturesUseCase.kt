package com.meusremedios.domain.usecase

import com.meusremedios.data.ml.FeatureExtractor
import com.meusremedios.data.repository.MedicationPhotoRepository
import javax.inject.Inject

/**
 * Reprocessa fotos cadastradas sem embedding, preenchendo embedding e imprint.
 * Idempotente: fotos com embedding já preenchido são puladas.
 * Falhas individuais são isoladas — um erro não interrompe as demais fotos.
 */
class MigratePhotoFeaturesUseCase
    @Inject
    constructor(
        private val photoRepository: MedicationPhotoRepository,
        private val featureExtractor: FeatureExtractor,
    ) {
        suspend operator fun invoke() {
            val photos = photoRepository.getPhotosWithoutEmbedding()
            for (photo in photos) {
                runCatching {
                    val features = featureExtractor.extract(photo.filePath)
                    photoRepository.update(
                        photo.copy(
                            embedding = features.embedding,
                            imprintText = features.imprintText,
                        ),
                    )
                }
            }
        }
    }
