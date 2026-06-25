package com.meusremedios.domain.usecase

import com.meusremedios.data.ml.FeatureSet
import com.meusremedios.data.ml.RecognitionParams
import com.meusremedios.data.ml.RecognitionScorer
import com.meusremedios.data.repository.MedicationPhotoRepository
import com.meusremedios.data.repository.MedicationRepository
import com.meusremedios.domain.model.CollisionCandidate
import com.meusremedios.domain.model.MedicationPhoto
import javax.inject.Inject

/**
 * Verifica se as features de fotos pendentes colidem visualmente com fotos já
 * cadastradas de outros medicamentos.
 *
 * Retorna candidatos cujo melhor score ≥ [RecognitionParams.THRESHOLD_CONFIDENT],
 * ordenados por score decrescente. O medicamento sendo editado é excluído via
 * [excludeMedicationId] (use `0L` para novos medicamentos).
 */
class CheckPhotoCollisionUseCase
    @Inject
    constructor(
        private val photoRepository: MedicationPhotoRepository,
        private val medicationRepository: MedicationRepository,
    ) {
        suspend operator fun invoke(
            queryFeatures: List<FeatureSet>,
            excludeMedicationId: Long,
        ): List<CollisionCandidate> {
            if (queryFeatures.isEmpty()) return emptyList()

            val registeredPhotos =
                photoRepository.getAll()
                    .filter { it.medicationId != excludeMedicationId }
            if (registeredPhotos.isEmpty()) return emptyList()

            return registeredPhotos
                .groupBy { it.medicationId }
                .mapNotNull { (medicationId, photos) ->
                    val bestScore =
                        queryFeatures.maxOf { query ->
                            photos.maxOf { photo -> RecognitionScorer.score(query, photo.toFeatureSet()) }
                        }
                    if (bestScore >= RecognitionParams.THRESHOLD_CONFIDENT) {
                        val name =
                            medicationRepository.getById(medicationId)?.name
                                ?: return@mapNotNull null
                        CollisionCandidate(
                            medicationId = medicationId,
                            medicationName = name,
                            score = bestScore,
                        )
                    } else {
                        null
                    }
                }
                .sortedByDescending { it.score }
        }

        private fun MedicationPhoto.toFeatureSet(): FeatureSet =
            FeatureSet(
                embedding = embedding,
                colorLab = dominantColorLab,
                aspectRatio = aspectRatio,
                imprintText = imprintText,
            )
    }
