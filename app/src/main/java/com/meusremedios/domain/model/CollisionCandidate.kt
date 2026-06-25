package com.meusremedios.domain.model

/** Candidato retornado por [com.meusremedios.domain.usecase.CheckPhotoCollisionUseCase]. */
data class CollisionCandidate(
    val medicationId: Long,
    val medicationName: String,
    val score: Float,
)
