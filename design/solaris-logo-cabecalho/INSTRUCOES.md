# Mudança de design: animação do logo no cabeçalho

**Decisão de 16/10/2026.** O texto "Solaris" no centro do cabeçalho sai. No lugar dele entra a animação da abertura (planeta, horizonte, reflexo ondulando, ondas e estrelas piscando) em versão **transparente e monocromática**, que herda a cor do tema.

## Arquivos

| Arquivo | O que é |
|---|---|
| `logo-animado-transparente.svg` | Referência visual e de tempo (abra no navegador). Usa `currentColor`. |
| `referencia/preview-claro.gif` e `preview-escuro.gif` | Como a animação deve ficar nos dois temas. |
| `referencia/02-chat.png` e `13-chat-escuro.png` | Telas completas com o cabeçalho novo. |

## Onde

Em todas as telas que usam o cabeçalho principal: Conversa, Memórias, Nova memória (por trás da folha) e os estados de erro, vazio e dado sensível, nos temas claro e escuro.

Telas com cabeçalho de voltar (Ajustes, Aparência, Detalhe) **não mudam**: continuam com o título em texto.

O layout do cabeçalho continua o mesmo: altura de 56 dp, VPN à esquerda, engrenagem à direita, logo centralizado.

## Especificação

- **Tamanho:** 48 × 36 dp, numa grade de 32 × 24 pixels (cada pixel tem 1,5 dp).
- **Cor:** uma só, `textoSuave` do tema ativo. Não desenhe fundo: o cabeçalho continua com `fundo`.
- **Opacidades:**

  | Elemento | Opacidade |
  |---|---|
  | Planeta e horizonte | 100% |
  | Estrelas | 70% |
  | Reflexo | 55% |
  | Ondas | 35% |

- **Funciona com qualquer paleta:** como a cor vem do tema, vale para as 5 paletas e para estilos importados sem nenhuma alteração.
- **Pixels nítidos:** arredonde o tamanho do pixel para um número inteiro de pixels de tela e centralize o desenho. Pixel fracionário deixa o desenho borrado e com emendas.
- **Acessibilidade:**
  - o cabeçalho continua anunciado como "Solaris" (`contentDescription`), com `heading()`;
  - o desenho em si é decorativo.
- **Movimento reduzido:** se a escala de animação do Android for 0, mostre só o quadro 0, parado.

### Animação

Um quadro a cada 160 ms. `i` é o número do quadro (0, 1, 2…). Coordenadas na grade 32 × 24; `(x, y, w)` é um retângulo de largura `w` e 1 de altura.

| Camada | Desenho | Movimento |
|---|---|---|
| Estrelas (70%) | Pixels em (4,3) (27,5) (7,9) (24,2) (29,10), `k` = 0..4 | A estrela `k` some quando `(i + 3k) % 6 == 0` |
| Planeta (100%) | Linhas y3 x14 w4 · y4 x12 w8 · y5–y9 x11 w10 · y10 x12 w8 · y11 x14 w4 | Fixo |
| Horizonte (100%) | y14 x0 w32 | Fixo |
| Reflexo (55%) | Linhas `r` = 0..3: y16 x11 w10 · y18 x12 w8 · y20 x14 w4 · y22 x15 w2 | Desloca `dx = round(sin(i/12 · 2π + 1,6r))` |
| Ondas (35%) | y16 x3 w3 (+1) · y18 x24 w4 (−1) · y21 x6 w3 (+1) · y22 x25 w2 (−1) | `x = (x0 + direção · (i / 2)) mod 32`, dando a volta na borda |

Use divisão inteira em `i / 2`.

## Implementação sugerida (Compose)

```kotlin
@Composable
fun SolarisHeaderMark(modifier: Modifier = Modifier) {
    val cor = LocalSolarisColors.current.textoSuave
    val context = LocalContext.current
    val parado = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val quadro by produceState(0, parado) {
        if (!parado) while (true) { delay(160); value = (value + 1) % 192 }
    }

    Canvas(modifier.size(48.dp, 36.dp)) {
        val p = floor(size.width / 32f).coerceAtLeast(1f)          // pixel inteiro
        val ox = ((size.width - 32 * p) / 2f).roundToInt().toFloat()
        val oy = ((size.height - 24 * p) / 2f).roundToInt().toFloat()
        val i = quadro

        fun r(x: Int, y: Int, w: Int, a: Float) {
            for (xx in intArrayOf(x, x - 32, x + 32)) {            // volta na borda
                val x0 = xx.coerceAtLeast(0)
                val x1 = (xx + w).coerceAtMost(32)
                if (x1 > x0) drawRect(
                    cor.copy(alpha = a),
                    Offset(ox + x0 * p, oy + y * p),
                    Size((x1 - x0) * p, p),
                )
            }
        }

        listOf(4 to 3, 27 to 5, 7 to 9, 24 to 2, 29 to 10).forEachIndexed { k, (x, y) ->
            if ((i + k * 3) % 6 != 0) r(x, y, 1, .7f)
        }
        listOf(Triple(3, 14, 4), Triple(4, 12, 8), Triple(5, 11, 10), Triple(6, 11, 10),
               Triple(7, 11, 10), Triple(8, 11, 10), Triple(9, 11, 10), Triple(10, 12, 8),
               Triple(11, 14, 4)).forEach { (y, x, w) -> r(x, y, w, 1f) }
        r(0, 14, 32, 1f)
        listOf(Triple(16, 11, 10), Triple(18, 12, 8), Triple(20, 14, 4), Triple(22, 15, 2))
            .forEachIndexed { k, (y, x, w) ->
                val dx = round(sin(i / 12f * 2f * PI.toFloat() + k * 1.6f)).toInt()
                r(x + dx, y, w, .55f)
            }
        listOf(listOf(16, 3, 3, 1), listOf(18, 24, 4, -1), listOf(21, 6, 3, 1), listOf(22, 25, 2, -1))
            .forEach { (y, x0, w, d) -> r(Math.floorMod(x0 + d * (i / 2), 32), y, w, .35f) }
    }
}
```

No cabeçalho:

```kotlin
Box(
    Modifier.semantics { contentDescription = "Solaris"; heading() },
    contentAlignment = Alignment.Center,
) { SolarisHeaderMark() }
```

## Aceite

1. O logo aparece centralizado no cabeçalho de todas as telas listadas, sem fundo próprio.
2. Na paleta Estação:
   - no tema claro, a cor é `#3F5045`;
   - no escuro, `#C3C6BB`.
3. Ao trocar de paleta em Ajustes › Aparência, o logo muda de cor junto.
4. Os pixels ficam nítidos em 2×, 2,75× e 3×, sem borrão nem emendas.
5. A animação bate com `preview-claro.gif`: o reflexo ondula, as ondas andam e as estrelas piscam.
6. Com animações desativadas no Android, o logo fica parado.
7. O TalkBack lê "Solaris, título".
