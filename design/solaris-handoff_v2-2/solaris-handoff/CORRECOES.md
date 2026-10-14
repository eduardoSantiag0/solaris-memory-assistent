# Correções — revisão de design de 14/10/2026

Comparação entre a build atual (`correcoes/observado-2026-10-14.png`, tema escuro, Compose for Desktop) e o design aprovado (`referencia-png/13-chat-escuro.png` e `02-chat.png`).

A estrutura está correta: cabeçalho, abas, bandeirinha, linha de fonte e compositor. Os itens abaixo são o que falta para a build bater com o design. Faça na ordem; os itens P0 são bloqueantes.

| # | Prioridade | Assunto |
|---|---|---|
| 1 | P0 | Respostas com memória sem relação com a pergunta |
| 2 | P0 | Máscara de dado sensível revela o tamanho do valor |
| 3 | P1 | Largura do layout no desktop |
| 4 | P1 | Bloco de dado sensível |
| 5 | P1 | Mensagem de conflito |
| 6 | P1 | Textos de confirmação |
| 7 | P2 | Ritmo vertical e separador de data |
| 8 | P2 | Botão Revelar |
| 9 | P2 | Comando `/add-sensitive` no autocomplete |
| 10 | P2 | Textura dither no fundo da conversa (novo) |

---

## 1. Respostas com memória sem relação com a pergunta (P0) — RF-04, CT-05, CT-13

**Observado:** o usuário escreveu "Ola" e o app respondeu "Esqueci a chave na escola, segundo o que você registrou ontem às 20:46.", com a ficha EDBC.

**Por que está errado:** o app só pode apresentar uma memória quando ela responde à pergunta. A busca por similaridade sempre devolve o "mais parecido", mesmo quando nada é parecido.

**Como corrigir:**

1. Classificar antes de buscar. Saudação, agradecimento ou conversa sem pergunta ("oi", "olá", "obrigado", "tudo bem?") **não consultam memórias**. Resposta fixa:
   > Oi! Pergunte sobre algo que você registrou, ou toque em + para guardar uma memória nova.
2. Aplicar um **limite mínimo de relevância** na recuperação (`HANDOFF.md`, seção 5.6):
   - correspondência textual ou de campo exato tem prioridade;
   - na similaridade vetorial, descartar candidatos abaixo do limite configurado;
   - se nada passar, responder com a tela 11 ("Não encontrei essa informação.").
3. Nunca usar a frase "segundo o que você registrou…" sem uma ficha que realmente sustente a resposta.

**Aceite:** CT-13 (novo). "Olá" → resposta fixa, sem ficha. "Qual a cor do céu?" → "Não encontrei essa informação.", sem ficha.

## 2. Máscara de dado sensível revela o tamanho do valor (P0) — RNF-04, seção 12

**Observado:** `••• •• • •••`, com grupos que acompanham o valor real.

**Por que está errado:** dá para deduzir quantos caracteres e quantas palavras tem o dado escondido.

**Como corrigir:** a máscara é **sempre** `••••••••` (8 pontos, sem espaços nem separadores), qualquer que seja o valor. Isso vale na conversa, na lista de Memórias, no Detalhe e em notificações.

**Aceite:** um RG de 9 dígitos e um texto de 40 caracteres mostram exatamente a mesma máscara.

## 3. Largura do layout no desktop (P1)

**Observado:** a janela tem cerca de 750 px e a conversa ocupa toda a largura. As perguntas ficam coladas na borda direita, longe das respostas, e as linhas de texto ficam longas demais.

**Como corrigir:**

- O alvo principal é **Android em retrato** (390 dp nas referências). Valide em emulador ou aparelho.
- No desktop ou em telas largas (tablet, celular deitado), limite o conteúdo a uma **coluna de no máximo 560 dp, centralizada**. Isso vale para cabeçalho, abas, conversa e compositor. Fora da coluna, só o fundo.
- Mensagens do usuário ocupam no máximo 78% da coluna, não da janela.

**Aceite:** numa janela de 1200 px, a conversa aparece numa coluna central de 560 dp, igual à referência no celular.

## 4. Bloco de dado sensível (P1) — tela 02 e tela 12

**Observado:** um painel grande preenchido, com o botão Revelar e o aviso `SENSÍVEL · PEDE BIOMETRIA` dentro dele.

**Design (tela 02):** sem painel. Numa linha, a máscara sobre `preenchimento` e o botão **Revelar** de contorno ao lado. Embaixo, fora de qualquer caixa, o meta `SENSÍVEL · PEDE BIOMETRIA`, precedido do ícone linear de cadeado em `acao`. Depois de revelar, segue a tela 12: o valor aparece numa superfície com a contagem regressiva.

## 5. Mensagem de conflito (P1) — RF-13, CT-06, tela 10

**Observado:** uma mensagem solta: "aviso: Encontrei mais de uma memória relacionada com conteúdos diferentes; a resposta usa a mais recente, especifique uma data para desambiguar."

**Por que está errado:** escolhe uma das memórias sem perguntar, usa um rótulo técnico ("aviso:") e não mostra quais são as fichas.

**Design:** uma única resposta, sem afirmar nenhuma das duas como verdade:

> Sobre a chave do carro, encontrei **duas fichas diferentes**: gaveta da cozinha (mais recente) e bolso da jaqueta verde. Qual vale?

Em seguida, uma linha de fonte para cada ficha (`FICHA 7C1E · 13/10 · COZINHA ›`). O texto entre parênteses e o rótulo de cada linha vêm do conteúdo das fichas.

## 6. Textos de confirmação (P1)

Use exatamente os textos da tabela em `HANDOFF.md`, seção 5.5. Os principais:

| Situação | Observado | Correto |
|---|---|---|
| `/add` | Memória permanente criada. | Salva como permanente. |
| `/add-sensitive` | Memória permanente e sensível criada. | Salva como permanente e sensível. |
| `/add-temp N` | — | Salva como temporária. Vale até dd/mm, HH:mm. |

## 7. Ritmo vertical e separador de data (P2)

**Observado:** o espaço entre os turnos varia; há um vão grande depois da primeira resposta. Também falta o separador de data.

**Design:**
- 28 dp entre turnos (de uma mensagem do usuário até a resposta seguinte, e vice-versa);
- 6 a 10 dp dentro da resposta (meta → texto → linha de fonte);
- separador `— dd/mm/aaaa —` centralizado, em mono 12, sempre que o dia muda.

## 8. Botão Revelar (P2)

**Observado:** texto em sans e negrito, com ícone.

**Design:** IBM Plex Mono 14, peso 500, contorno de 1 dp na cor `filete`, altura de 44 dp, ícone de olho linear à esquerda.

## 9. Comando `/add-sensitive` (P2)

O comando foi criado na build sem estar no design. **Fica aprovado:** `/add-sensitive <texto>` cria uma memória permanente marcada como sensível. Para isso:

- incluir o comando no autocomplete (tela 03, já atualizada com as três opções);
- reconhecer com `^/add-sensitive\s+(.+)$`;
- memórias temporárias e sensíveis continuam sendo criadas pela folha Nova memória (tipo Temporária + "Marcar como sensível").

## 10. Textura dither no fundo da conversa (P2, novo)

**Decisão de design de 14/10/2026:** a área rolável da conversa ganha uma textura de dither 2×2 na cor `textura` do tema. Veja `HANDOFF.md`, seção 2.3, e as telas 02, 03, 10, 11, 12 e 13 atualizadas.

**Aceite:**

- a textura aparece só atrás das mensagens (não no cabeçalho, nas abas, no compositor nem em Memórias);
- fica parada ao rolar;
- não mostra moiré em densidades 2×, 2,75× e 3×.
