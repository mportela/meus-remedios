package com.meusremedios.domain.usecase

import com.meusremedios.data.media.MedicationImageStore
import com.meusremedios.data.repository.MedicationPhotoRepository
import com.meusremedios.domain.model.MedicationPhoto
import javax.inject.Inject

/** Remove uma foto: apaga o registro e o arquivo de imagem associado. */
class RemoveMedicationPhotoUseCase
    @Inject
    constructor(
        private val imageStore: MedicationImageStore,
        private val photoRepository: MedicationPhotoRepository,
    ) {
        suspend operator fun invoke(photo: MedicationPhoto) {
            photoRepository.delete(photo)
            imageStore.delete(photo.filePath)
        }
    }
