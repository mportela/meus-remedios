# Configuração de Secrets do GitHub para Build Release Assinado

Este arquivo descreve os secrets necessários para que o workflow de release (`release.yml`) gere um APK assinado corretamente.

## Secrets Necessários

### 1. `KEYSTORE_FILE` (string base64 da keystore)
- **Valor:** A string base64 completa do arquivo `app/meus-remedios-key.jks`
- **Como obter:** `base64 < app/meus-remedios-key.jks`
- **Comprimento:** ~4000 caracteres

### 2. `KEYSTORE_PASSWORD`
- **Valor:** Senha da keystore (mínimo 6 caracteres — usar a mesma senha de `storePassword` configurada na geração da keystore)
- **Descrição:** Senha usada ao criar a keystore com `keytool`

### 3. `KEY_PASSWORD`
- **Valor:** Senha da chave privada (mínimo 6 caracteres — usar a mesma senha de `keyPassword` configurada na geração da keystore)
- **Descrição:** Senha de acesso à chave privada dentro da keystore

### 4. `KEY_ALIAS`
- **Valor:** `meus-remedios-key`
- **Descrição:** Alias da chave dentro da keystore

## Como Adicionar os Secrets

### Via GitHub Web Interface:
1. Vá para **Settings → Secrets and variables → Actions**
2. Clique em **New repository secret**
3. Para cada secret acima:
   - Digite o **Name** exatamente como listado
   - Cole o **Secret** (valor)
   - Clique em **Add secret**

### Via GitHub CLI:
```bash
gh secret set KEYSTORE_FILE --body "$(base64 < app/meus-remedios-key.jks)"
gh secret set KEYSTORE_PASSWORD --body "123456"
gh secret set KEY_PASSWORD --body "123456"
gh secret set KEY_ALIAS --body "meus-remedios-key"
```

## Fluxo de Release

Após adicionar os secrets:

1. **Criar tag:** `git tag v1.2.0`
2. **Push da tag:** `git push origin v1.2.0`
3. O workflow `release.yml` será acionado automaticamente
4. APK assinado estará disponível em **Actions → Release Build → Artifacts**

## Segurança

- Os secrets são **criptografados** e armazenados com segurança no GitHub
- Eles **nunca aparecem** em logs de workflow
- Apenas os runners do GitHub Actions têm acesso durante a execução
- Não adicione os secrets no `.gitignore` do repositório — eles ficam apenas no GitHub

## Verificação

Para verificar se os secrets foram adicionados corretamente:

1. Vá para **Settings → Secrets and variables → Actions**
2. Confirme que todos os 4 secrets estão listados
3. Faça um push de teste com uma tag `v*.*.*` para validar o workflow

