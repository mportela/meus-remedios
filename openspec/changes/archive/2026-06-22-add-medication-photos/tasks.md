## 1. Armazenamento de imagens (`data/media`)

- [x] 1.1 `MedicationImageStore` (interface): `stage`, `persist`, `delete`
- [x] 1.2 `FileMedicationImageStore` (impl): grava em `files/medication_photos/`,
      copia URIs para cache (`stage`), move cache→definitivo (`persist`) e apaga
- [x] 1.3 `FileProvider` no manifest + `res/xml/file_paths.xml`

## 2. Extração de features (`data/ml`)

- [x] 2.1 `LabColor` (puro): conversão sRGB→Lab (D65) e cor dominante de `IntArray`
- [x] 2.2 `PhotoFeatures` (embedding?, dominantColorLab, aspectRatio)
- [x] 2.3 `FeatureExtractor` (interface) + `DefaultFeatureExtractor` (decodifica
      arquivo com downsample, calcula cor Lab e aspect ratio; embedding = null)

## 3. Use cases de foto (`domain/usecase`)

- [x] 3.1 `AddMedicationPhotoUseCase` (extrai features + persiste arquivo + grava)
- [x] 3.2 `RemoveMedicationPhotoUseCase` (apaga arquivo + registro)
- [x] 3.3 `ObserveMedicationPhotosUseCase`

## 4. Injeção de dependência (Hilt)

- [x] 4.1 `MediaModule` (vincula `MedicationImageStore` e `FeatureExtractor`)

## 5. Integração na UI do formulário

- [x] 5.1 Estado de fotos no `MedicationFormViewModel` (existentes + pendentes +
      removidas) e processamento após salvar com sucesso
- [x] 5.2 Seção de fotos no `MedicationFormScreen`: adicionar (câmera/galeria),
      escolher lado, miniaturas e remover
- [x] 5.3 Launchers `TakePicture`/`PickVisualMedia` + `FileProvider` na captura
- [x] 5.4 Strings pt-BR (adicionar foto, câmera, galeria, frente/verso, remover)
- [x] 5.5 Acessibilidade: rótulos/`contentDescription` em pt-BR

## 6. Testes e verificação

- [x] 6.1 Testes de `LabColor` (conversão determinística de cores conhecidas)
- [x] 6.2 Testes dos use cases de foto (add/remove) com fakes de store/extractor/
      repositório
- [x] 6.3 Rodar `./gradlew test` e `./gradlew assembleDebug`
- [x] 6.4 Atualizar `CHANGELOG.md` (seção "Não lançado")
