# solaris — handoff de design para implementação

Proposta A, "retrô calmo". Versão 1.0 do handoff, 13/10/2026.
Destino: app Android em Kotlin + Jetpack Compose (fases M0 a M2 do documento de requisitos).

## 0. Como usar este pacote

| Ordem | Arquivo | Para quê |
|---|---|---|
| 1 | `docs/Requisitos_v1.1.docx` | Fonte da verdade de **comportamento**: requisitos funcionais (RF), não funcionais (RNF), API e casos de teste (CT). |
| 2 | `HANDOFF.md` (este arquivo) | Fonte da verdade de **interface**: telas, estados, componentes, textos e regras visuais. |
| 3 | `design-tokens.json` | Valores exatos de cor, tipografia, espaçamento e dimensões. Não copie cores a olho das imagens. |
| 4 | `referencia-png/` | Como cada tela deve ficar. Use para comparar o resultado (critério de aceite visual, seção 10). |
| 5 | `telas/*.html` | As mesmas telas em HTML estático. Abra no navegador para medir distâncias e ler valores CSS exatos. |
| 6 | `assets/` | Logo, ícones e o GIF da abertura. |

Se o documento de requisitos e este handoff discordarem sobre **comportamento**, vale o documento. Sobre **aparência**, vale este handoff.

Todos os dados pessoais nas telas são fictícios (RNF-12).

## 1. Identidade

- **Nome exibido:** `solaris`, em minúsculas e IBM Plex Mono. Nome longo: `solaris-memory-assistente`.
- **Símbolo:** "Reflexo A2", um planeta sobre o horizonte com o reflexo em 2 faixas. Existe **só em versão escura**: sobre Deep Ocean, ou dentro de um quadrado Deep Ocean quando a tela é clara.
- **Paleta oficial:** Estação. Pode ser trocada pelo usuário em Ajustes › Aparência (seção 7).
- **Princípios visuais:**
  - Cantos retos em tudo (`RectangleShape`).
  - Filetes de 1 px no lugar de sombras. Sem elevação, sem gradiente e sem transparência decorativa.
  - Sem emojis em lugar nenhum da interface.
  - Ícones lineares com traço de 1,75 px e pontas quadradas. Não usar Material Icons preenchidos.
  - Pixel art só no logo e na tela de abertura.
  - Metadados (datas, IDs, rótulos) sempre em IBM Plex Mono, caixa alta, 12 px.
  - Datas no formato `dd/mm/aaaa` e horas em `HH:mm` (24 h), exibidas no fuso local (PRE-04).

## 2. Tokens → Compose

Leia `design-tokens.json`. Estrutura sugerida:

```kotlin
@Immutable
data class SolarisColors(
    val fundo: Color, val superficie: Color, val texto: Color, val filete: Color,
    val usuario: Color, val textoSobreUsuario: Color,
    val acao: Color, val textoSobreAcao: Color, val iconeSobreAcao: Color,
    val status: Color, val foco: Color, val trilho: Color,
    val textoSecundario: Color, // escuro: Faded Gold; claro: igual a `texto`
)
val LocalSolarisColors = staticCompositionLocalOf<SolarisColors> { error("sem tema") }

@Composable
fun SolarisTheme(paleta: Paleta, escuro: Boolean, content: @Composable () -> Unit)
```

- Gere `SolarisColors` a partir de `paletas.<id>.claro` ou `paletas.<id>.escuro`.
- `temaPadrao = "sistema"`: siga `isSystemInDarkTheme()` até o usuário escolher outro.
- **Tipografia:** IBM Plex Sans e IBM Plex Mono (licença OFL). Empacote os pesos 400, 500 e 600 em `res/font`, ou use o provedor de fontes do Google Play Services. Os nomes de estilo estão em `tipografia.estilos`.
- **Use `sp` para textos.** Nenhum texto abaixo de 12 sp. Respeite a escala de fonte do sistema (CT-12):
  - nenhuma altura fixa pode cortar texto;
  - cabeçalho e barra de navegação crescem com o conteúdo, usando as alturas do token como mínimo;
  - listas e conversa rolam.
- `forma.cantos = 0` em todos os componentes, inclusive diálogos e bottom sheets.
- **Foco visível:** contorno de 2 dp na cor `foco`, afastado 2 dp do elemento.
- **Alvo de toque:** no mínimo 44 × 44 dp, mesmo em ícones.

## 3. Componentes

| Nome no design | Composable sugerido | Onde aparece | Notas |
|---|---|---|---|
| Cabeçalho | `AppHeader(titulo, voltar?, status)` | todas | Altura mínima 56 dp, filete inferior. À esquerda: símbolo em quadrado de 30 dp ou botão voltar. À direita: status da VPN (quadrado de 8 dp na cor `status` + `VPN · OK`, ou `SEM VPN` com quadrado vazado em ação). |
| Navegação inferior | `BottomNav` | telas principais | 3 itens: CHAT, MEMÓRIAS, AJUSTES. Ícone + rótulo mono 12. O item ativo tem filete superior de 2 dp na cor `foco` e peso 600. Não usar `NavigationBar` do Material com indicador em pílula. |
| Compositor | `Composer` | Chat | `[+]` (44 dp, contorno 1 dp) + campo (44 dp, fundo `superficie`) + enviar (44 dp, fundo `acao`, seta em `iconeSobreAcao`). **Não há atalhos acima do campo.** |
| Mensagem do usuário | `UserMessage` | Chat | Bloco alinhado à direita, fundo `usuario`, texto `textoSobreUsuario`, padding 10/12, largura máx. 80%. Comandos (`/add…`) em mono 600. |
| Resposta | `AssistantMessage` | Chat | **Sem balão.** Meta `SOLARIS · HH:mm` e texto corrido; pode conter uma Ficha, um valor mascarado ou botões. |
| Ficha | `MemoryCard(variante)` | Chat, Memórias, Detalhe | Fundo `superficie`, filete de 1 dp. Cabeçalho mono (`FICHA Nº XXXX` / `Nº XXXX` + data), texto literal da memória e rodapé com selo. Na variante do chat há um "furo" decorativo (círculo de 8 dp com contorno) centralizado embaixo. Três variantes: `Fonte` (no chat), `Item` (na lista), `Detalhe`. |
| Selo | `Stamp(tipo)` | Fichas | Mono 12 com contorno de 1 dp. `PERMANENTE` (marcador), `TEMPORÁRIA · 1D 23H` (ampulheta, contorno `acao`), `SENSÍVEL` (cadeado, contorno `acao`), `VENCIDA EM dd/mm` (contorno tracejado). **Sempre ícone + texto, nunca só cor** (seção 11.5 dos requisitos). |
| Valor mascarado | `MaskedValue` + `RevealButton` | Chat, Memórias | Mostra `••.•••.•••-•`. Ver seção 5.4. |
| Folha Nova memória | `NovaMemoriaSheet` | botão + | `ModalBottomSheet` de cantos retos, alça de 40×3 dp e véu `veu` sobre o fundo. Ver tela 04. |
| Autocomplete de comandos | `CommandSuggestions` | Chat, ao digitar `/` | Popup ancorado acima do compositor, com borda de 1 dp. Ver tela 03. |
| Diálogo de confirmação | `ConfirmDialog` | Excluir | Cantos retos, cabeçalho mono, título 20/600, botões lado a lado. |
| Faixa de erro | `ErrorBanner` | Erros | Abaixo do cabeçalho, fundo `superficie`, ícone de alerta em `acao`, botão Reconectar. Anunciar ao leitor de tela (`LiveRegion.Assertive`). |
| Barra "buscando" | `SearchingBar` | Chat, Abertura | Trilho de 1 dp na cor `trilho` com um segmento de 30% que vai e volta em 1,6 s. Parar a animação se as animações do sistema estiverem desativadas. |
| Botões | `PrimaryButton`, `SecondaryButton`, `DangerButton`, `OutlineChip`, `IconSquareButton` | várias | Primário: fundo `texto`, texto `fundo`. Secundário: contorno de 1 dp. Perigo: fundo `acao`, texto `textoSobreAcao` (branco). Altura de 48 dp, ou 44 dp nos menores. |
| Interruptor | `SolarisSwitch` | Ajustes | Retângulo de 40×22 dp. Desligado: fundo `superficie` com pino `status`. Ligado: fundo `texto` com pino `usuario`. |
| Seletor de paleta | `PaletteOption` | Aparência | Rádio + nome + selo + faixa com as 5 cores. |

## 4. Telas

Os números batem com os arquivos em `telas/` e `referencia-png/`.

### 01 · Abertura (`01-abertura`)

- Fundo Deep Ocean. Ao centro, `assets/reflexo.gif` em 256×192 dp, ampliado sem suavização (vizinho mais próximo).
- Abaixo: `solaris` em mono 32/500, `MEMORY-ASSISTENTE` em mono 12 na cor ouro e a frase "Lembra só o que você registrou."
- No rodapé: barra "buscando", `CONECTANDO AO SERVIDOR PRIVADO` e `via WireGuard · nada sai do seu servidor`.
- **Android 12 ou mais novo:** a splash do sistema (`SplashScreen` API) mostra só `icone-app-108.svg` estático. Esta tela vem logo depois, enquanto o app autentica e checa `/api/v1/health`.
- **Animação:** o GIF é a referência. Na implementação, prefira um sprite em `Canvas` do Compose ou uma sequência de quadros, para ficar nítido em qualquer densidade.
- **Movimento reduzido:** mostrar só o primeiro quadro.

### 02 · Chat (`02-chat`) — RF-01, RF-04, RF-13

- A conversa rola e as mensagens novas ficam embaixo. Há um separador de data `— dd/mm/aaaa —`.
- **Após um comando bem-sucedido:** "Salva como temporária. Vale até dd/mm, HH:mm." ou "Salva como permanente." **Só depois** do 201 do servidor (RNF-06).
- **Resposta a uma pergunta:** frase curta com o trecho-chave em 600, seguida da **Ficha-fonte**. Tocar na ficha abre o Detalhe.
- **Dado sensível:** valor mascarado + botão Revelar + meta `SENSÍVEL · PEDE BIOMETRIA`.

### 03 · Autocomplete (`03-chat-autocomplete`) — RF-15

- Aparece quando o campo começa com `/`. Filtra conforme o usuário digita.
- Duas opções:
  - `/add <texto>` — "Memória permanente";
  - `/add-temp <dias> <texto>` — "Temporária · sai das respostas depois do prazo".
- A opção destacada tem fundo `usuario`. Tocar numa opção insere o comando com espaço no fim e posiciona o cursor.
- Rodapé: "Prefere um formulário? Toque em +".
- Semântica: o popup é uma lista de opções e o campo indica que está expandido.

### 04 · Nova memória (`04-nova-memoria`) — RF-02, RF-03, RF-12, RF-15

- **Tipo:** dois cartões de rádio, Permanente ("Fica até você excluir.") e Temporária ("Sai das respostas depois do prazo."). Padrão: Temporária com 2 dias, igual ao exemplo; ajustável.
- **VALE POR** (só se for Temporária): botões `−` e `+` e o valor `N dia(s)`, de 1 a 365. Embaixo, ao vivo: `até dd/mm, HH:mm`, calculado como agora + 24 h × N.
- **O QUE VOCÊ QUER LEMBRAR?:** campo de várias linhas. Botão Salvar desativado enquanto estiver vazio.
- **DATA DO EVENTO** (opcional): "Não informada" + botão Definir, que abre um seletor de data e hora. Enviar como `eventAt`. **Nunca preencher sozinho** (seção 5.1 dos requisitos).
- **Marcar como sensível:** envia `sensitivity: SENSITIVE`.
- **Salvar memória** (botão primário, largura total): enquanto envia, troque o texto por "Salvando…" e desative o botão. Em caso de sucesso, feche a folha e mostre a confirmação no chat. Em caso de erro, a folha **continua aberta** com o texto preservado e a mensagem de erro.
- **Rodapé educativo:** `Equivale a /add-temp N no chat` ou `Equivale a /add no chat`.

### 05 · Memórias (`05-memorias`) — RF-07, RF-14

- **Busca:** campo com lupa; busca textual no servidor (`GET /memories?q=`).
- **Filtros:** abas mono TODAS · PERMANENTES · TEMPORÁRIAS, com sublinhado de 2 dp na ativa.
- **VIGENTES · N:** fichas `Item`, das mais recentes para as mais antigas.
- **VENCIDAS · N · FORA DAS RESPOSTAS:** seção separada por filete tracejado. Fichas tracejadas e texto riscado. Nunca aparecem como atuais (RF-06).
- Valores sensíveis aparecem mascarados também na lista.

### 06 · Detalhe (`06-detalhe`) — RF-07, RF-08, RF-09

- **Cabeçalho:** voltar + `Ficha Nº XXXX`.
- **TEXTO REGISTRADO:** o texto literal em 21/500.
- **VENCE EM** (só temporárias): contador mono 34 (`1d 23h 12m`), uma barra de 4 dp com a proporção restante e "Até dd/mm/aaaa, HH:mm. Depois disso, não entra mais nas respostas."
- **Metadados:** REGISTRADA, EVENTO, ATUALIZADA, STATUS.
- **Botões:** Editar (secundário) e Excluir (perigo).
- **Editar:** reaproveita a folha Nova memória com o título "Editar memória", os valores preenchidos e "Salvar alterações" (`PATCH`). Esse estado não foi desenhado à parte.

### 07 · Excluir (`07-excluir`) — RF-08, CT-09

- Diálogo com título "Excluir esta ficha?" e texto: "“<início do texto>” sai das buscas na hora. Não dá para desfazer."
- Botões Cancelar e Excluir.
- Depois do 204, volte para Memórias. O item some imediatamente da lista e do chat.

### 08 · Ajustes (`08-ajustes`) — RNF-04, RNF-08, seção 10.3

- **CONEXÃO:** VPN · WireGuard, Servidor (API) e Modelo local, cada um com seu status. Endereço do servidor + Testar.
- **APARÊNCIA:** linha que leva à tela 09 e mostra a paleta atual.
- **PRIVACIDADE** (interruptores):
  - Pedir biometria ao abrir (ligado);
  - Mascarar dados sensíveis (ligado);
  - Ocultar de novo depois de 30 segundos (ligado);
  - Bloquear capturas de tela (ligado);
  - Mostrar texto nas notificações (desligado).
- **Botões:** Limpar cache e Encerrar sessão (contorno e texto em ação).

### 09 · Aparência (`09-aparencia`)

- **TEMA:** controle segmentado Claro · Escuro · Sistema (padrão: Sistema).
- **PALETA:** 5 opções. Estação vem primeiro, marcada como "OFICIAL · PADRÃO".
- **ESTILOS IMPORTADOS:** área tracejada com "Nenhum estilo importado ainda." e o botão "Importar estilo". Ver seção 7.
- A troca de tema ou de paleta vale na hora, sem reiniciar o app. A escolha fica salva no dispositivo.

### 10 · Erros (`10-erros`) — RF-10, RF-13, RF-15, CT-06, CT-07

- **Sem servidor:** faixa "Sem conexão com o servidor. Nada foi salvo." + Reconectar. O status do cabeçalho vira `SEM VPN`.
- **Conflito:** "Encontrei duas fichas diferentes:" + duas fichas compactas + "A mais recente é a de ontem à noite. Qual delas vale?"
- **Comando inválido:** "Comando inválido. Nada foi salvo. Os dias precisam ser um número:" + exemplo corrigido em mono + botão "Usar o formulário".
- **Mensagem não enviada:** bloco tracejado + `NÃO ENVIADO` + "Tentar de novo". Reenviar com o **mesmo** `clientRequestId` (CT-08).

### 11 · Não encontrei e buscando (`11-vazio-buscando`) — RF-04, CT-05

- **Sem resultado:** "Não encontrei essa informação." + "Não há nenhuma ficha sobre isso. Quer guardar agora?" + botão "Nova memória".
- **Buscando:** `Procurando em N memórias vigentes…` + barra "buscando". Anunciar como status ao leitor de tela.

### 12 · Dado sensível revelado (`12-sensivel`) — RNF-04, seção 12

Ver seção 5.4.

### 13 e 14 · Tema escuro (`13-chat-escuro`, `14-memorias-escuro`)

Mesma estrutura das telas claras com os tokens `escuro`. Metadados em ouro (`textoSecundario`), filetes em verde oxidado e o símbolo sem o quadrado.

### 20, 21 e 30 · Folhas de referência

Componentes, paletas e logo. Não são telas do app.

## 5. Comportamentos obrigatórios

### 5.1 Comandos digitados (seção 4 dos requisitos)

- Reconhecer no app, de forma determinística, antes de qualquer chamada ao modelo:
  - `^/add\s+(.+)$` → permanente;
  - `^/add-temp\s+(\d+)\s+(.+)$` → temporária, com N inteiro entre 1 e 365.
- Qualquer outro texto começando com `/add` é **comando inválido**: mostrar o erro da tela 10 sem chamar a API.
- Traduzir os comandos para `POST /api/v1/memories {content, ttlDays?, eventAt?, sensitivity?}`. **Nunca** mandar o comando como pergunta ao LLM.

### 5.2 Confirmações honestas

- Nunca mostrar "Salva…" ou remover um item sem a resposta de sucesso do servidor (RNF-06 e RF-10).
- Enquanto espera, a mensagem do usuário aparece normal e a resposta mostra a barra "buscando" ou "Salvando…".
- Toda escrita leva um `clientRequestId` (UUID gerado no app) para reenvio idempotente.

### 5.3 Temporárias

- O prazo restante aparece como `1D 23H` nos selos e `1d 23h 12m` no Detalhe. Abaixo de 1 hora, use minutos (`45M`).
- Ao vencer, a memória vai para "Vencidas" e nunca é usada em respostas, mesmo que o app ainda tenha cache (RF-06).

### 5.4 Dados sensíveis (RNF-04, RNF-05, seção 12)

1. Valores marcados como sensíveis aparecem sempre mascarados: chat, lista, notificações e pré-visualizações.
2. **Revelar** pede biometria com `BiometricPrompt` (com alternativa de credencial do aparelho).
3. **Depois de revelar:**
   - o valor aparece em mono 22;
   - uma barra de 3 dp em ação vai encolhendo;
   - `OCULTA DE NOVO EM 0:SS`;
   - botão Ocultar.

   Aos 30 s, ou ao sair da tela ou mandar o app para segundo plano, volta a mascarar.
4. Com "Bloquear capturas de tela" ligado, que é o padrão, aplicar `WindowManager.LayoutParams.FLAG_SECURE` à janela. Isso também esconde o conteúdo na tela de apps recentes. A faixa `CAPTURA DE TELA BLOQUEADA NESTA CONVERSA` aparece enquanto um valor sensível está revelado.
5. Nunca registrar o valor em logs (RNF-05) nem guardá-lo em cache em claro.

### 5.5 Acessibilidade (RNF-09, seção 11.5)

- Contraste mínimo de 4,5:1 para texto. Todas as paletas fornecidas passam (seção 7).
- Rótulos para leitor de tela em todos os botões só com ícone: "Nova memória", "Enviar", "Voltar para Memórias", "Fechar".
- Status, erros e o contador sensível anunciados por regiões dinâmicas (live regions), sem roubar o foco.
- Temporária, permanente e sensível se distinguem por **ícone + texto**, nunca só por cor.
- Respeitar a escala de fonte e o movimento reduzido do sistema.

## 6. Ícones

Todos lineares, num quadro de 24 × 24, traço de 1,75 e pontas quadradas. Os caminhos SVG exatos estão em `telas/20-componentes.html`. Converta para `ImageVector`.

| Ícone | Uso | Caminho SVG |
|---|---|---|
| chat | navegação | `M4 5h16v11H9l-5 4z` |
| memórias (arquivo) | navegação | `M3 4h18v4H3zM5 8v12h14V8M10 12h4` |
| ajustes (controles) | navegação | `M4 7h8M16 7h4M4 17h2M10 17h10` + 2 quadrados de 4×4 |
| permanente (marcador) | selo | `M6 3h12v18l-6-4-6 4z` |
| temporária (ampulheta) | selo | `M7 3h10L7 21h10z` |
| sensível (cadeado) | selo | retângulo 5,11,14×10 + `M8 11V7a4 4 0 0 1 8 0v4` |
| revelar (olho) | botão | `M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12z` + círculo r3 |
| buscar | campo | círculo (11,11) r6 + `M16 16l5 5` |
| editar | botão | `M4 20h4L19 9l-4-4L4 16z` |
| excluir | botão | `M4 6h16M9 6V3h6v3M6 6l1 15h10l1-15` |
| nova (+) | botão | `M12 5v14M5 12h14` |
| enviar | botão | `M5 12h13M13 7l5 5-5 5` |
| voltar | cabeçalho | `M19 12H6M11 7l-5 5 5 5` |
| fechar | folha | `M6 6l12 12M18 6L6 18` |
| alerta | erro | `M12 3L2 21h20z` + `M12 10v5M12 17.5v1` |

## 7. Paletas e importação de estilo

As 5 paletas estão em `design-tokens.json › paletas`. Contraste verificado (texto sobre fundo e superfície, texto sobre destaque, branco sobre ação):

| Paleta | Pior caso | Situação |
|---|---|---|
| Estação (oficial) | branco sobre ação: 5,13:1 | ok |
| Biblioteca | texto sobre destaque: 6,55:1 | ok |
| Gelo | branco sobre ação: 4,84:1 | ok |
| Deserto | branco sobre ação: 5,72:1 | ok |
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
2. Derivar os tokens `claro` e `escuro` com o mesmo mapeamento das paletas prontas (seção 2). Para as duas cores que o arquivo não traz, use uma aproximação:
   - `superficie` clara = 60% papel + 40% branco;
   - `superficie` escura = 92% base + 8% papel.

   Os valores das paletas prontas foram ajustados à mão e podem diferir um pouco dessa fórmula.
3. **Conferir o contraste antes de aplicar:** base/papel, base/destaque e branco/ação precisam ser ≥ 4,5:1. Se falhar, mostrar quais pares falharam e não aplicar.
4. Guardar no aparelho e listar na área "Estilos importados", com opção de remover.
5. Nunca executar nem interpretar nada além dessas chaves. O arquivo é só dado.

## 8. Logo e ícones do app (`assets/`)

| Arquivo | Uso |
|---|---|
| `logo-simbolo.svg` | Símbolo para ≥ 40 dp, sobre fundo Deep Ocean. |
| `logo-simbolo-pequeno.svg` | 24–32 dp, com traços mais grossos (cabeçalho). |
| `icone-app-108.svg` | Base do ícone adaptativo. Fundo: Deep Ocean. Primeiro plano: o símbolo na zona segura de 72 dp. Gerar o `ic_launcher` com o Image Asset Studio. |
| `icone-notificacao.svg` | Ícone monocromático da barra de status. |
| `logo-pixel-16.svg` | Versão pixel; ampliar só em múltiplos inteiros. |
| `assinatura-horizontal.svg` | Símbolo + nome, para materiais fora do app. |
| `reflexo.gif` | Animação da abertura: 32×24 pixels ×8, 12 quadros, 160 ms. |

## 9. Fora deste handoff (decidir na implementação)

- Seletor de data e hora do evento: usar o do Material 3 com o tema aplicado (cantos retos, cores do token).
- Primeiro uso: configuração do endereço do servidor e da sessão.
- Lista de Memórias vazia (zero memórias): reaproveitar o texto e o botão da tela 11.
- Estado "Editar memória": seção 4, tela 06.
- Widgets, compartilhamento e modo offline (M5).

## 10. Critério de aceite visual

Para cada tela de `referencia-png/` (capturas a 390 × 844 dp, escala 2×):

- Cores idênticas aos tokens. Nada de cor "parecida".
- Fontes, pesos e tamanhos iguais à tabela de tipografia; diferença de até 1 sp é aceitável por renderização.
- Cantos retos, filetes de 1 dp e nenhuma sombra.
- Mesma hierarquia e ordem de elementos. Espaçamentos com tolerância de ±2 dp.
- Funciona com fonte do sistema em 130%, sem corte (CT-12).
- Leitor de tela anuncia todos os botões só com ícone.
