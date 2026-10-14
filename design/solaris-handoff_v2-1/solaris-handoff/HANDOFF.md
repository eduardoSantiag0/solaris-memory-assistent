# solaris-memory-assistent — handoff de design para implementação

Proposta A, "retrô calmo". Handoff versão 1.1, 14/10/2026.
Destino: app Android em Kotlin + Jetpack Compose (fases M0 a M2 do documento de requisitos).

## 0. Como usar este pacote

| Ordem | Arquivo | Para quê |
|---|---|---|
| 1 | `docs/Requisitos_v1.1.docx` | Fonte da verdade de **comportamento**: requisitos funcionais (RF), não funcionais (RNF), API e casos de teste (CT). |
| 2 | `HANDOFF.md` (este arquivo) | Fonte da verdade de **interface**: telas, estados, componentes, textos e regras visuais. |
| 3 | `design-tokens.json` | Valores exatos de cor, tipografia, espaçamento e dimensões. Não copie cores a olho das imagens. |
| 4 | `referencia-png/` | Como cada tela deve ficar. Use para comparar o resultado (critério de aceite visual, seção 10). |
| 5 | `telas/*.html` | As mesmas telas em HTML estático. Abra no navegador para medir distâncias e ler valores CSS exatos. |
| 6 | `assets/` | Logo, ícones, GIF da abertura e banners do repositório. |
| — | `CORRECOES.md` | Lista de correções da revisão de 14/10/2026, com a captura da build em `correcoes/`. |

Se o documento de requisitos e este handoff discordarem sobre **comportamento**, vale o documento. Sobre **aparência**, vale este handoff.

Todos os dados pessoais nas telas são fictícios (RNF-12).

## 1. Identidade

- **Nome exibido:** `Solaris` no cabeçalho do app, em IBM Plex Mono 400, espaçamento de 3 px e cor `textoSuave`. Nome longo: `solaris-memory-assistent`. O subtítulo da marca é `MEMORY-ASSISTENT`.
- **Símbolo:** "Reflexo A2", um planeta sobre o horizonte com o reflexo em 2 faixas. Existe **só em versão escura**. Aparece na abertura, no ícone do app e nos banners; **não aparece no cabeçalho**.
- **Paleta oficial:** Estação. Pode ser trocada pelo usuário em Ajustes › Aparência (seção 7).
- **Princípios visuais:**
  - Cantos retos em tudo (`RectangleShape`).
  - Sem sombras, sem elevação, sem gradiente, sem transparência decorativa.
  - **Hierarquia de contornos** (seção 2.2): borda só no que é botão de contorno ou opção selecionável. O resto é superfície preenchida ou linha.
  - Sem emojis em lugar nenhum da interface.
  - Ícones lineares (traço de 1,75 px, pontas quadradas). Exceções: a seta de enviar e a engrenagem vêm do Remix Icon (seção 6).
  - Pixel art só no símbolo, na tela de abertura e nos banners.
  - Metadados (datas, IDs, rótulos) sempre em IBM Plex Mono, caixa alta, 12 px.
  - Datas em `dd/mm/aaaa` e horas em `HH:mm` (24 h), no fuso local (PRE-04).

## 2. Tokens → Compose

Leia `design-tokens.json`. Estrutura sugerida:

```kotlin
@Immutable
data class SolarisColors(
    val fundo: Color, val superficie: Color, val texto: Color, val textoSuave: Color,
    val filete: Color, val divisoria: Color, val tracejado: Color,
    val preenchimento: Color, val seloTemporaria: Color, val seloPermanente: Color,
    val usuario: Color, val textoSobreUsuario: Color,
    val acao: Color, val textoSobreAcao: Color, val iconeSobreAcao: Color,
    val status: Color, val foco: Color, val trilho: Color, val placeholder: Color,
    val textoSecundario: Color, // escuro: Faded Gold; claro: igual a `texto`
)
val LocalSolarisColors = staticCompositionLocalOf<SolarisColors> { error("sem tema") }

@Composable
fun SolarisTheme(paleta: Paleta, escuro: Boolean, content: @Composable () -> Unit)
```

- Gere `SolarisColors` a partir de `paletas.<id>.claro` ou `paletas.<id>.escuro`.
- `temaPadrao = "sistema"`: siga `isSystemInDarkTheme()` até o usuário escolher outro.
- **Tipografia:** IBM Plex Sans e IBM Plex Mono (licença OFL), pesos 400, 500 e 600. Empacote em `res/font` ou use o provedor de fontes do Google Play Services.
- **Use `sp` para textos.** Nenhum texto abaixo de 12 sp. Respeite a escala de fonte do sistema (CT-12):
  - nenhuma altura fixa pode cortar texto;
  - cabeçalho e abas crescem com o conteúdo (as alturas do token são mínimas);
  - listas e conversa rolam.
- **Foco visível:** contorno de 2 dp na cor `foco`, afastado 2 dp do elemento.
- **Alvo de toque:** no mínimo 44 × 44 dp, mesmo em ícones e na linha de fonte.
- **Telas largas** (desktop, tablet, celular deitado): todo o conteúdo (cabeçalho, abas, conversa, compositor) fica numa **coluna de no máximo 560 dp, centralizada**. Fora dela, só o `fundo`. Larguras em porcentagem (como os 78% da mensagem do usuário) se referem à coluna, não à janela. O alvo principal continua sendo Android em retrato.

### 2.1 Ritmo vertical

| Entre | Espaço |
|---|---|
| Turnos da conversa (pergunta → resposta → próxima pergunta) | 28 dp |
| Itens dentro de uma resposta (meta → texto → linha de fonte ou botões) | 6 a 10 dp |
| Separador de data e o turno seguinte | 28 dp |
| Fichas na lista de Memórias | 14 dp |
| Seções de Ajustes e Aparência | 22 dp |
| Margem lateral da tela | 20 dp |

O separador `— dd/mm/aaaa —` (mono 12, centralizado) aparece sempre que o dia muda na conversa.

### 2.2 Hierarquia de contornos

| Tipo de elemento | Tratamento | Token |
|---|---|---|
| Divisórias de estrutura (sob as abas, acima do compositor, entre seções) | linha de 1 dp bem clara | `divisoria` |
| Campo de texto (mensagem, busca, endereço, descrição) | fundo `superficie` + **só linha de base de 2 dp** | `texto` |
| Botão "+" e botões de passo (− / +) | quadrado preenchido, sem borda | `preenchimento` |
| Enviar | quadrado preenchido | `acao` |
| Botões de contorno (Revelar, Reconectar, Tentar de novo, Editar, Testar) | **borda de 1 dp** | `filete` |
| Opções selecionáveis (tipo de memória, paleta, tema) | borda de 1 dp; selecionada ganha fundo `usuario` | `filete` |
| Fichas | superfície preenchida **sem borda**; cabeçalho separado por tracejado | `superficie`, `tracejado` |
| Selos | etiqueta preenchida, sem borda | `seloPermanente`, `seloTemporaria` |
| Valor mascarado, exemplo de comando | superfície preenchida | `preenchimento` |
| Faixa de erro | fundo rosado, sem borda | `seloTemporaria` |
| Fichas vencidas | fundo transparente + borda tracejada | `tracejado` |
| Diálogos e folhas | borda de 1 dp (separa do véu) | `filete` |

## 3. Componentes

| Nome no design | Composable sugerido | Onde aparece | Notas |
|---|---|---|---|
| Cabeçalho principal | `AppHeader` | Conversa e Memórias | Altura mínima 56 dp. Grade de 3 colunas: **status da VPN à esquerda** (quadrado de 7 dp na cor `status` + `VPN · OK` em mono 12; desconectado: quadrado vazado em `acao` + `SEM VPN`), **`Solaris` centralizado**, **engrenagem à direita** (44 dp, abre Ajustes). Sem filete inferior: quem separa são as abas. |
| Cabeçalho secundário | `BackHeader(titulo)` | Detalhe, Ajustes, Aparência | Voltar à esquerda, título centralizado (mono 16, espaçamento 2), engrenagem ou espaço vazio à direita. Filete inferior `divisoria`. |
| Abas | `SectionTabs` | Conversa e Memórias | **Conversa · Memórias 14**, alinhadas à esquerda, logo abaixo do cabeçalho. Altura 46 dp, IBM Plex Sans 15. Ativa: peso 600, cor `texto`, sublinhado de 2 dp em `foco`. Inativa: `textoSuave`. O contador fica numa etiqueta mono 12 com fundo `preenchimento`. Não há barra de navegação inferior. |
| Compositor | `Composer` | Conversa | `[+]` (44 dp, fundo `preenchimento`) + campo + enviar (44 dp, fundo `acao`). Placeholder **"Mensagem aqui…"** na cor `placeholder` (texto a ~42% de opacidade). Não há atalhos de comando acima do campo. |
| Mensagem do usuário | `UserMessage` | Conversa | Bloco à direita, fundo `usuario`, padding 12/14, largura máxima de 78%. Comandos (`/add…`) em mono 600. |
| Resposta | `AssistantMessage` | Conversa | Sem balão. Meta `SOLARIS · HH:mm`, texto em Plex Sans 16 com linha 1,5. Pode conter linha de fonte, valor mascarado ou botões. |
| Confirmação de gravação | `SavedMessage` | Conversa | Resposta com uma **bandeirinha de marcador** (12×16, preenchida em `acao` no claro e `usuario` no escuro) antes do texto: "Salva como temporária. Vale até dd/mm, HH:mm." |
| Linha de fonte | `SourceLink` | Conversa | Substitui o cartão grande de ficha. Uma linha em mono 12, cor `textoSuave`, sublinhado pontilhado: ícone de documento + `FICHA 7C1E · 13/10 · 21:40` + `›`. Abre o Detalhe. Alvo de toque de 44 dp sem aumentar o espaço visual. A resposta em si cita a origem em texto ("segundo o que você registrou ontem às 21:40"). |
| Ficha | `MemoryCard` | Memórias, Detalhe | Superfície `superficie` sem borda. Cabeçalho mono (`Nº XXXX` + selos) separado por tracejado; texto literal; data. No Detalhe há um "furo" decorativo (círculo de 8 dp) centralizado embaixo. |
| Selo | `Stamp` | Fichas | Mono 12, etiqueta preenchida. `PERMANENTE` (marcador, `seloPermanente`), `TEMPORÁRIA · 1D 23H` (ampulheta em `acao`, `seloTemporaria`), `SENSÍVEL` (cadeado em `acao`, `seloTemporaria`), `VENCIDA EM dd/mm` (sem fundo). Sempre ícone + texto, nunca só cor. |
| Valor mascarado | `MaskedValue` + `RevealButton` | Conversa, Memórias | **Sempre `••••••••`** (8 pontos fixos, qualquer que seja o valor; a máscara nunca revela tamanho nem formato), em mono 16 sobre `preenchimento`. Ao lado, sem painel envolvendo os dois, o botão **Revelar**: mono 14/500, contorno de 1 dp, 44 dp de altura, olho linear à esquerda. Embaixo, fora de caixa, o meta `SENSÍVEL · PEDE BIOMETRIA` com cadeado em `acao`. Ver seção 5.4. |
| Folha Nova memória | `NovaMemoriaSheet` | botão + | `ModalBottomSheet` de cantos retos, alça de 40×3 dp, véu sobre o fundo. Ver tela 04. |
| Autocomplete de comandos | `CommandSuggestions` | Conversa, ao digitar `/` | Popup acima do compositor, fundo `superficie` com linha superior de 2 dp em `texto`. Ver tela 03. |
| Diálogo de confirmação | `ConfirmDialog` | Excluir | Cantos retos, borda de 1 dp, cabeçalho mono, título 20/600, botões lado a lado. |
| Faixa de erro | `ErrorBanner` | Erros | Abaixo das abas, fundo `seloTemporaria`, ícone de alerta em `acao`, botão Reconectar. Anunciar ao leitor de tela (`LiveRegion.Assertive`). |
| Barra "buscando" | `SearchingBar` | Conversa, Abertura | Trilho de 1 dp em `trilho` com segmento de 30% que vai e volta em 1,6 s. Parar com movimento reduzido. |
| Botões | `PrimaryButton`, `SecondaryButton`, `DangerButton`, `OutlineChip`, `IconFillButton` | várias | Primário: fundo `texto`, texto `fundo`. Secundário e de contorno: borda de 1 dp. Perigo: fundo `acao`, texto branco. Altura de 48 dp, ou 44 dp nos menores. |
| Interruptor | `SolarisSwitch` | Ajustes | Retângulo de 40×22 dp. Desligado: fundo `superficie`, pino `status`. Ligado: fundo `texto`, pino `usuario`. |
| Seletor de paleta | `PaletteOption` | Aparência | Rádio + nome + selo + faixa com as 5 cores. |

## 4. Telas

Os números batem com os arquivos em `telas/` e `referencia-png/`.

### 01 · Abertura (`01-abertura`)

- Fundo Deep Ocean. Ao centro, `assets/reflexo.gif` em 256×192 dp, ampliado sem suavização (vizinho mais próximo).
- Abaixo: `solaris` em mono 32/500, `MEMORY-ASSISTENT` em mono 12 na cor ouro e a frase "Lembra só o que você registrou."
- Rodapé: barra "buscando", `CONECTANDO AO SERVIDOR PRIVADO` e `via WireGuard · nada sai do seu servidor`.
- **Android 12 ou mais novo:** a splash do sistema (`SplashScreen` API) mostra só `icone-app-108.svg` estático. Esta tela vem logo depois, enquanto o app autentica e checa `/api/v1/health`.
- **Animação:** prefira um sprite em `Canvas` do Compose ou uma sequência de quadros, em vez do GIF. Com movimento reduzido, mostrar só o primeiro quadro.

### 02 · Conversa (`02-chat`) — RF-01, RF-04, RF-13

- Cabeçalho principal + abas (Conversa ativa) + conversa rolável + compositor.
- **Espaçamento:** margem lateral de 20 dp, 28 dp entre turnos, 6 a 10 dp dentro de uma resposta.
- Separador de data `— dd/mm/aaaa —` centralizado.
- **Após comando bem-sucedido:** `SavedMessage` com bandeirinha. **Só depois** do 201 do servidor (RNF-06).
- **Resposta a uma pergunta:** frase curta com o trecho-chave em 600, citando a origem em texto, seguida da **linha de fonte**. Tocar na linha abre o Detalhe.
- **Dado sensível:** valor mascarado + botão Revelar + meta `SENSÍVEL · PEDE BIOMETRIA`.

### 03 · Autocomplete (`03-chat-autocomplete`) — RF-15

- Aparece quando o campo começa com `/`. Filtra conforme o usuário digita.
- Opções:
  - `/add <texto>` — "Memória permanente";
  - `/add-temp <dias> <texto>` — "Temporária · sai das respostas depois do prazo";
  - `/add-sensitive <texto>` — "Permanente e sensível · mascarada nas respostas".
- A opção destacada tem fundo `usuario`. Tocar insere o comando com espaço no fim.
- Rodapé: "Prefere um formulário? Toque em +".
- Semântica: o popup é uma lista de opções e o campo indica que está expandido.

### 04 · Nova memória (`04-nova-memoria`) — RF-02, RF-03, RF-12, RF-15

- **Tipo:** dois cartões de rádio com borda, Permanente ("Fica até você excluir.") e Temporária ("Sai das respostas depois do prazo."). Padrão: Temporária, 2 dias.
- **VALE POR** (só se for Temporária): botões `−` e `+` preenchidos e o valor `N dia(s)`, de 1 a 365. Embaixo, ao vivo: `até dd/mm, HH:mm` (agora + 24 h × N).
- **O QUE VOCÊ QUER LEMBRAR?:** campo de várias linhas com linha de base. Botão Salvar desativado enquanto estiver vazio.
- **DATA DO EVENTO** (opcional): "Não informada" + botão Definir, que abre um seletor de data e hora. Envia `eventAt`. Nunca preencher sozinho (seção 5.1 dos requisitos).
- **Marcar como sensível:** envia `sensitivity: SENSITIVE`.
- **Salvar memória** (primário, largura total):
  - durante o envio: "Salvando…" com o botão desativado;
  - sucesso: fecha a folha e mostra a confirmação na conversa;
  - erro: a folha continua aberta, com o texto preservado.
- **Rodapé educativo:** `Equivale a /add-temp N no chat` ou `Equivale a /add no chat`.

### 05 · Memórias (`05-memorias`) — RF-07, RF-14

- Cabeçalho principal + abas (Memórias ativa). **Tela inteira**, não é gaveta.
- **Barra de busca:** campo com lupa e linha de base, mais um botão `+` preenchido (abre Nova memória). Busca textual no servidor (`GET /memories?q=`).
- **Filtros:** TODAS · PERMANENTES · TEMPORÁRIAS em mono 13, com sublinhado de 2 dp na ativa.
- **VIGENTES · N:** fichas sem borda, das mais recentes para as mais antigas, com 14 dp entre elas.
- **VENCIDAS · N · FORA DAS RESPOSTAS:** depois de uma divisória. Fichas com borda tracejada e texto riscado. Nunca aparecem como atuais (RF-06).
- Valores sensíveis aparecem mascarados também na lista.

### 06 · Detalhe (`06-detalhe`) — RF-07, RF-08, RF-09

- **Cabeçalho secundário:** voltar (para Memórias), `Ficha Nº XXXX` e a engrenagem.
- **TEXTO REGISTRADO:** ficha sem borda com o texto literal em 21/500 e o furo decorativo.
- **VENCE EM** (só temporárias): contador mono 34 (`1d 23h 12m`), barra de 4 dp com a proporção restante e "Até dd/mm/aaaa, HH:mm. Depois disso, não entra mais nas respostas."
- **Metadados:** REGISTRADA, EVENTO, ATUALIZADA, STATUS, separados do resto por uma divisória.
- **Botões:** Editar (contorno) e Excluir (perigo).
- **Editar:** reaproveita a folha Nova memória com o título "Editar memória", os valores preenchidos e "Salvar alterações" (`PATCH`).

### 07 · Excluir (`07-excluir`) — RF-08, CT-09

- Diálogo "Excluir esta ficha?" com o texto: "“<início do texto>” sai das buscas na hora. Não dá para desfazer."
- Botões Cancelar e Excluir. Depois do 204, voltar para Memórias; o item some na hora da lista e da conversa.

### 08 · Ajustes (`08-ajustes`) — RNF-04, RNF-08, seção 10.3

- **Acesso:** pela engrenagem do cabeçalho. Cabeçalho secundário com voltar e o título "Ajustes".
- **CONEXÃO:** VPN · WireGuard, Servidor (API) e Modelo local, cada um com seu status. Endereço do servidor (campo com linha de base) + Testar.
- **APARÊNCIA:** linha que leva à tela 09 e mostra a paleta atual.
- **PRIVACIDADE** (interruptores):
  - Pedir biometria ao abrir (ligado);
  - Mascarar dados sensíveis (ligado);
  - Ocultar de novo depois de 30 segundos (ligado);
  - Bloquear capturas de tela (ligado);
  - Mostrar texto nas notificações (desligado).
- **Botões:** Limpar cache e Encerrar sessão (contorno e texto em ação).

### 09 · Aparência (`09-aparencia`)

- **TEMA:** Claro · Escuro · Sistema (padrão: Sistema).
- **PALETA:** 5 opções. Estação vem primeiro, marcada "OFICIAL · PADRÃO".
- **ESTILOS IMPORTADOS:** área tracejada com "Nenhum estilo importado ainda." e o botão "Importar estilo". Ver seção 7.
- Troca de tema ou paleta vale na hora, sem reiniciar. A escolha fica salva no aparelho.

### 10 · Erros (`10-erros`) — RF-10, RF-13, RF-15, CT-06, CT-07

- **Sem servidor:** faixa rosada "Sem conexão com o servidor. Nada foi salvo." + Reconectar. O status do cabeçalho vira `SEM VPN`.
- **Conflito:** "Sobre a chave do carro, encontrei duas fichas diferentes: gaveta da cozinha (mais recente) e bolso da jaqueta verde. Qual vale?" + uma linha de fonte para cada ficha.
- **Comando inválido:** "Comando inválido. Nada foi salvo. Os dias precisam ser um número:" + exemplo corrigido em mono sobre `preenchimento` + botão "Usar o formulário".
- **Mensagem não enviada:** bloco tracejado + `NÃO ENVIADO` + "Tentar de novo". Reenviar com o **mesmo** `clientRequestId` (CT-08).

### 11 · Não encontrei e buscando (`11-vazio-buscando`) — RF-04, CT-05

- **Sem resultado:** "Não encontrei essa informação." + "Não há nenhuma ficha sobre isso. Quer guardar agora?" + botão "Nova memória".
- **Buscando:** `Procurando em N memórias vigentes…` + barra "buscando". Anunciar como status.

### 12 · Dado sensível revelado (`12-sensivel`) — RNF-04, seção 12

Ver seção 5.4.

### 13 e 14 · Tema escuro (`13-chat-escuro`, `14-memorias-escuro`)

Mesma estrutura das telas claras com os tokens `escuro`. Metadados em ouro (`textoSecundario`), bandeirinha em ouro.

### 20, 21 e 30 · Folhas de referência

Componentes, paletas e logo. Não são telas do app.

## 5. Comportamentos obrigatórios

### 5.1 Comandos digitados (seção 4 dos requisitos)

- Reconhecer no app, de forma determinística, antes de qualquer chamada ao modelo:
  - `^/add\s+(.+)$` → permanente;
  - `^/add-temp\s+(\d+)\s+(.+)$` → temporária, N inteiro de 1 a 365;
  - `^/add-sensitive\s+(.+)$` → permanente e sensível (`sensitivity: SENSITIVE`).
- Temporária e sensível ao mesmo tempo só pela folha Nova memória.
- Qualquer outro texto começando com `/add` é **comando inválido**: mostrar o erro da tela 10 sem chamar a API.
- Traduzir para `POST /api/v1/memories {content, ttlDays?, eventAt?, sensitivity?}`. **Nunca** mandar o comando como pergunta ao LLM.

### 5.2 Confirmações honestas

- Nunca mostrar "Salva…" ou remover um item sem a resposta de sucesso do servidor (RNF-06 e RF-10).
- Enquanto espera, a mensagem do usuário aparece normal e a resposta mostra a barra "buscando" ou "Salvando…".
- Toda escrita leva um `clientRequestId` (UUID gerado no app) para reenvio idempotente.

### 5.3 Temporárias

- Prazo restante: `1D 23H` nos selos e `1d 23h 12m` no Detalhe. Abaixo de 1 hora, minutos (`45M`).
- Ao vencer, vai para "Vencidas" e nunca é usada em respostas, mesmo com cache no app (RF-06).

### 5.4 Dados sensíveis (RNF-04, RNF-05, seção 12)

1. Valores sensíveis aparecem sempre mascarados: conversa, lista, notificações e pré-visualizações.
2. **Revelar** pede biometria com `BiometricPrompt` (com alternativa de credencial do aparelho).
3. **Depois de revelar:**
   - valor em mono 20, sem quebra de linha;
   - barra de 3 dp em `acao` que vai encolhendo;
   - `OCULTA DE NOVO EM 0:SS`;
   - botão Ocultar.

   Aos 30 s, ou ao sair da tela ou mandar o app para segundo plano, volta a mascarar.
4. Com "Bloquear capturas de tela" ligado (padrão), aplicar `WindowManager.LayoutParams.FLAG_SECURE` à janela; isso também esconde o conteúdo na tela de apps recentes. A faixa `CAPTURA DE TELA BLOQUEADA NESTA CONVERSA` aparece enquanto um valor está revelado.
5. Nunca registrar o valor em logs (RNF-05) nem guardá-lo em cache em claro.

### 5.5 Textos exatos

Use estes textos como estão. Partes entre `< >` são preenchidas pelo app.

| Situação | Texto |
|---|---|
| Gravou permanente | Salva como permanente. |
| Gravou permanente e sensível | Salva como permanente e sensível. |
| Gravou temporária | Salva como temporária. Vale até `<dd/mm>`, `<HH:mm>`. |
| Gravou temporária e sensível (pela folha) | Salva como temporária e sensível. Vale até `<dd/mm>`, `<HH:mm>`. |
| Resposta com fonte | `<resposta curta>`, segundo o que você registrou `<quando>`. + linha de fonte. `<quando>` = "hoje às HH:mm", "ontem às HH:mm" ou "em dd/mm". |
| Conflito entre fichas | Sobre `<assunto>`, encontrei **duas fichas diferentes**: `<conteúdo A>` (mais recente) e `<conteúdo B>`. Qual vale? + uma linha de fonte por ficha. Nunca escolher sozinho. |
| Nada encontrado | Não encontrei essa informação. / Não há nenhuma ficha sobre isso. Quer guardar agora? + botão Nova memória |
| Saudação ou conversa sem pergunta | Oi! Pergunte sobre algo que você registrou, ou toque em + para guardar uma memória nova. (sem ficha) |
| Comando inválido | Comando inválido. Nada foi salvo. `<motivo>`: + exemplo corrigido em mono + botão Usar o formulário |
| Sem servidor | Sem conexão com o servidor. Nada foi salvo. + Reconectar |
| Envio falhou | NÃO ENVIADO + Tentar de novo |

Não use rótulos técnicos como "aviso:", "erro:" ou "info:" no começo das mensagens.

### 5.6 Relevância mínima (RF-04, CT-05, CT-13)

1. **Classificar a mensagem antes de buscar:** comando, pergunta ou conversa (saudação, agradecimento). Conversa nunca consulta memórias e recebe a resposta fixa da seção 5.5.
2. **Correspondência textual primeiro:** nomes, números e documentos têm prioridade (RF-05).
3. **Similaridade com limite:** candidatos vindos do índice vetorial abaixo de um limite mínimo configurável são descartados. Calibre o limite com as perguntas dos casos de teste.
4. **Sem candidato acima do limite:** responder "Não encontrei essa informação." e nunca mostrar a "ficha mais parecida".
5. **Dois ou mais candidatos válidos com conteúdo diferente:** usar o texto de conflito, nunca escolher sozinho.

**CT-13 (novo):** "Olá" → resposta fixa sem ficha. Pergunta sem nenhuma relação com as memórias → "Não encontrei essa informação.", sem ficha.

### 5.7 Acessibilidade (RNF-09, seção 11.5)

- Contraste mínimo de 4,5:1 para texto. Todas as paletas e tons passam (seção 7). A exceção consciente é o placeholder, deixado mais apagado a pedido do design; o rótulo acessível do campo é "Mensagem".
- Rótulos para leitor de tela em todos os botões só com ícone: "Nova memória", "Enviar", "Ajustes", "Voltar", "Fechar".
- Status, erros e o contador sensível anunciados por regiões dinâmicas, sem roubar o foco.
- Temporária, permanente e sensível se distinguem por **ícone + texto**, nunca só por cor.
- Respeitar escala de fonte e movimento reduzido do sistema.

## 6. Ícones

A maioria é linear, num quadro de 24 × 24, traço de 1,75 e pontas quadradas. Converta para `ImageVector`. Os caminhos exatos estão em `telas/20-componentes.html` e `telas/02-chat.html`.

| Ícone | Uso | Fonte |
|---|---|---|
| engrenagem | cabeçalho, abre Ajustes | Remix Icon `settings-3-line` (preenchido) |
| enviar | compositor | Remix Icon `corner-right-up-line` (preenchido) |
| bandeirinha | confirmação de gravação | `M0 0h12v16l-6-4-6 4z` (12×16, preenchida) |
| documento | linha de fonte | `M5 3h10l4 4v14H5zM9 11h6M9 15h6` |
| permanente (marcador) | selo | `M6 3h12v18l-6-4-6 4z` |
| temporária (ampulheta) | selo | `M7 3h10L7 21h10z` |
| sensível (cadeado) | selo | retângulo 5,11,14×10 + `M8 11V7a4 4 0 0 1 8 0v4` |
| revelar (olho) | botão | `M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12z` + círculo r3 |
| buscar | Memórias | círculo (11,11) r6 + `M16 16l5 5` |
| editar | Detalhe | `M4 20h4L19 9l-4-4L4 16z` |
| excluir | Detalhe | `M4 6h16M9 6V3h6v3M6 6l1 15h10l1-15` |
| nova (+) | compositor, Memórias | `M12 5v14M5 12h14` |
| voltar | cabeçalho secundário | `M19 12H6M11 7l-5 5 5 5` |
| fechar | folha | `M6 6l12 12M18 6L6 18` |
| alerta | erro | `M12 3L2 21h20z` + `M12 10v5M12 17.5v1` |
| seta (›) | linha de fonte, Ajustes | `M9 6l6 6-6 6` |

Os ícones do Remix Icon seguem a Remix Icon License v1.0: uso em app comercial permitido, atribuição opcional. Sugestão: "Icons by Remix Icon (https://remixicon.com)" na tela Sobre.

## 7. Paletas e importação de estilo

As 5 paletas estão em `design-tokens.json › paletas`, cada uma com versão clara e escura e com os tons de superfície e divisória. Contraste verificado:

| Paleta | Pior caso | Situação |
|---|---|---|
| Estação (oficial) | branco sobre ação: 5,13:1 | ok |
| Biblioteca | texto sobre destaque: 6,55:1 | ok |
| Gelo | branco sobre ação: 4,84:1 | ok |
| Deserto | texto suave sobre fundo claro: 5,58:1 | ok |
| Crepúsculo | branco sobre ação: 5,73:1 | ok |

**Formato de importação** (`.json`, escolhido pelo seletor de arquivos do sistema):

```json
{
  "formato": "solaris-style",
  "versao": 1,
  "nome": "Meu estilo",
  "base": "#172621",
  "linha": "#566C5B",
  "papel": "#E8E5D8",
  "destaque": "#B8A47B",
  "acao": "#995F4C"
}
```

Regras:

1. Rejeitar se `formato` ou `versao` forem desconhecidos, se faltar alguma cor ou se alguma cor não for `#RRGGBB`.
2. Derivar os tokens `claro` e `escuro` com o mesmo mapeamento das paletas prontas. As fórmulas de mistura usadas para os tons derivados (divisória, preenchimento, selos, texto suave) estão em `design-tokens.json` como valores prontos; para estilos importados, use as mesmas proporções:
   - divisória clara: 12% de base sobre papel;
   - preenchimento claro: 7% de base sobre papel;
   - selo temporária claro: 18% de ação sobre papel;
   - selo permanente claro: 14% de linha sobre papel;
   - texto suave claro: 60% de linha sobre base.
3. **Conferir o contraste antes de aplicar:** base/papel, base/destaque, branco/ação e texto suave/papel precisam ser ≥ 4,5:1. Se falhar, mostrar quais pares falharam e não aplicar.
4. Guardar no aparelho e listar em "Estilos importados", com opção de remover.
5. Nunca executar nem interpretar nada além dessas chaves. O arquivo é só dado.

## 8. Logo, ícones do app e banners (`assets/`)

| Arquivo | Uso |
|---|---|
| `logo-simbolo.svg` | Símbolo para ≥ 40 dp, sobre fundo Deep Ocean. |
| `logo-simbolo-pequeno.svg` | 24–32 dp, com traços mais grossos. |
| `icone-app-108.svg` | Base do ícone adaptativo: fundo Deep Ocean, símbolo na zona segura de 72 dp. Gerar o `ic_launcher` com o Image Asset Studio. |
| `icone-notificacao.svg` | Ícone monocromático da barra de status. |
| `logo-pixel-16.svg` | Versão pixel; ampliar só em múltiplos inteiros. |
| `assinatura-horizontal.svg` | Símbolo + nome, para materiais fora do app. |
| `reflexo.gif` | Animação da abertura: 32×24 pixels ×8, 12 quadros, 160 ms. |
| `banners/solaris-banner-readme.gif` | Banner animado do topo do README (1600×560). O `.png` ao lado é a versão estática. |
| `banners/solaris-banner-social.png` | Prévia social do repositório no GitHub (1280×640). O GitHub não anima essa imagem. |

## 9. Fora deste handoff (decidir na implementação)

- Seletor de data e hora do evento: o do Material 3 com o tema aplicado (cantos retos, cores do token).
- Primeiro uso: configuração do endereço do servidor e da sessão.
- Lista de Memórias vazia (zero memórias): reaproveitar o texto e o botão da tela 11.
- Estado "Editar memória": seção 4, tela 06.
- Gesto de deslizar entre Conversa e Memórias: opcional, desde que as abas continuem visíveis.
- Widgets, compartilhamento e modo offline (M5).

## 10. Critério de aceite visual

Para cada tela de `referencia-png/` (capturas a 390 × 844 dp, escala 2×):

- Cores idênticas aos tokens. Nada de cor "parecida".
- Hierarquia de contornos da seção 2.2 respeitada: nenhuma ficha, selo ou campo com borda completa.
- Fontes, pesos e tamanhos iguais à tabela de tipografia; diferença de até 1 sp é aceitável por renderização.
- Cantos retos, nenhuma sombra.
- Mesma hierarquia e ordem de elementos. Espaçamentos com tolerância de ±2 dp.
- Funciona com fonte do sistema em 130%, sem corte (CT-12).
- Leitor de tela anuncia todos os botões só com ícone.
