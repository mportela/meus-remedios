## 1. Camada de dados — queries e update

- [x] 1.1 Adicionar `@Query("SELECT * FROM medication_photos WHERE embedding IS NULL")
         suspend fun getWithoutEmbedding(): List<MedicationPhotoEntity>` em `MedicationPhotoDao`
- [x] 1.2 Adicionar `@Update suspend fun update(photo: MedicationPhotoEntity)` em `MedicationPhotoDao`
- [x] 1.3 Adicionar `suspend fun getPhotosWithoutEmbedding(): List<MedicationPhoto>` em
         `MedicationPhotoRepository` e implementar em `MedicationPhotoRepositoryImpl`
         (mapeia entidade → domínio via conversor existente)
- [x] 1.4 Adicionar `suspend fun update(photo: MedicationPhoto)` em `MedicationPhotoRepository`
         e implementar em `MedicationPhotoRepositoryImpl`
         (mapeia domínio → entidade e chama `dao.update`)

## 2. Use case de migração

- [x] 2.1 Criar `app/src/main/java/com/meusremedios/domain/usecase/MigratePhotoFeaturesUseCase.kt`:
         - Injeta `MedicationPhotoRepository` e `TfliteFeatureExtractor` via construtor
         - `suspend operator fun invoke()`: busca fotos sem embedding, para cada uma executa
           `extractor.extract(photo.filePath)`, copia embedding + imprintText para o domínio,
           chama `repository.update()`. Captura exceções individualmente (log + continue).

## 3. Wiring no Application

- [x] 3.1 Injetar `MigratePhotoFeaturesUseCase` em `MeusRemediosApplication`
- [x] 3.2 Chamar `migratePhotoFeaturesUseCase()` no `appScope` em `onCreate()`, após o warm-up
         do TFLite (para aproveitar o modelo já carregado); usar `Dispatchers.IO`

## 4. Testes

- [x] 4.1 Criar `MigratePhotoFeaturesUseCaseTest` em `app/src/test/`:
         - Cenário: fotos sem embedding → extractor chamado para cada, repository.update chamado
         - Cenário: fotos com embedding já preenchido → extractor não chamado (skip)
         - Cenário: falha em uma foto → demais fotos processadas, exceção não propagada
         - Usar fakes/mocks para `MedicationPhotoRepository` e `TfliteFeatureExtractor`
- [x] 4.2 Adicionar teste em `RepositoryTest` (ou `DaoTest`) para `getPhotosWithoutEmbedding`:
         inserir fotos com e sem embedding, verificar que apenas as sem embedding retornam
- [x] 4.3 Rodar `./gradlew test` e confirmar 0 falhas

## 5. Documentação

- [x] 5.1 Atualizar `CHANGELOG.md` com entrada F4.6 em "Não lançado"
- [x] 5.2 Executar `openspec archive --change migrate-existing-photo-features`
