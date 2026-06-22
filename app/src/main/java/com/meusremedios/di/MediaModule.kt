package com.meusremedios.di

import com.meusremedios.data.media.FileMedicationImageStore
import com.meusremedios.data.media.MedicationImageStore
import com.meusremedios.data.ml.DefaultFeatureExtractor
import com.meusremedios.data.ml.FeatureExtractor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Vincula as interfaces de mídia e extração de features às implementações. */
@Module
@InstallIn(SingletonComponent::class)
abstract class MediaModule {

    @Binds
    @Singleton
    abstract fun bindMedicationImageStore(impl: FileMedicationImageStore): MedicationImageStore

    @Binds
    @Singleton
    abstract fun bindFeatureExtractor(impl: DefaultFeatureExtractor): FeatureExtractor
}
