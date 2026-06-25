package com.meusremedios.di

import com.meusremedios.data.media.MedicationImageStore
import com.meusremedios.data.ml.FeatureExtractor
import com.meusremedios.data.ml.ImprintReader
import com.meusremedios.domain.usecase.FakeFeatureExtractor
import com.meusremedios.domain.usecase.FakeMedicationImageStore
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Substitui [MediaModule] em testes, provendo fakes leves para ML e armazenamento
 * de imagens — sem TFLite, sem ML Kit, sem acesso ao sistema de arquivos.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [MediaModule::class])
object FakeMlModule {

    @Provides
    @Singleton
    fun provideFeatureExtractor(): FeatureExtractor = FakeFeatureExtractor()

    @Provides
    @Singleton
    fun provideImprintReader(): ImprintReader = object : ImprintReader {
        override suspend fun read(imagePath: String): String? = null
    }

    @Provides
    @Singleton
    fun provideMedicationImageStore(): MedicationImageStore = FakeMedicationImageStore()
}
