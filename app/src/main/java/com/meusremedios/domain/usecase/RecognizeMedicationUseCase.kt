package com.meusremedios.domain.usecase

import com.meusremedios.data.ml.FeatureExtractor
import com.meusremedios.data.ml.FeatureSet
import com.meusremedios.data.ml.RecognitionEngine
import com.meusremedios.data.ml.RecognitionScorer
import com.meusremedios.data.repository.MedicationPhotoRepository
import com.meusremedios.data.repository.MedicationRepository
import com.meusremedios.domain.model.MedicationPhoto
import com.meusremedios.domain.model.RecognitionCandidate
import com.meusremedios.domain.model.RecognitionOutcome
import javax.inject.Inject

/**
 * Reconhece um comprimido comparando uma ou mais fotos de consulta com as fotos
 * cadastradas. Agrega por medicamento pelo melhor score (combinando frente/verso
 * e múltiplas fotos) e decide via [RecognitionEngine]. Tudo on-device (RN-3.3).
 *
 * @param queryImagePaths caminhos das fotos capturadas (1 = frente; 2 = frente+verso).
 */
class RecognizeMedicationUseCase
    @Inject
    constructor(
        private val medicationPhotoRepository: MedicationPhotoRepository,
        private val medicationRepository: MedicationRepository,
        private val featureExtractor: FeatureExtractor,
    ) {
        suspend operator fun invoke(queryImagePaths: List<String>): RecognitionOutcome {
            if (queryImagePaths.isEmpty()) return RecognitionOutcome.NoMatch

            val registeredPhotos = medicationPhotoRepository.getAll()
            if (registeredPhotos.isEmpty()) return RecognitionOutcome.NoPhotosRegistered

            val queryFeatures =
                queryImagePaths.map { path ->
                    featureExtractor.extract(path).let { features ->
                        FeatureSet(
                            embedding = features.embedding,
                            colorLab = features.dominantColorLab,
                            aspectRatio = features.aspectRatio,
                            imprintText = features.imprintText,
                        )
                    }
                }

            val candidates =
                registeredPhotos
                    .groupBy { it.medicationId }
                    .mapNotNull { (medicationId, photos) ->
                        val medication = medicationRepository.getById(medicationId) ?: return@mapNotNull null
                        val bestScore =
                            photos.maxOf { photo ->
                                queryFeatures.maxOf { query -> RecognitionScorer.score(query, photo.toFeatureSet()) }
                            }
                        RecognitionCandidate(
                            medicationId = medicationId,
                            medicationName = medication.name,
                            score = bestScore,
                        )
                    }
                    .sortedByDescending { it.score }

            return RecognitionEngine.decide(candidates)
        }

        private fun MedicationPhoto.toFeatureSet(): FeatureSet =
            FeatureSet(
                embedding = embedding,
                colorLab = dominantColorLab,
                aspectRatio = aspectRatio,
                imprintText = imprintText,
            )
    }
