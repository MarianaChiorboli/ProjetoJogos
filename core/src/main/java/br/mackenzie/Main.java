    package br.mackenzie;

    import com.badlogic.gdx.ApplicationAdapter;
    import com.badlogic.gdx.Gdx;
    import com.badlogic.gdx.Input;
    import com.badlogic.gdx.audio.Music;
    import com.badlogic.gdx.graphics.Cursor;
    import com.badlogic.gdx.graphics.GL20;
    import com.badlogic.gdx.graphics.Texture;
    import com.badlogic.gdx.graphics.g2d.SpriteBatch;
    import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
    import com.badlogic.gdx.math.MathUtils;
    import com.badlogic.gdx.math.Rectangle;
    import com.badlogic.gdx.math.Vector2;
    import com.badlogic.gdx.utils.viewport.FitViewport;

    public class Main extends ApplicationAdapter {
        private enum Estado { MENU, FASE1, FASE2, FASE3, NIVEL_CONCLUIDO, FIM_DE_JOGO }

        private static final float LARGURA_MUNDO = 8.0f;
        private static final float ALTURA_MUNDO = 4.5f;   // 16:9, igual ao cenário
        // Largura de UM cenário: igual nas 3 fases (mesmo zoom)
        private static final float LARGURA_CENARIO = 12f;
        private static final float SEGUIMENTO_CAMERA = 0.5f;
        private static final float LARGURA_BARRA = 0.5f;
        private static final float ALTURA_BARRA = 3.5f;

        // ---- Fada (compartilhado entre as fases) ----
        private static final float ALTURA_FADA = 1.3f;       // altura da fada no mundo
        private static final float CHAO_Y = 1.65f;           // posição inicial da fada no cenário
        private static final float VELOCIDADE_FADA = 2.0f;   // unidades do mundo por segundo
        private static final float FADA_X_INICIAL = 2.0f;
        private static final float CAMERA_X_INICIAL = 0.5f;
        private static final float CAMERA_Y_INICIAL = 0.75f;
        private static final float DISTANCIA_REGAR = 1.3f;

        // ---- Fase 1 ----
        private static final float FLOR_X = 11.15f;
        private static final float ESCALA_FLOR = 0.8f;

        // ---- Fase 2 ----
        // Altura (no cenário) do chão onde as flores ficam.
        private static final float CHAO_FLORES_FASE2 = CHAO_Y;
        private static final float FLOR1_X_FASE2 = 4.3f;
        private static final float FLOR2_X_FASE2 = 10.7f;
        private static final float LARGURA_IMAGEM_FLOR_FASE2 = 6.9f;
        private static final float ANCORA_X_IMAGEM_FLOR_FASE2 = 0.83f;
        private static final float ANCORA_Y_IMAGEM_FLOR_FASE2 = 0.12f;

        // ---- Fase 3 ----
        // O cenário da fase 3 é 4x mais largo: a imagem se repete 4 vezes (alternando
        // espelhada, para as emendas ficarem invisíveis) SEM aumentar o zoom.
        private static final int REPETICOES_CENARIO_FASE3 = 4;
        private static final int QTD_FLORES_FASE3 = 4;
        private static final float CHAO_FLORES_FASE3 = CHAO_Y;
        // Tamanho da flor na fase 3 (diminua/aumente aqui). Antes era 6.9.
        private static final float LARGURA_IMAGEM_FLOR_FASE3 = 4.8f;
        // Posição de cada flor DENTRO da sua repetição do cenário (0 = borda esquerda da
        // imagem, 1 = borda direita). Escolhidas na faixa de terra, longe das pontes e do rio
        // (as pontes ficam em ~0.13-0.33 e ~0.74-0.91 da largura da imagem).
        private static final float[] FLORES_FASE3_FRACAO = {0.52f, 0.60f, 0.45f, 0.58f};
        // Base da flor dentro das imagens 4..7 (medido nas imagens enviadas)
        private static final float ANCORA_X_IMAGEM_FLOR_FASE3 = 0.825f;
        private static final float ANCORA_Y_IMAGEM_FLOR_FASE3 = 0.08f;

        private static final float DURACAO_TRANSFORMACAO_FLOR = 0.4f;
        // Abaixo disso a força conta como "soltou", liberando a rega da próxima flor
        private static final float LIMITE_SOLTAR_FORCA = 0.2f;

        // ---- Menu / botões ----
        private static final float PLAY_X = 6.02f;
        private static final float PLAY_Y = 0.95f;
        private static final float PLAY_RAIO = 0.28f;
        private static final Rectangle MENU_TEXTO_INICIAR = new Rectangle(4.27f, 1.00f, 1.40f, 0.36f);
        private static final boolean DEBUG_HITBOX = false;
        private static final Rectangle BTN_PAUSAR = new Rectangle(7.04f, 3.81f, 0.56f, 0.48f);
        private static final Rectangle BTN_PAUSA_CONTINUAR = new Rectangle(3.05f, 2.42f, 1.90f, 0.78f);
        private static final Rectangle BTN_PAUSA_REINICIAR = new Rectangle(3.10f, 1.70f, 1.75f, 0.68f);
        private static final Rectangle BTN_PAUSA_MENU = new Rectangle(3.10f, 1.00f, 1.78f, 0.65f);
        private static final Rectangle BTN_PROXIMA_FASE = new Rectangle(2.85f, 1.00f, 2.40f, 0.62f);
        private static final Rectangle BTN_JOGAR_NOVAMENTE = new Rectangle(3.05f, 0.52f, 1.90f, 0.46f);
        // Tela final (ajuste conforme a arte que você criar; use DEBUG_HITBOX para ver)
        private static final Rectangle BTN_FINAL_REINICIAR = new Rectangle(3.05f, 0.68f, 1.90f, 0.48f);
        private static final Rectangle BTN_FINAL_MENU = new Rectangle(2.90f, 1.10f, 2.20f, 0.55f);
        private static final boolean ESCURECER_NA_PAUSA = true;
        private static final float PAUSA_ESCURECIMENTO = 0.45f;

        private final ForceInput entradaForca;
        private Estado estado = Estado.MENU;
        private int faseAtual = 1;
        private boolean pausado = false;
        private boolean nivelConcluido = false;
        private float forcaAtual;
        private Music musicaMenu;
        private Music musicaJardim;
        private Music somPassouFase;
        private Music somRegar;
        private Estado estadoAudioAnterior = null;
        private Cursor.SystemCursor cursorAtual = Cursor.SystemCursor.Arrow;
        private FitViewport viewport;
        private ShapeRenderer desenhador;
        private SpriteBatch lote;
        private final Vector2 toque = new Vector2();

        private Texture texMenu;
        private Texture texCenario;
        private Texture texCenarioFase2;
        private Texture texNivelConcluido;
        private Texture texJardimEncantadoRestaurado;
        private Texture texFlorMorta;
        private Texture texFlorViva;
        private Texture texFlorMortaFase2;
        private Texture texFlorVivaFase2;
        private Texture texFlorMortaYFase2;
        private Texture texFlorVivaYFase2;
        private final Texture[] texFloresMortasFase3 = new Texture[QTD_FLORES_FASE3];
        private final Texture[] texFloresVivasFase3 = new Texture[QTD_FLORES_FASE3];
        private Texture texPausa;
        private Texture texBotaoPausar;
        private Texture texFadaParada;
        private Texture texFadaAndando;
        private Texture texFadaRegando;
        private Texture texFadaVoando1;
        private Texture texFadaVoando2;

        // Estado da fada e da câmera (usado pelas 3 fases)
        private float fadaX = FADA_X_INICIAL;  // centro dos pés da fada no cenário
        private float fadaY = CHAO_Y;
        private float cameraX = CAMERA_X_INICIAL;
        private float cameraY = CAMERA_Y_INICIAL;
        private boolean andando = false;
        private boolean subindo = false;
        private boolean descendo = false;
        private boolean viradaParaDireita = true;
        private float tempoAndando = 0f;

        // Flores das fases 2 e 3 (mesma lógica; só muda a configuração)
        private Texture[] texFloresMortas;
        private Texture[] texFloresVivas;
        private float[] florX;
        private boolean[] florRecuperada;
        private float[] animacaoFlor;
        private float chaoFlores;
        private float larguraImagemFlor;
        private float ancoraXFlor;
        private float ancoraYFlor;
        private boolean aguardandoNovaMedicao;
        private boolean conclusaoPendente;
        // Regra de negócio: a fada só rega se estiver perto de uma flor ainda seca
        private boolean pertoDaFlor = false;

        public Main(ForceInput entradaForca) {
            this.entradaForca = entradaForca;
        }

        @Override
        public void create() {
            viewport = new FitViewport(LARGURA_MUNDO, ALTURA_MUNDO);
            desenhador = new ShapeRenderer();
            lote = new SpriteBatch();
            texMenu = carregar("Menu.jpg");
            texCenario = carregar("CenárioPrincipal.jpg");
            texCenarioFase2 = carregar("Fase2-jardim em recuperação.jpg");
            texNivelConcluido = carregar("Nível concluído.png");
            texJardimEncantadoRestaurado = carregar("Jardim Encantado Restaurado.png");
            texFlorMorta = carregar("FlorMortaLvl1.png");
            texFlorViva = carregar("FlorRegadaLvl1.png");
            texFlorMortaFase2 = carregar("FlorMortaLvl2.png");
            texFlorVivaFase2 = carregar("FlorVivaLvl2.png");
            texFlorMortaYFase2 = carregar("FlorMortaYLvl2.png");
            texFlorVivaYFase2 = carregar("FlorVivaYLvl2.png");
            for (int i = 0; i < QTD_FLORES_FASE3; i++) {
                int numero = i + 4;   // arquivos 4-morta.png ... 7-viva.png
                texFloresMortasFase3[i] = carregar(numero + "-morta.png");
                texFloresVivasFase3[i] = carregar(numero + "-viva.png");
            }
            texPausa = carregar("MenuImplementado.png");
            texBotaoPausar = carregar("Pause.png");
            texFadaParada = carregar("Fada Estatica.png");
            texFadaAndando = carregar("Fada Andando.png");
            texFadaRegando = carregar("Fada Regando.png");
            texFadaVoando1 = carregar("Fada Voandinho1.png");
            texFadaVoando2 = carregar("Fada Voandinho2.png");
            musicaMenu = carregarMusica("menu.mp3", true);
            musicaJardim = carregarMusica("jardim.mp3", true);
            somPassouFase = carregarMusica("passou de fase.mp3", false);
            somRegar = carregarMusica("regar.mp3", true);
        }

        private Music carregarMusica(String arquivo, boolean repetir) {
            Music m = Gdx.audio.newMusic(Gdx.files.internal(arquivo));
            m.setLooping(repetir);
            return m;
        }

        private Texture carregar(String arquivo) {
            Texture t = new Texture(Gdx.files.internal(arquivo));
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return t;
        }

        @Override
        public void resize(int largura, int altura) {
            viewport.update(largura, altura, true);
        }

        // ------------------------------------------------------------------
        // Cenário da fase atual
        // ------------------------------------------------------------------
        private float larguraCenario() {
            return faseAtual == 3 ? LARGURA_CENARIO * REPETICOES_CENARIO_FASE3 : LARGURA_CENARIO;
        }

        private Texture texturaCenario() {
            switch (faseAtual) {
                case 2: return texCenarioFase2;
                case 3: return texCenario;
                default: return texCenario;
            }
        }

        // Altura calculada a partir de UMA repetição (largura 12), mantendo o mesmo zoom
        private float alturaCenario() {
            Texture t = texturaCenario();
            return LARGURA_CENARIO * t.getHeight() / t.getWidth();
        }

        // ------------------------------------------------------------------
        // Movimento da fada + câmera (idêntico nas fases)
        // ------------------------------------------------------------------
        private void atualizarMovimento(float delta) {
            boolean direita = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
            boolean esquerda = Gdx.input.isKeyPressed(Input.Keys.LEFT);
            boolean teclaCima = Gdx.input.isKeyPressed(Input.Keys.UP);
            boolean teclaBaixo = Gdx.input.isKeyPressed(Input.Keys.DOWN);
            andando = direita != esquerda; // só anda se apertar UMA das setas
            subindo = teclaCima != teclaBaixo && teclaCima;
            descendo = teclaCima != teclaBaixo && teclaBaixo;

            float xAnterior = fadaX;
            float yAnterior = fadaY;

            if (andando) {
                viradaParaDireita = direita;
                fadaX += (direita ? 1f : -1f) * VELOCIDADE_FADA * delta;
                tempoAndando += delta;
            } else {
                tempoAndando = 0f;
            }

            if (subindo) {
                fadaY += VELOCIDADE_FADA * delta;
            } else if (descendo) {
                fadaY -= VELOCIDADE_FADA * delta;
            }

            float larguraMapa = larguraCenario();
            float alturaMapa = alturaCenario();
            float alturaMaximaFada = Math.max(alturaDe(texFadaVoando1), alturaDe(texFadaVoando2));
            fadaY = MathUtils.clamp(fadaY, 0f, Math.max(0f, alturaMapa - alturaMaximaFada));

            // usa a maior imagem da fada, para nenhuma delas ultrapassar o cenário
            float meiaLargura = larguraMaximaFada() / 2f;
            fadaX = MathUtils.clamp(fadaX, meiaLargura, larguraMapa - meiaLargura);

            // A câmera segue a fada como antes (metade do deslocamento)...
            cameraX += (fadaX - xAnterior) * SEGUIMENTO_CAMERA;
            cameraY += (fadaY - yAnterior) * SEGUIMENTO_CAMERA;
            // ...mas nunca deixa a imagem da fada sair da tela (necessário em cenários largos)
            cameraX = MathUtils.clamp(cameraX, fadaX - (LARGURA_MUNDO - meiaLargura),
                    fadaX - meiaLargura);
            cameraY = MathUtils.clamp(cameraY, fadaY + alturaMaximaFada - ALTURA_MUNDO, fadaY);
            // e a câmera nunca sai do cenário
            cameraX = MathUtils.clamp(cameraX, 0f, larguraMapa - LARGURA_MUNDO);
            cameraY = MathUtils.clamp(cameraY, 0f, Math.max(0f, alturaMapa - ALTURA_MUNDO));
        }

        private float larguraMaximaFada() {
            float maior = larguraDe(texFadaParada);
            maior = Math.max(maior, larguraDe(texFadaAndando));
            maior = Math.max(maior, larguraDe(texFadaRegando));
            maior = Math.max(maior, larguraDe(texFadaVoando1));
            maior = Math.max(maior, larguraDe(texFadaVoando2));
            return maior;
        }

        private void pararFada() {
            andando = false;
            subindo = false;
            descendo = false;
            tempoAndando = 0f;
        }

        private void resetarFada() {
            fadaX = FADA_X_INICIAL;
            fadaY = CHAO_Y;
            cameraX = CAMERA_X_INICIAL;
            cameraY = CAMERA_Y_INICIAL;
            viradaParaDireita = true;
            pararFada();
        }

        private void atualizarFase1(float delta) {
            atualizarMovimento(delta);
            pertoDaFlor = !nivelConcluido && fadaPertoDaFlor();
        }

        // ------------------------------------------------------------------
        // Fases 2 e 3: regar todas as flores
        // ------------------------------------------------------------------
        private void configurarFlores(int fase) {
            if (fase == 2) {
                texFloresMortas = new Texture[] {texFlorMortaFase2, texFlorMortaYFase2};
                texFloresVivas = new Texture[] {texFlorVivaFase2, texFlorVivaYFase2};
                florX = new float[] {FLOR1_X_FASE2, FLOR2_X_FASE2};
                chaoFlores = CHAO_FLORES_FASE2;
                larguraImagemFlor = LARGURA_IMAGEM_FLOR_FASE2;
                ancoraXFlor = ANCORA_X_IMAGEM_FLOR_FASE2;
                ancoraYFlor = ANCORA_Y_IMAGEM_FLOR_FASE2;
            } else {
                texFloresMortas = texFloresMortasFase3;
                texFloresVivas = texFloresVivasFase3;
                // uma flor por repetição do cenário, sempre sobre a terra (nunca na ponte).
                // As repetições ímpares estão espelhadas, então a fração é invertida nelas.
                florX = new float[QTD_FLORES_FASE3];
                for (int i = 0; i < QTD_FLORES_FASE3; i++) {
                    boolean espelhada = (i % 2) == 1;
                    float fracao = espelhada ? 1f - FLORES_FASE3_FRACAO[i] : FLORES_FASE3_FRACAO[i];
                    florX[i] = i * LARGURA_CENARIO + fracao * LARGURA_CENARIO;
                }
                chaoFlores = CHAO_FLORES_FASE3;
                larguraImagemFlor = LARGURA_IMAGEM_FLOR_FASE3;
                ancoraXFlor = ANCORA_X_IMAGEM_FLOR_FASE3;
                ancoraYFlor = ANCORA_Y_IMAGEM_FLOR_FASE3;
            }
            florRecuperada = new boolean[florX.length];
            animacaoFlor = new float[florX.length];
            for (int i = 0; i < animacaoFlor.length; i++) {
                animacaoFlor[i] = DURACAO_TRANSFORMACAO_FLOR;
            }
        }

        private boolean todasRecuperadas() {
            for (boolean recuperada : florRecuperada) {
                if (!recuperada) {
                    return false;
                }
            }
            return true;
        }

        private boolean animacoesTerminadas() {
            for (float anim : animacaoFlor) {
                if (anim < DURACAO_TRANSFORMACAO_FLOR) {
                    return false;
                }
            }
            return true;
        }

        // Flor mais próxima (em X) ao alcance da fada, ou -1
        private int florAlvo() {
            int alvo = -1;
            float menorDistancia = Float.MAX_VALUE;
            for (int i = 0; i < florX.length; i++) {
                if (florRecuperada[i]) {
                    continue;
                }
                float dx = Math.abs(fadaX - florX[i]);
                float dy = Math.abs(fadaY - chaoFlores);
                if (dx <= DISTANCIA_REGAR && dy <= DISTANCIA_REGAR && dx < menorDistancia) {
                    menorDistancia = dx;
                    alvo = i;
                }
            }
            return alvo;
        }

        private void atualizarFasePlantas(float delta) {
            for (int i = 0; i < animacaoFlor.length; i++) {
                if (animacaoFlor[i] < DURACAO_TRANSFORMACAO_FLOR) {
                    animacaoFlor[i] = Math.min(DURACAO_TRANSFORMACAO_FLOR, animacaoFlor[i] + delta);
                }
            }

            if (conclusaoPendente) {
                // espera a animação da última flor antes de encerrar a fase
                if (animacoesTerminadas()) {
                    concluirFasePlantas();
                }
                return;
            }

            atualizarMovimento(delta);

            // Mesma regra da fase 1: perto da flor + força máxima = rega
            forcaAtual = entradaForca.getForce();

            // Depois de regar uma flor, é preciso soltar a força antes de regar a próxima
            if (aguardandoNovaMedicao && forcaAtual <= LIMITE_SOLTAR_FORCA) {
                aguardandoNovaMedicao = false;
            }

            int alvo = florAlvo();
            pertoDaFlor = alvo >= 0;
            if (!aguardandoNovaMedicao && alvo >= 0 && forcaAtual >= 0.999f) {
                florRecuperada[alvo] = true;
                animacaoFlor[alvo] = 0f;
                aguardandoNovaMedicao = true;
            }

            if (todasRecuperadas()) {
                conclusaoPendente = true;
                pertoDaFlor = false;
                pararFada();
            }
        }

        private void concluirFasePlantas() {
            nivelConcluido = true;
            Gdx.app.log("Fase" + faseAtual, "Nível " + faseAtual + " concluído");
            if (faseAtual == 3) {
                estado = Estado.FIM_DE_JOGO;
            } else {
                estado = Estado.NIVEL_CONCLUIDO;
            }
        }

        // ------------------------------------------------------------------
        // Fada
        // ------------------------------------------------------------------
        // As imagens da fada usam a MESMA escala, para ela não mudar de tamanho ao trocar.
        private float escala() {
            return ALTURA_FADA / texFadaParada.getHeight();
        }

        private float larguraDe(Texture t) {
            return t.getWidth() * escala();
        }

        private float alturaDe(Texture t) {
            return t.getHeight() * escala();
        }

        // Usada por todas as fases
        private void desenharFada() {
            Texture tex;
            if (estaRegando()) {
                tex = texFadaRegando;
            } else if (subindo) {
                tex = texFadaVoando1;
            } else if (descendo) {
                tex = texFadaVoando2;
            } else if (andando) {
                tex = texFadaAndando;
            } else {
                tex = texFadaParada;
            }
            float w = larguraDe(tex);
            float h = alturaDe(tex);

            // Pulinho a cada passo (só enquanto anda)
            float pulo = andando ? Math.abs(MathUtils.sin(tempoAndando * 10f)) * 0.06f : 0f;

            // A imagem "Andando" original olha para a direita; flip quando vai para a esquerda.
            boolean flip = andando && !viradaParaDireita;

            lote.draw(tex,
                    fadaX - cameraX - w / 2f, fadaY - cameraY + pulo, w, h,
                    0, 0, tex.getWidth(), tex.getHeight(),
                    flip, false);
        }

        private boolean estaRegando() {
            return pertoDaFlor && Gdx.input.isKeyPressed(Input.Keys.SPACE);
        }

        // ------------------------------------------------------------------
        // Desenho
        // ------------------------------------------------------------------
        private void desenharBarraForca() {
            desenhador.begin(ShapeRenderer.ShapeType.Filled);
            desenhador.setColor(0.25f, 0.25f, 0.3f, 1.0f);
            desenhador.rect(0.3f, 0.5f, LARGURA_BARRA, ALTURA_BARRA);
            desenhador.setColor(0.4f, 0.85f, 0.55f, 1.0f);
            desenhador.rect(0.3f, 0.5f, LARGURA_BARRA, ALTURA_BARRA * forcaAtual);
            desenhador.end();
        }

        private void desenharPausa() {
            if (!pausado) {
                return;
            }
            if (ESCURECER_NA_PAUSA) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                desenhador.begin(ShapeRenderer.ShapeType.Filled);
                desenhador.setColor(0f, 0f, 0f, PAUSA_ESCURECIMENTO);
                desenhador.rect(0f, 0f, LARGURA_MUNDO, ALTURA_MUNDO);
                desenhador.end();
                Gdx.gl.glDisable(GL20.GL_BLEND);
            }

            lote.begin();
            lote.draw(texPausa, 0, 0, LARGURA_MUNDO, ALTURA_MUNDO);
            lote.end();
        }

        private void desenharHitboxesPausa() {
            desenhador.rect(BTN_PAUSAR.x, BTN_PAUSAR.y, BTN_PAUSAR.width, BTN_PAUSAR.height);
            if (pausado) {
                desenhador.rect(BTN_PAUSA_CONTINUAR.x, BTN_PAUSA_CONTINUAR.y,
                        BTN_PAUSA_CONTINUAR.width, BTN_PAUSA_CONTINUAR.height);
                desenhador.rect(BTN_PAUSA_REINICIAR.x, BTN_PAUSA_REINICIAR.y,
                        BTN_PAUSA_REINICIAR.width, BTN_PAUSA_REINICIAR.height);
                desenhador.rect(BTN_PAUSA_MENU.x, BTN_PAUSA_MENU.y,
                        BTN_PAUSA_MENU.width, BTN_PAUSA_MENU.height);
            }
        }

        // Fase 3: a mesma imagem repetida (alternando espelhada), no mesmo zoom das outras fases
        private void desenharCenarioLongo() {
            float altura = alturaCenario();
            Texture t = texCenario;
            for (int i = 0; i < REPETICOES_CENARIO_FASE3; i++) {
                float x = i * LARGURA_CENARIO - cameraX;
                if (x > LARGURA_MUNDO || x + LARGURA_CENARIO < 0f) {
                    continue;   // fora da tela
                }
                boolean espelhar = (i % 2) == 1;
                // +0.01 de sobra na largura para não aparecer fresta entre as repetições
                lote.draw(t, x, -cameraY, LARGURA_CENARIO + 0.01f, altura,
                        0, 0, t.getWidth(), t.getHeight(), espelhar, false);
            }
        }

        private void desenharFlor(Texture tex, float x, float alpha) {
            float altura = larguraImagemFlor * tex.getHeight() / tex.getWidth();
            // A base da flor (âncora) fica sempre sobre o chão do cenário
            float desenhoX = x - ancoraXFlor * larguraImagemFlor - cameraX;
            float desenhoY = chaoFlores - ancoraYFlor * altura - cameraY;
            if (desenhoX > LARGURA_MUNDO || desenhoX + larguraImagemFlor < 0f) {
                return;     // fora da tela
            }
            lote.setColor(1f, 1f, 1f, alpha);
            lote.draw(tex, desenhoX, desenhoY, larguraImagemFlor, altura);
            lote.setColor(1f, 1f, 1f, 1f);
        }

        private void desenharFasePlantas() {
            lote.begin();
            if (faseAtual == 3) {
                desenharCenarioLongo();
            } else {
                lote.draw(texCenarioFase2, -cameraX, -cameraY, larguraCenario(), alturaCenario());
            }
            for (int i = 0; i < florX.length; i++) {
                boolean viva = florRecuperada[i];
                float alpha = viva ? Math.min(1f, animacaoFlor[i] / DURACAO_TRANSFORMACAO_FLOR) : 1f;
                desenharFlor(viva ? texFloresVivas[i] : texFloresMortas[i], florX[i], alpha);
            }
            desenharFada();
            lote.draw(texBotaoPausar, 0, 0, LARGURA_MUNDO, ALTURA_MUNDO);
            lote.end();

            desenharBarraForca();

            if (DEBUG_HITBOX) {
                desenhador.begin(ShapeRenderer.ShapeType.Line);
                desenhador.setColor(1f, 1f, 1f, 1f);
                desenharHitboxesPausa();
                // linha do chão das flores e pontos de âncora
                desenhador.line(0f, chaoFlores - cameraY, LARGURA_MUNDO, chaoFlores - cameraY);
                for (float x : florX) {
                    desenhador.circle(x - cameraX, chaoFlores - cameraY, 0.08f);
                }
                desenhador.end();
            }

            desenharPausa();
        }

        private void desenharFase1() {
            float alturaCenario = alturaCenario();
            float larguraFlor = LARGURA_MUNDO * ESCALA_FLOR;
            float alturaFlor = ALTURA_MUNDO * ESCALA_FLOR;
            float xFlor = FLOR_X - larguraFlor / 2f - cameraX;
            float yFlor = CAMERA_Y_INICIAL + CHAO_Y * (1f - ESCALA_FLOR) - cameraY;

            lote.begin();
            lote.draw(texCenario, -cameraX, -cameraY, larguraCenario(), alturaCenario);
            lote.draw(nivelConcluido ? texFlorViva : texFlorMorta,
                    xFlor, yFlor, larguraFlor, alturaFlor);
            desenharFada();
            lote.draw(texBotaoPausar, 0, 0, LARGURA_MUNDO, ALTURA_MUNDO);
            lote.end();

            // Barra de força (teste do sensor), por cima do cenário
            desenharBarraForca();

            if (DEBUG_HITBOX) {
                desenhador.begin(ShapeRenderer.ShapeType.Line);
                desenhador.setColor(1f, 1f, 1f, 1f);
                desenharHitboxesPausa();
                desenhador.line(FLOR_X - DISTANCIA_REGAR - cameraX, 0f,
                        FLOR_X - DISTANCIA_REGAR - cameraX, ALTURA_MUNDO);
                desenhador.line(FLOR_X + DISTANCIA_REGAR - cameraX, 0f,
                        FLOR_X + DISTANCIA_REGAR - cameraX, ALTURA_MUNDO);
                desenhador.end();
            }

            desenharPausa();
        }

        private boolean fadaPertoDaFlor() {
            return Math.abs(fadaX - FLOR_X) <= DISTANCIA_REGAR
                    && Math.abs(fadaY - CHAO_Y) <= DISTANCIA_REGAR;
        }

        private void verificarRega() {
            if (!nivelConcluido && forcaAtual >= 0.999f && pertoDaFlor) {
                nivelConcluido = true;
                estado = Estado.NIVEL_CONCLUIDO;
                pararFada();
                Gdx.app.log("Fase1", "Nivel 1 concluido");
            }
        }

        // ------------------------------------------------------------------
        // Telas: nível concluído e final
        // ------------------------------------------------------------------
        private void desenharNivelConcluido() {
            viewport.unproject(toque.set(Gdx.input.getX(), Gdx.input.getY()));
            boolean sobreProximaFase = BTN_PROXIMA_FASE.contains(toque);
            boolean sobreJogarNovamente = BTN_JOGAR_NOVAMENTE.contains(toque);
            atualizarCursor(sobreProximaFase || sobreJogarNovamente
                    ? Cursor.SystemCursor.Hand : Cursor.SystemCursor.Arrow);

            if (Gdx.input.justTouched()) {
                if (sobreJogarNovamente) {
                    reiniciarFaseAtual();
                    return;
                } else if (sobreProximaFase) {
                    if (faseAtual == 1) {
                        iniciarFase2();
                    } else {
                        iniciarFase3();
                    }
                    return;
                }
            }

            lote.begin();
            lote.draw(texNivelConcluido, 0, 0, LARGURA_MUNDO, ALTURA_MUNDO);
            lote.end();
        }

        private void desenharTelaFinal() {
            viewport.unproject(toque.set(Gdx.input.getX(), Gdx.input.getY()));
            boolean sobreReiniciar = BTN_FINAL_REINICIAR.contains(toque);
            boolean sobreMenu = BTN_FINAL_MENU.contains(toque);
            atualizarCursor(sobreReiniciar || sobreMenu
                    ? Cursor.SystemCursor.Hand : Cursor.SystemCursor.Arrow);

            if (Gdx.input.justTouched()) {
                if (sobreReiniciar) {
                    iniciarFase1();      // joga tudo de novo, desde a fase 1
                    return;
                } else if (sobreMenu) {
                    voltarAoMenu();
                    return;
                }
            }

            lote.begin();
            lote.draw(texJardimEncantadoRestaurado, 0, 0, LARGURA_MUNDO, ALTURA_MUNDO);
            lote.end();

            if (DEBUG_HITBOX) {
                desenhador.begin(ShapeRenderer.ShapeType.Line);
                desenhador.setColor(1f, 1f, 1f, 1f);
                desenhador.rect(BTN_FINAL_REINICIAR.x, BTN_FINAL_REINICIAR.y,
                        BTN_FINAL_REINICIAR.width, BTN_FINAL_REINICIAR.height);
                desenhador.rect(BTN_FINAL_MENU.x, BTN_FINAL_MENU.y,
                        BTN_FINAL_MENU.width, BTN_FINAL_MENU.height);
                desenhador.end();
            }
        }

        // ------------------------------------------------------------------
        // Início / reinício de fases
        // ------------------------------------------------------------------
        private void reiniciarFaseAtual() {
            switch (faseAtual) {
                case 2: iniciarFase2(); break;
                case 3: iniciarFase3(); break;
                default: iniciarFase1(); break;
            }
        }

        private void prepararNovaFase(int fase, Estado novoEstado) {
            estado = novoEstado;
            faseAtual = fase;
            atualizarCursor(Cursor.SystemCursor.Arrow);
            pausado = false;
            entradaForca.reset();
            nivelConcluido = false;
            forcaAtual = 0f;
            pertoDaFlor = false;
            resetarFada();
            musicaJardim.stop();
            somRegar.stop();
        }

        private void iniciarFase1() {
            prepararNovaFase(1, Estado.FASE1);
        }

        private void iniciarFase2() {
            prepararNovaFase(2, Estado.FASE2);
            iniciarPlantas(2);
        }

        private void iniciarFase3() {
            prepararNovaFase(3, Estado.FASE3);
            iniciarPlantas(3);
        }

        private void iniciarPlantas(int fase) {
            configurarFlores(fase);
            aguardandoNovaMedicao = false;
            conclusaoPendente = false;
        }

        private Rectangle botaoSobMouse() {
            if (pausado && BTN_PAUSA_CONTINUAR.contains(toque)) {
                return BTN_PAUSA_CONTINUAR;
            } else if (pausado && BTN_PAUSA_REINICIAR.contains(toque)) {
                return BTN_PAUSA_REINICIAR;
            } else if (pausado && BTN_PAUSA_MENU.contains(toque)) {
                return BTN_PAUSA_MENU;
            } else if (!pausado && BTN_PAUSAR.contains(toque)) {
                return BTN_PAUSAR;
            }
            return null;
        }

        private void voltarAoMenu() {
            estado = Estado.MENU;
            pausado = false;
            atualizarCursor(Cursor.SystemCursor.Arrow);
        }

        private void atualizarCursor(Cursor.SystemCursor cursor) {
            if (cursorAtual != cursor) {
                Gdx.graphics.setSystemCursor(cursor);
                cursorAtual = cursor;
            }
        }

        private void atualizarEntradaFase() {
            viewport.unproject(toque.set(Gdx.input.getX(), Gdx.input.getY()));
            Rectangle botao = botaoSobMouse();
            atualizarCursor(botao != null ? Cursor.SystemCursor.Hand : Cursor.SystemCursor.Arrow);

            if (Gdx.input.justTouched() && botao != null) {
                if (botao == BTN_PAUSAR) {
                    pausado = true;
                } else if (botao == BTN_PAUSA_CONTINUAR) {
                    pausado = false;
                } else if (botao == BTN_PAUSA_REINICIAR) {
                    iniciarFase1();
                } else if (botao == BTN_PAUSA_MENU) {
                    voltarAoMenu();
                }
            }
        }

        private void desenharMenu() {
            viewport.unproject(toque.set(Gdx.input.getX(), Gdx.input.getY()));
            boolean sobrePlay = toque.dst(PLAY_X, PLAY_Y) <= PLAY_RAIO || MENU_TEXTO_INICIAR.contains(toque);
            atualizarCursor(sobrePlay ? Cursor.SystemCursor.Hand : Cursor.SystemCursor.Arrow);

            if (Gdx.input.justTouched() && sobrePlay) {
                iniciarFase1();
                return;
            }

            lote.begin();
            lote.draw(texMenu, 0, 0, LARGURA_MUNDO, ALTURA_MUNDO);
            lote.end();

            if (sobrePlay) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                desenhador.begin(ShapeRenderer.ShapeType.Filled);
                desenhador.setColor(1f, 0.84f, 0f, 0.25f);
                desenhador.circle(PLAY_X, PLAY_Y, PLAY_RAIO);
                desenhador.end();
                Gdx.gl.glDisable(GL20.GL_BLEND);
            }

            if (DEBUG_HITBOX) {
                desenhador.begin(ShapeRenderer.ShapeType.Line);
                desenhador.setColor(1f, 1f, 1f, 1f);
                desenhador.circle(PLAY_X, PLAY_Y, PLAY_RAIO);
                desenhador.rect(MENU_TEXTO_INICIAR.x, MENU_TEXTO_INICIAR.y,
                        MENU_TEXTO_INICIAR.width, MENU_TEXTO_INICIAR.height);
                desenhador.end();
            }
        }

        @Override
        public void render() {
            Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1.0f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            viewport.apply();
            lote.setProjectionMatrix(viewport.getCamera().combined);
            desenhador.setProjectionMatrix(viewport.getCamera().combined);

            switch (estado) {
                case MENU:
                    desenharMenu();
                    break;
                case FASE1: {
                    boolean estavaPausado = pausado;
                    atualizarEntradaFase();
                    if (estado == Estado.MENU) {
                        break;
                    }
                    if (!estavaPausado && !pausado && !nivelConcluido) {
                        atualizarFase1(Gdx.graphics.getDeltaTime());
                        forcaAtual = entradaForca.getForce();
                        verificarRega();
                    }
                    if (estado == Estado.NIVEL_CONCLUIDO) {
                        desenharNivelConcluido();
                    } else {
                        desenharFase1();
                    }
                    break;
                }
                case FASE2:
                case FASE3: {
                    boolean estavaPausado = pausado;
                    atualizarEntradaFase();
                    if (estado == Estado.MENU) {
                        break;
                    }
                    if (!estavaPausado && !pausado) {
                        atualizarFasePlantas(Gdx.graphics.getDeltaTime());
                    }
                    if (estado == Estado.NIVEL_CONCLUIDO) {
                        desenharNivelConcluido();
                    } else if (estado == Estado.FIM_DE_JOGO) {
                        desenharTelaFinal();
                    } else {
                        desenharFasePlantas();
                    }
                    break;
                }
                case NIVEL_CONCLUIDO:
                    desenharNivelConcluido();
                    break;
                case FIM_DE_JOGO:
                    desenharTelaFinal();
                    break;
            }
            atualizarAudio();
        }

        private void atualizarAudio() {
            boolean emFase = estado == Estado.FASE1 || estado == Estado.FASE2 || estado == Estado.FASE3;
            boolean telaConclusao = estado == Estado.NIVEL_CONCLUIDO || estado == Estado.FIM_DE_JOGO;
            boolean tocarMenu = estado == Estado.MENU || (emFase && pausado);
            boolean tocarJardim = emFase && !pausado;

            if (tocarMenu && !musicaMenu.isPlaying()) {
                musicaMenu.play();
            } else if (!tocarMenu && musicaMenu.isPlaying()) {
                musicaMenu.stop();
            }

            if (tocarJardim && !musicaJardim.isPlaying()) {
                musicaJardim.play();
            } else if (!tocarJardim && musicaJardim.isPlaying()) {
                musicaJardim.pause();
            }

            if (telaConclusao && estado != estadoAudioAnterior) {
                somPassouFase.stop();
                somPassouFase.play();
            } else if (!telaConclusao && somPassouFase.isPlaying()) {
                somPassouFase.stop();
            }

            boolean regando = tocarJardim && estaRegando();
            if (regando && !somRegar.isPlaying()) {
                somRegar.play();
            } else if (!regando && somRegar.isPlaying()) {
                somRegar.stop();
            }

            estadoAudioAnterior = estado;
        }

        @Override
        public void dispose() {
            musicaMenu.dispose();
            musicaJardim.dispose();
            somPassouFase.dispose();
            somRegar.dispose();
            desenhador.dispose();
            lote.dispose();
            texMenu.dispose();
            texCenario.dispose();
            texCenarioFase2.dispose();
            texNivelConcluido.dispose();
            texJardimEncantadoRestaurado.dispose();
            texFlorMorta.dispose();
            texFlorViva.dispose();
            texFlorMortaFase2.dispose();
            texFlorVivaFase2.dispose();
            texFlorMortaYFase2.dispose();
            texFlorVivaYFase2.dispose();
            for (int i = 0; i < QTD_FLORES_FASE3; i++) {
                texFloresMortasFase3[i].dispose();
                texFloresVivasFase3[i].dispose();
            }
            texPausa.dispose();
            texBotaoPausar.dispose();
            texFadaParada.dispose();
            texFadaAndando.dispose();
            texFadaRegando.dispose();
            texFadaVoando1.dispose();
            texFadaVoando2.dispose();
        }
    }