    package br.mackenzie;

    import com.badlogic.gdx.ApplicationAdapter;
    import com.badlogic.gdx.Gdx;
    import com.badlogic.gdx.audio.Music;
    import com.badlogic.gdx.graphics.Cursor;
    import com.badlogic.gdx.graphics.GL20;
    import com.badlogic.gdx.graphics.Texture;
    import com.badlogic.gdx.graphics.g2d.SpriteBatch;
    import com.badlogic.gdx.graphics.g2d.TextureRegion;
    import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
    import com.badlogic.gdx.math.MathUtils;
    import com.badlogic.gdx.math.Rectangle;
    import com.badlogic.gdx.math.Vector2;
    import com.badlogic.gdx.utils.viewport.FitViewport;

    public class Main extends ApplicationAdapter {
        private enum Estado { MENU, FASE1, FASE2, FASE3, NIVEL_CONCLUIDO, FIM_DE_JOGO, RESULTADOS }

        private static final float LARGURA_MUNDO = 8.0f;
        private static final float ALTURA_MUNDO = 4.5f;   // 16:9, igual ao cenário
        // Largura de UMA imagem de cenário (mesmo zoom nas 3 fases). O cenário se repete
        // sem fim, alternando normal/espelhado, para as emendas ficarem invisíveis.
        private static final float LARGURA_CENARIO = 12f;
        // A câmera fica parada na vertical; só o cenário anda na horizontal.
        private static final float CAMERA_Y = 0.75f;
        private static final float LARGURA_BARRA = 0.5f;
        private static final float ALTURA_BARRA = 3.3f;   // um pouco mais baixa, para não encostar no tempo

        // ==================================================================
        // CONTROLE PELA FORÇA (sensor / ESPAÇO)
        // A força (0 a 1) define a ALTURA da Lumi. O cenário anda sozinho.
        // ==================================================================
        private static final float ALTURA_FADA = 1.3f;          // altura da imagem da fada no mundo
        private static final float FADA_X_TELA = 1.9f;          // Lumi fica sempre nesta posição horizontal
        // Altura do CORPO da Lumi na tela: força 0 = Y_FADA_MIN (perto do chão), força 1 = Y_FADA_MAX
        private static final float Y_FADA_MIN = 1.2f;
        private static final float Y_FADA_MAX = 3.4f;
        private static final float SUAVIZACAO_FADA = 8f;        // maior = Lumi segue a força mais rápido
        private static final float LIMIAR_SUBINDO = 0.15f;      // velocidade vertical para trocar o sprite
        // Posição do corpo dentro de cada imagem da fada (medido nas imagens; 0..1 a partir da esquerda/baixo).
        // Serve para a Lumi não "pular" de lugar quando troca de sprite.
        private static final float[] CENTRO_X_SPRITE = {0.494f, 0.298f, 0.452f, 0.423f}; // parada, voando1, voando2, regando
        private static final float[] CENTRO_Y_SPRITE = {0.521f, 0.493f, 0.494f, 0.452f};

        // ---- Flores e luzes mágicas ----
        private static final float CHAO_FLORES = 1.65f;         // chão das flores (coordenada do cenário)
        private static final float PRIMEIRA_FLOR_X_TELA = 5.0f; // onde a 1ª flor aparece ao começar a fase
        private static final float ESPACO_ENTRE_FLORES = 3.5f;  // distância entre uma flor e a próxima
        private static final float TAMANHO_LUZ = 1.0f;          // tamanho da imagem da luz (fases 1 e 2)
        private static final float TOLERANCIA_ALTURA = 0.30f;   // quão perto (na vertical) a Lumi precisa estar da luz
        private static final float ALCANCE_X = 0.6f;            // janela horizontal em que a Lumi alcança a luz
        private static final float ALCANCE_X_DESLOCAMENTO = 0.25f; // a mão da Lumi fica um pouco à direita do corpo
        private static final float FATOR_VELOCIDADE_REGANDO = 0.2f;  // o cenário desacelera enquanto ela rega
        private static final float DECAIMENTO_PROGRESSO = 0.5f; // ao sair da luz, o progresso cai devagar
        private static final float DISTANCIA_SAIDA_TELA = 0.7f; // flor passou deste ponto à esquerda = "volta mais tarde"
        private static final float MARGEM_RETORNO = 0.8f;       // a flor que volta reaparece logo depois da borda direita
        private static final float DURACAO_TRANSFORMACAO_FLOR = 0.4f;

        // ---- Configuração de cada fase (índice 0 = fase 1) — Tabela 2 do artigo ----
        // Altura de cada luz em "força" (0 = chão, 1 = força máxima). Uma luz por flor.
        private static final float[][] ALTURAS_LUZ = {
                {0.5f, 0.5f, 0.5f},                        // fase 1: mesma altura -> força constante
                {0.30f, 0.70f, 0.45f, 0.80f},              // fase 2: alturas diferentes -> ajuste da força
                {0.60f, 0.25f, 0.80f, 0.40f, 0.70f},       // fase 3: alturas diferentes, mais rápido, luz menor
        };
        private static final float[] TEMPO_DESPERTAR = {1.5f, 1.8f, 2.0f};   // segundos tocando a luz
        private static final float[] VELOCIDADE_CENARIO = {0.6f, 0.6f, 0.75f}; // unidades do mundo por segundo
        private static final float[] ESCALA_LUZ = {1.0f, 1.0f, 0.7f};          // fase 3: luz 30% menor

        // Desenho das imagens de flor de cada fase (as imagens ocupam a tela toda; a âncora é a base da flor)
        private static final float[] LARGURA_IMAGEM_FLOR = {6.4f, 6.9f, 4.8f};
        private static final float[] ANCORA_X_FLOR = {0.4837f, 0.83f, 0.825f};
        private static final float[] ANCORA_Y_FLOR = {0.16f, 0.12f, 0.08f};
        private static final int QTD_FLORES_FASE3 = 4;   // imagens 4..7

        // ---- Menu / botões ----
        private static final float PLAY_X = 6.02f;
        private static final float PLAY_Y = 0.95f;
        private static final float PLAY_RAIO = 0.28f;
        private static final Rectangle MENU_TEXTO_INICIAR = new Rectangle(4.27f, 1.00f, 1.40f, 0.36f);
        private static final Rectangle BTN_SOM = new Rectangle(7.06f, 3.68f, 0.56f, 0.48f);
        private static final boolean DEBUG_HITBOX = false;
        private static final Rectangle BTN_PAUSAR = new Rectangle(7.04f, 3.81f, 0.56f, 0.48f);
        private static final Rectangle BTN_PAUSA_CONTINUAR = new Rectangle(3.05f, 2.42f, 1.90f, 0.78f);
        private static final Rectangle BTN_PAUSA_REINICIAR = new Rectangle(3.10f, 1.70f, 1.75f, 0.68f);
        private static final Rectangle BTN_PAUSA_MENU = new Rectangle(3.10f, 1.00f, 1.78f, 0.65f);
        private static final Rectangle BTN_PROXIMA_FASE = new Rectangle(2.85f, 1.00f, 2.40f, 0.62f);
        private static final Rectangle BTN_JOGAR_NOVAMENTE = new Rectangle(3.05f, 0.52f, 1.90f, 0.46f);
        // Tela final (ajuste conforme a arte que você criar; use DEBUG_HITBOX para ver)
        // Tela "Você restaurou o jardim!" (3 botões)
        private static final Rectangle BTN_FINAL_RESULTADOS = new Rectangle(3.05f, 1.07f, 1.88f, 0.39f);
        private static final Rectangle BTN_FINAL_REINICIAR = new Rectangle(2.98f, 0.60f, 0.98f, 0.35f);
        private static final Rectangle BTN_FINAL_MENU = new Rectangle(4.03f, 0.60f, 0.98f, 0.35f);
        // Tela de resultados
        private static final Rectangle BTN_RESULTADOS_MENU = new Rectangle(2.40f, 0.37f, 1.50f, 0.36f);
        private static final Rectangle BTN_RESULTADOS_JOGAR = new Rectangle(4.10f, 0.37f, 1.50f, 0.36f);
        private static final boolean ESCURECER_NA_PAUSA = true;
        private static final float PAUSA_ESCURECIMENTO = 0.45f;

        // ---- Contador de flores "regadas / total" (topo central, só nas fases) ----
        private static final float PAINEL_CONTADOR_W = 1.5625f;
        private static final float PAINEL_CONTADOR_H = 0.6375f;
        private static final float PAINEL_CONTADOR_X = (LARGURA_MUNDO - PAINEL_CONTADOR_W) / 2f;
        private static final float PAINEL_CONTADOR_Y = 3.84f;
        private static final float TEXTO_CONTADOR_CENTRO_X = 1.0f;  // centro do texto, medido da borda esquerda do painel
        private static final float ALTURA_NUMERO = 0.3625f;
        private static final float ESPACAMENTO_NUMERO = 0.9f;       // 1 = células encostadas; menor = mais juntas
        private static final String CARACTERES_CONTADOR = "0123456789/:,%s";

        // ==================================================================
        // MÉTRICAS (seção 3.4 do artigo) e ESTRELAS
        // ==================================================================
        // Firmeza = 1 - (desvio padrão da força enquanto rega / SIGMA_REFERENCIA_FIRMEZA), entre 0 e 1.
        // Desvio igual ou maior que a referência = firmeza 0%.
        private static final float SIGMA_REFERENCIA_FIRMEZA = 0.10f;
        // Estrelas: 1 por terminar + 1 se nenhuma flor voltou + 1 se firmeza >= este valor
        private static final float FIRMEZA_PARA_ESTRELA = 0.70f;
        // Frases de incentivo: melhora mínima (em relação à fase 1) para a frase aparecer
        private static final float MELHORA_MINIMA_FIRMEZA = 0.05f;   // 5 pontos percentuais
        private static final float MELHORA_MINIMA_CHEGADA = 0.3f;    // segundos

        // ---- Layout da tela de resultados (coordenadas do mundo, medidas na arte) ----
        private static final float RESULTADO_COLUNA_X = 1.40f;       // borda esquerda da coluna da fase 1
        private static final float RESULTADO_COLUNA_PASSO = 1.775f;  // distância entre colunas
        private static final float RESULTADO_COLUNA_W = 1.65f;
        private static final float RESULTADO_ESTRELAS_Y = 2.725f;    // centro das estrelas
        private static final float RESULTADO_ESTRELA_TAM = 0.33f;
        private static final float RESULTADO_ESTRELA_PASSO = 0.35f;
        private static final float RESULTADO_LINHA1_Y = 2.35f;       // centro da 1ª linha (Tempo)
        private static final float RESULTADO_LINHA_PASSO = 0.31f;
        private static final float RESULTADO_VALOR_MARGEM = 0.14f;   // distância do valor até a borda direita
        private static final float RESULTADO_ALTURA_NUMERO = 0.24f;
        private static final float RESULTADO_FRASE_Y = 0.89f;
        private static final float RESULTADO_FRASE_W = 5.0f;
        private static final float RESULTADO_FRASE_H = 0.30f;

        // ---- Tempo da fase (canto superior esquerdo, só nas fases) ----
        private static final float PAINEL_TEMPO_X = 0.0625f;
        private static final float PAINEL_TEMPO_Y = 3.9f;
        private static final float PAINEL_TEMPO_W = 1.3125f;
        private static final float PAINEL_TEMPO_H = 0.575f;
        private static final float TEXTO_TEMPO_CENTRO_X = 0.85f;    // centro do texto, medido da borda esquerda do painel
        private static final float ALTURA_NUMERO_TEMPO = 0.30f;
        private static final float ESPACAMENTO_NUMERO_TEMPO = 0.75f;

        private final ForceInput entradaForca;
        private Estado estado = Estado.MENU;
        private int faseAtual = 1;
        private boolean pausado = false;
        private boolean nivelConcluido = false;
        private boolean somMutado = false;
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
        private Texture texSomLigado;
        private Texture texSomDesligado;
        private Texture texPainelContador;
        private Texture texPainelTempo;
        private Texture texResultadosFundo;
        private Texture texEstrelaCheia;
        private Texture texEstrelaVazia;
        private Texture texFraseFirmeza2, texFraseFirmeza3, texFraseChegada2, texFraseChegada3;
        private Texture texFrasePrimeira, texFraseGeral;
        private Texture texNumerosContador;
        private final TextureRegion[] regNumeros = new TextureRegion[15];   // 0-9 / : , % s
        private Texture texLuzMagica;
        private Texture texFadaParada;
        private Texture texFadaRegando;
        private Texture texFadaVoando1;
        private Texture texFadaVoando2;

        // ---- Estado da Lumi ----
        private float fadaY = Y_FADA_MIN;   // altura do CORPO da Lumi na tela
        private boolean subindo = false;
        private boolean descendo = false;
        private float tempoVoo = 0f;        // usado para a Lumi "flutuar" de leve
        // Lumi está tocando a luz de uma flor seca (= regando)
        private boolean tocandoLuz = false;

        // ---- Cenário rolando ----
        private float deslocamento = 0f;    // quanto o cenário já andou (unidades do mundo)
        private float tempoFase = 0f;       // segundos jogados na fase (para na pausa)

        // ---- Flores da fase atual ----
        private Texture[] texFloresMortas;  // uma imagem por flor
        private Texture[] texFloresVivas;
        private float[] florX;              // posição da flor no cenário
        private float[] alturaLuz;          // altura da luz em "força" (0..1)
        private float[] progressoFlor;      // 0..1 enquanto a Lumi rega
        private int[] tentativasFlor;       // quantas vezes a flor precisou passar (base para as métricas)
        private boolean[] florRecuperada;
        private float[] animacaoFlor;
        private boolean conclusaoPendente;

        // ---- Métricas de cada flor (da passada em que ela despertou) ----
        private float[] inicioPassada;      // tempoFase quando a luz entrou na tela (-1 = ainda não)
        private float[] chegadaPassada;     // segundos até a Lumi tocar a luz nesta passada (-1 = ainda não)
        private float[] chegadaFlor;        // chegada da passada em que a flor despertou
        private float[] firmezaFlor;        // 0..1
        private double[] somaForca;
        private double[] somaForcaQuadrado;
        private int[] amostrasForca;

        // ---- Resultado de cada fase (índice 0 = fase 1) ----
        private final boolean[] resultadoValido = new boolean[3];
        private final float[] resultadoTempo = new float[3];
        private final float[] resultadoChegada = new float[3];
        private final float[] resultadoFirmeza = new float[3];
        private final int[] resultadoVoltas = new int[3];
        private final int[] resultadoEstrelas = new int[3];

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
            texLuzMagica = carregar("LuzMagica.png");
            texFadaParada = carregar("Fada Estatica.png");
            texFadaRegando = carregar("Fada Regando.png");
            texFadaVoando1 = carregar("Fada Voandinho1.png");
            texFadaVoando2 = carregar("Fada Voandinho2.png");
            texSomLigado = carregar("SomLigado.png");
            texSomDesligado = carregar("SomDesligado.png");
            texPainelContador = carregar("PainelContador.png");
            texPainelTempo = carregar("PainelTempo.png");
            texResultadosFundo = carregar("resultados/ResultadosFundo.jpg");
            texEstrelaCheia = carregar("resultados/EstrelaCheia.png");
            texEstrelaVazia = carregar("resultados/EstrelaVazia.png");
            texFraseFirmeza2 = carregar("resultados/FraseFirmeza2.png");
            texFraseFirmeza3 = carregar("resultados/FraseFirmeza3.png");
            texFraseChegada2 = carregar("resultados/FraseChegada2.png");
            texFraseChegada3 = carregar("resultados/FraseChegada3.png");
            texFrasePrimeira = carregar("resultados/FrasePrimeira.png");
            texFraseGeral = carregar("resultados/FraseGeral.png");
            texNumerosContador = carregar("NumerosContador.png");
            // A imagem tem 15 células do mesmo tamanho: 0 1 2 3 4 5 6 7 8 9 / : , % s
            int larguraCelula = texNumerosContador.getWidth() / regNumeros.length;
            for (int i = 0; i < regNumeros.length; i++) {
                regNumeros[i] = new TextureRegion(texNumerosContador, i * larguraCelula, 0,
                        larguraCelula, texNumerosContador.getHeight());
            }
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
        private int indiceFase() {
            return MathUtils.clamp(faseAtual - 1, 0, 2);
        }

        private Texture texturaCenario() {
            return faseAtual == 2 ? texCenarioFase2 : texCenario;
        }

        // Altura de uma imagem de cenário com largura LARGURA_CENARIO (mesmo zoom em todas as fases)
        private float alturaCenario() {
            Texture t = texturaCenario();
            return LARGURA_CENARIO * t.getHeight() / t.getWidth();
        }

        // Converte uma força (0..1) na altura do corpo da Lumi na tela
        private float alturaNaTela(float forca) {
            return Y_FADA_MIN + forca * (Y_FADA_MAX - Y_FADA_MIN);
        }

        // ------------------------------------------------------------------
        // Lumi: a altura vem da força
        // ------------------------------------------------------------------
        private void atualizarLumi(float delta) {
            float alvo = alturaNaTela(forcaAtual);
            float anterior = fadaY;
            fadaY += (alvo - fadaY) * Math.min(1f, SUAVIZACAO_FADA * delta);
            float velocidade = delta > 0f ? (fadaY - anterior) / delta : 0f;
            subindo = velocidade > LIMIAR_SUBINDO;
            descendo = velocidade < -LIMIAR_SUBINDO;
            tempoVoo += delta;
        }

        // ------------------------------------------------------------------
        // Flores: chegam pela direita, cada uma com uma luz mágica na altura certa
        // ------------------------------------------------------------------
        private void configurarFlores(int fase) {
            int indice = fase - 1;
            float[] alturas = ALTURAS_LUZ[indice];
            int quantidade = alturas.length;

            texFloresMortas = new Texture[quantidade];
            texFloresVivas = new Texture[quantidade];
            for (int i = 0; i < quantidade; i++) {
                if (fase == 1) {
                    texFloresMortas[i] = texFlorMorta;
                    texFloresVivas[i] = texFlorViva;
                } else if (fase == 2) {
                    boolean amarela = (i % 2) == 1;   // alterna os dois tipos de flor da fase 2
                    texFloresMortas[i] = amarela ? texFlorMortaYFase2 : texFlorMortaFase2;
                    texFloresVivas[i] = amarela ? texFlorVivaYFase2 : texFlorVivaFase2;
                } else {
                    texFloresMortas[i] = texFloresMortasFase3[i % QTD_FLORES_FASE3];
                    texFloresVivas[i] = texFloresVivasFase3[i % QTD_FLORES_FASE3];
                }
            }

            florX = new float[quantidade];
            alturaLuz = alturas.clone();
            progressoFlor = new float[quantidade];
            tentativasFlor = new int[quantidade];
            florRecuperada = new boolean[quantidade];
            animacaoFlor = new float[quantidade];
            inicioPassada = new float[quantidade];
            chegadaPassada = new float[quantidade];
            chegadaFlor = new float[quantidade];
            firmezaFlor = new float[quantidade];
            somaForca = new double[quantidade];
            somaForcaQuadrado = new double[quantidade];
            amostrasForca = new int[quantidade];
            for (int i = 0; i < quantidade; i++) {
                inicioPassada[i] = -1f;
                chegadaPassada[i] = -1f;
                florX[i] = PRIMEIRA_FLOR_X_TELA + i * ESPACO_ENTRE_FLORES;
                tentativasFlor[i] = 1;
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

        private int floresRecuperadas() {
            int total = 0;
            for (boolean recuperada : florRecuperada) {
                if (recuperada) total++;
            }
            return total;
        }

        private boolean animacoesTerminadas() {
            for (float anim : animacaoFlor) {
                if (anim < DURACAO_TRANSFORMACAO_FLOR) {
                    return false;
                }
            }
            return true;
        }

        private float luzXNaTela(int i) {
            return florX[i] - deslocamento;
        }

        private float luzYNaTela(int i) {
            return alturaNaTela(alturaLuz[i]);
        }

        private float tamanhoLuz() {
            return TAMANHO_LUZ * ESCALA_LUZ[indiceFase()];
        }

        // A Lumi alcança a luz da flor i? (perto na horizontal E na altura certa)
        private boolean lumiTocaLuz(int i) {
            float dx = Math.abs(luzXNaTela(i) - (FADA_X_TELA + ALCANCE_X_DESLOCAMENTO));
            float dy = Math.abs(luzYNaTela(i) - fadaY);
            return dx <= ALCANCE_X && dy <= TOLERANCIA_ALTURA * ESCALA_LUZ[indiceFase()];
        }

        // Onde a flor i reaparece: logo depois da borda direita da tela,
        // mas sempre a uma distância segura das outras flores que ainda estão chegando
        private float posicaoDeRetorno(int i) {
            float posicao = deslocamento + LARGURA_MUNDO + MARGEM_RETORNO;
            for (int j = 0; j < florX.length; j++) {
                if (j != i) {
                    posicao = Math.max(posicao, florX[j] + ESPACO_ENTRE_FLORES);
                }
            }
            return posicao;
        }

        private void atualizarFase(float delta) {
            int indice = indiceFase();

            for (int i = 0; i < animacaoFlor.length; i++) {
                if (animacaoFlor[i] < DURACAO_TRANSFORMACAO_FLOR) {
                    animacaoFlor[i] = Math.min(DURACAO_TRANSFORMACAO_FLOR, animacaoFlor[i] + delta);
                }
            }

            // A força é lida UMA vez por frame (o KeyboardForceInput muda a cada chamada)
            forcaAtual = entradaForca.getForce();
            atualizarLumi(delta);

            if (conclusaoPendente) {
                tocandoLuz = false;
                deslocamento += VELOCIDADE_CENARIO[indice] * delta;
                // espera a animação da última flor antes de encerrar a fase
                if (animacoesTerminadas()) {
                    concluirFase();
                }
                return;
            }

            tempoFase += delta;   // para quando a última flor desperta (conclusaoPendente)

            medirChegada();

            // Rega: a Lumi está tocando a luz de alguma flor seca?
            tocandoLuz = false;
            for (int i = 0; i < florX.length; i++) {
                if (florRecuperada[i]) {
                    continue;
                }
                if (lumiTocaLuz(i)) {
                    tocandoLuz = true;
                    // Consistência: guarda a força enquanto rega
                    somaForca[i] += forcaAtual;
                    somaForcaQuadrado[i] += (double) forcaAtual * forcaAtual;
                    amostrasForca[i]++;
                    progressoFlor[i] += delta / TEMPO_DESPERTAR[indice];
                    if (progressoFlor[i] >= 1f) {
                        progressoFlor[i] = 1f;
                        florRecuperada[i] = true;
                        animacaoFlor[i] = 0f;
                        chegadaFlor[i] = Math.max(0f, chegadaPassada[i]);
                        firmezaFlor[i] = calcularFirmeza(i);
                        Gdx.app.log("Fase" + faseAtual, "Flor " + (i + 1) + " despertou (tentativas: "
                                + tentativasFlor[i] + ", chegada: " + chegadaFlor[i] + " s, firmeza: "
                                + Math.round(firmezaFlor[i] * 100) + "%)");
                    }
                } else if (progressoFlor[i] > 0f) {
                    progressoFlor[i] = Math.max(0f,
                            progressoFlor[i] - delta * DECAIMENTO_PROGRESSO / TEMPO_DESPERTAR[indice]);
                }
            }

            // O cenário anda sozinho; desacelera enquanto a Lumi rega
            float velocidade = VELOCIDADE_CENARIO[indice] * (tocandoLuz ? FATOR_VELOCIDADE_REGANDO : 1f);
            deslocamento += velocidade * delta;

            // Flor que passou sem despertar "volta mais tarde", entrando de novo pela direita
            for (int i = 0; i < florX.length; i++) {
                if (!florRecuperada[i] && luzXNaTela(i) < -DISTANCIA_SAIDA_TELA) {
                    florX[i] = posicaoDeRetorno(i);
                    progressoFlor[i] = 0f;
                    tentativasFlor[i]++;
                    // nova passada: as métricas recomeçam para esta flor
                    inicioPassada[i] = -1f;
                    chegadaPassada[i] = -1f;
                    somaForca[i] = 0;
                    somaForcaQuadrado[i] = 0;
                    amostrasForca[i] = 0;
                    Gdx.app.log("Fase" + faseAtual, "Flor " + (i + 1) + " passou; volta mais tarde");
                }
            }

            if (todasRecuperadas()) {
                conclusaoPendente = true;
                tocandoLuz = false;
            }
        }

        // Velocidade (artigo, 3.4): tempo que a criança leva para levar a Lumi até a ALTURA da próxima flor.
        // O relógio começa quando a luz dessa flor aparece na tela (e ela é a próxima a ser regada)
        // e para quando a Lumi entra na faixa de altura da luz.
        private void medirChegada() {
            float limiteEsquerdo = FADA_X_TELA + ALCANCE_X_DESLOCAMENTO - ALCANCE_X;
            int proxima = -1;
            for (int i = 0; i < florX.length; i++) {
                if (florRecuperada[i] || luzXNaTela(i) < limiteEsquerdo) {
                    continue;
                }
                if (proxima < 0 || luzXNaTela(i) < luzXNaTela(proxima)) {
                    proxima = i;
                }
            }
            if (proxima < 0 || chegadaPassada[proxima] >= 0f) {
                return;
            }
            if (inicioPassada[proxima] < 0f) {
                if (luzXNaTela(proxima) <= LARGURA_MUNDO) {
                    inicioPassada[proxima] = tempoFase;
                }
                return;
            }
            float tolerancia = TOLERANCIA_ALTURA * ESCALA_LUZ[indiceFase()];
            if (Math.abs(luzYNaTela(proxima) - fadaY) <= tolerancia) {
                chegadaPassada[proxima] = tempoFase - inicioPassada[proxima];
            }
        }

        // Firmeza da flor i: 1 = força totalmente estável enquanto regava
        private float calcularFirmeza(int i) {
            int n = amostrasForca[i];
            if (n < 2) {
                return 1f;
            }
            double media = somaForca[i] / n;
            double variancia = Math.max(0.0, somaForcaQuadrado[i] / n - media * media);
            float desvio = (float) Math.sqrt(variancia);
            return MathUtils.clamp(1f - desvio / SIGMA_REFERENCIA_FIRMEZA, 0f, 1f);
        }

        // Guarda o resultado da fase que acabou de terminar
        private void registrarResultadoFase() {
            int indice = indiceFase();
            int quantidade = florX.length;
            float somaChegada = 0f;
            float somaFirmeza = 0f;
            int voltas = 0;
            for (int i = 0; i < quantidade; i++) {
                somaChegada += chegadaFlor[i];
                somaFirmeza += firmezaFlor[i];
                voltas += tentativasFlor[i] - 1;
            }
            resultadoValido[indice] = true;
            resultadoTempo[indice] = tempoFase;
            resultadoChegada[indice] = somaChegada / quantidade;
            resultadoFirmeza[indice] = somaFirmeza / quantidade;
            resultadoVoltas[indice] = voltas;
            int estrelas = 1;                                      // terminou a fase
            if (voltas == 0) estrelas++;                           // nenhuma flor voltou
            if (resultadoFirmeza[indice] >= FIRMEZA_PARA_ESTRELA) estrelas++;   // força firme
            resultadoEstrelas[indice] = estrelas;
            Gdx.app.log("Resultado", "Fase " + faseAtual + ": tempo " + formatarTempo(tempoFase)
                    + ", chegada " + formatarSegundos(resultadoChegada[indice])
                    + ", firmeza " + Math.round(resultadoFirmeza[indice] * 100) + "%"
                    + ", voltas " + voltas + ", estrelas " + estrelas);
        }

        private void concluirFase() {
            registrarResultadoFase();
            nivelConcluido = true;
            Gdx.app.log("Fase" + faseAtual, "Nível " + faseAtual + " concluído");
            if (faseAtual == 3) {
                estado = Estado.FIM_DE_JOGO;
            } else {
                estado = Estado.NIVEL_CONCLUIDO;
            }
        }

        // ------------------------------------------------------------------
        // Lumi (desenho)
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

        private void desenharFada() {
            Texture tex;
            int sprite;
            if (estaRegando()) {
                tex = texFadaRegando;
                sprite = 3;
            } else if (subindo) {
                tex = texFadaVoando1;
                sprite = 1;
            } else if (descendo) {
                tex = texFadaVoando2;
                sprite = 2;
            } else {
                tex = texFadaParada;
                sprite = 0;
            }
            float w = larguraDe(tex);
            float h = alturaDe(tex);
            // flutua de leve, para parecer que está voando
            float flutuar = MathUtils.sin(tempoVoo * 3f) * 0.03f;
            // posiciona pelo CORPO (e não pelo canto da imagem), para não "pular" ao trocar de sprite
            float x = FADA_X_TELA - CENTRO_X_SPRITE[sprite] * w;
            float y = fadaY + flutuar - CENTRO_Y_SPRITE[sprite] * h;
            lote.draw(tex, x, y, w, h);
        }

        // Lumi está regando = tocando a luz de uma flor seca (som e sprite usam a mesma regra)
        private boolean estaRegando() {
            return tocandoLuz;
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

        // Cenário sem fim: a mesma imagem repetida, alternando normal/espelhada
        private void desenharCenarioRolando() {
            Texture t = texturaCenario();
            float altura = alturaCenario();
            int primeira = MathUtils.floor(deslocamento / LARGURA_CENARIO);
            for (int k = primeira; k <= primeira + 1; k++) {
                float x = k * LARGURA_CENARIO - deslocamento;
                boolean espelhar = (k % 2) != 0;
                // +0.01 de sobra na largura para não aparecer fresta entre as repetições
                lote.draw(t, x, -CAMERA_Y, LARGURA_CENARIO + 0.01f, altura,
                        0, 0, t.getWidth(), t.getHeight(), espelhar, false);
            }
        }

        private void desenharFlor(Texture tex, float xCenario, float alpha) {
            int indice = indiceFase();
            float largura = LARGURA_IMAGEM_FLOR[indice];
            float altura = largura * tex.getHeight() / tex.getWidth();
            // A base da flor (âncora) fica sempre sobre o chão do cenário
            float desenhoX = xCenario - ANCORA_X_FLOR[indice] * largura - deslocamento;
            float desenhoY = CHAO_FLORES - ANCORA_Y_FLOR[indice] * altura - CAMERA_Y;
            if (desenhoX > LARGURA_MUNDO || desenhoX + largura < 0f) {
                return;     // fora da tela
            }
            lote.setColor(1f, 1f, 1f, alpha);
            lote.draw(tex, desenhoX, desenhoY, largura, altura);
            lote.setColor(1f, 1f, 1f, 1f);
        }

        // Luz mágica de cada flor seca, na altura que ela pede
        private void desenharLuzes() {
            float base = tamanhoLuz();
            for (int i = 0; i < florX.length; i++) {
                if (florRecuperada[i]) {
                    continue;
                }
                float x = luzXNaTela(i);
                if (x < -base || x > LARGURA_MUNDO + base) {
                    continue;
                }
                boolean tocando = !conclusaoPendente && lumiTocaLuz(i);
                // pulsa de leve; cresce quando a Lumi está tocando
                float pulso = 1f + MathUtils.sin(tempoVoo * 4f + i) * 0.06f;
                float tam = base * pulso * (tocando ? 1.25f : 1f);
                lote.draw(texLuzMagica, x - tam / 2f, luzYNaTela(i) - tam / 2f, tam, tam);
            }
        }

        // Anel de progresso ao lado da luz que está sendo regada
        private void desenharProgressoRega() {
            boolean algum = false;
            for (int i = 0; i < florX.length; i++) {
                if (!florRecuperada[i] && progressoFlor[i] > 0f) {
                    algum = true;
                    break;
                }
            }
            if (!algum) {
                return;
            }
            float raio = 0.15f;
            float afastamento = tamanhoLuz() * 0.5f;
            desenhador.begin(ShapeRenderer.ShapeType.Filled);
            for (int i = 0; i < florX.length; i++) {
                if (florRecuperada[i] || progressoFlor[i] <= 0f) {
                    continue;
                }
                float cx = luzXNaTela(i) + afastamento;
                float cy = luzYNaTela(i) + afastamento;
                desenhador.setColor(0.43f, 0.29f, 0.32f, 1f);          // contorno (marrom dos botões)
                desenhador.circle(cx, cy, raio + 0.03f, 24);
                desenhador.setColor(0.91f, 0.89f, 0.80f, 1f);          // fundo creme
                desenhador.circle(cx, cy, raio, 24);
                desenhador.setColor(0.47f, 0.75f, 0.43f, 1f);          // verde do progresso
                float graus = 360f * progressoFlor[i];
                desenhador.arc(cx, cy, raio - 0.02f, 90f - graus, graus, 24);
            }
            desenhador.end();
        }

        // Desenho de qualquer fase (1, 2 e 3)
        private void desenharFase() {
            lote.begin();
            desenharCenarioRolando();
            for (int i = 0; i < florX.length; i++) {
                boolean viva = florRecuperada[i];
                float alpha = viva ? Math.min(1f, animacaoFlor[i] / DURACAO_TRANSFORMACAO_FLOR) : 1f;
                if (viva && alpha < 1f) {
                    // a flor seca some por baixo enquanto a viva aparece
                    desenharFlor(texFloresMortas[i], florX[i], 1f - alpha);
                }
                desenharFlor(viva ? texFloresVivas[i] : texFloresMortas[i], florX[i], alpha);
            }
            desenharLuzes();
            desenharFada();
            lote.draw(texBotaoPausar, 0, 0, LARGURA_MUNDO, ALTURA_MUNDO);
            desenharContadorFlores(florRecuperada.length, floresRecuperadas());
            desenharTempo();
            lote.end();

            desenharProgressoRega();
            desenharBarraForca();

            if (DEBUG_HITBOX) {
                desenhador.begin(ShapeRenderer.ShapeType.Line);
                desenhador.setColor(1f, 1f, 1f, 1f);
                desenharHitboxesPausa();
                // área em que a Lumi alcança uma luz, e o centro do corpo da Lumi
                float tol = TOLERANCIA_ALTURA * ESCALA_LUZ[indiceFase()];
                float cx = FADA_X_TELA + ALCANCE_X_DESLOCAMENTO;
                desenhador.circle(FADA_X_TELA, fadaY, 0.06f);
                desenhador.rect(cx - ALCANCE_X, fadaY - tol, ALCANCE_X * 2f, tol * 2f);
                desenhador.line(0f, CHAO_FLORES - CAMERA_Y, LARGURA_MUNDO, CHAO_FLORES - CAMERA_Y);
                desenhador.end();
            }

            desenharPausa();
        }

        // Plaquinha com a flor e o placar "regadas / total".
        // Coordenadas de tela (não rola com o cenário). Chamar dentro de lote.begin()/end().
        // Só é chamado em desenharFase(): não aparece no menu nem nas telas de conclusão.
        // Escreve um texto com os números desenhados (0-9, / e :), centralizado em (centroX, centroY).
        // Chamar dentro de lote.begin()/end().
        private float larguraTexto(String texto, float altura, float espacamento) {
            TextureRegion modelo = regNumeros[0];
            float larguraNumero = altura * modelo.getRegionWidth() / modelo.getRegionHeight();
            return larguraNumero * espacamento * (texto.length() - 1) + larguraNumero;
        }

        private void desenharTexto(String texto, float centroX, float centroY, float altura, float espacamento) {
            TextureRegion modelo = regNumeros[0];
            float larguraNumero = altura * modelo.getRegionWidth() / modelo.getRegionHeight();
            float avanco = larguraNumero * espacamento;
            float larguraTexto = avanco * (texto.length() - 1) + larguraNumero;
            float x = centroX - larguraTexto / 2f;
            float y = centroY - altura / 2f;
            for (int i = 0; i < texto.length(); i++) {
                int indice = CARACTERES_CONTADOR.indexOf(texto.charAt(i));
                if (indice >= 0) {
                    lote.draw(regNumeros[indice], x + i * avanco, y, larguraNumero, altura);
                }
            }
        }

        // Plaquinha com relógio e o tempo da fase (m:ss), no canto superior esquerdo. Só nas fases.
        private void desenharTempo() {
            lote.draw(texPainelTempo, PAINEL_TEMPO_X, PAINEL_TEMPO_Y, PAINEL_TEMPO_W, PAINEL_TEMPO_H);
            String texto = formatarTempo(tempoFase);
            desenharTexto(texto, PAINEL_TEMPO_X + TEXTO_TEMPO_CENTRO_X,
                    PAINEL_TEMPO_Y + PAINEL_TEMPO_H / 2f, ALTURA_NUMERO_TEMPO, ESPACAMENTO_NUMERO_TEMPO);
        }

        private void desenharContadorFlores(int total, int regadas) {
            lote.draw(texPainelContador, PAINEL_CONTADOR_X, PAINEL_CONTADOR_Y,
                    PAINEL_CONTADOR_W, PAINEL_CONTADOR_H);

            desenharTexto(regadas + "/" + total,
                    PAINEL_CONTADOR_X + TEXTO_CONTADOR_CENTRO_X,
                    PAINEL_CONTADOR_Y + PAINEL_CONTADOR_H / 2f,
                    ALTURA_NUMERO, ESPACAMENTO_NUMERO);
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
            boolean sobreResultados = BTN_FINAL_RESULTADOS.contains(toque);
            boolean sobreReiniciar = BTN_FINAL_REINICIAR.contains(toque);
            boolean sobreMenu = BTN_FINAL_MENU.contains(toque);
            atualizarCursor(sobreResultados || sobreReiniciar || sobreMenu
                    ? Cursor.SystemCursor.Hand : Cursor.SystemCursor.Arrow);

            if (Gdx.input.justTouched()) {
                if (sobreResultados) {
                    estado = Estado.RESULTADOS;
                    atualizarCursor(Cursor.SystemCursor.Arrow);
                    return;
                } else if (sobreReiniciar) {
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
                desenharRetangulo(BTN_FINAL_RESULTADOS);
                desenharRetangulo(BTN_FINAL_REINICIAR);
                desenharRetangulo(BTN_FINAL_MENU);
                desenhador.end();
            }
        }

        private void desenharRetangulo(Rectangle r) {
            desenhador.rect(r.x, r.y, r.width, r.height);
        }

        // ------------------------------------------------------------------
        // Tela de resultados: compara as 3 fases (métricas da seção 3.4 do artigo)
        // ------------------------------------------------------------------
        private void desenharResultados() {
            viewport.unproject(toque.set(Gdx.input.getX(), Gdx.input.getY()));
            boolean sobreMenu = BTN_RESULTADOS_MENU.contains(toque);
            boolean sobreJogar = BTN_RESULTADOS_JOGAR.contains(toque);
            atualizarCursor(sobreMenu || sobreJogar ? Cursor.SystemCursor.Hand : Cursor.SystemCursor.Arrow);

            if (Gdx.input.justTouched()) {
                if (sobreMenu) {
                    voltarAoMenu();
                    return;
                } else if (sobreJogar) {
                    iniciarFase1();
                    return;
                }
            }

            lote.begin();
            lote.draw(texResultadosFundo, 0, 0, LARGURA_MUNDO, ALTURA_MUNDO);
            for (int f = 0; f < 3; f++) {
                if (!resultadoValido[f]) {
                    continue;
                }
                float colunaX = RESULTADO_COLUNA_X + f * RESULTADO_COLUNA_PASSO;
                float centroX = colunaX + RESULTADO_COLUNA_W / 2f;
                // estrelas
                for (int k = 0; k < 3; k++) {
                    Texture estrela = k < resultadoEstrelas[f] ? texEstrelaCheia : texEstrelaVazia;
                    float ex = centroX + (k - 1) * RESULTADO_ESTRELA_PASSO;
                    lote.draw(estrela, ex - RESULTADO_ESTRELA_TAM / 2f,
                            RESULTADO_ESTRELAS_Y - RESULTADO_ESTRELA_TAM / 2f,
                            RESULTADO_ESTRELA_TAM, RESULTADO_ESTRELA_TAM);
                }
                // valores, alinhados à direita: tempo, chegada, firmeza, voltas
                String[] valores = {
                        formatarTempo(resultadoTempo[f]),
                        formatarSegundos(resultadoChegada[f]),
                        Math.round(resultadoFirmeza[f] * 100) + "%",
                        String.valueOf(resultadoVoltas[f]),
                };
                float direita = colunaX + RESULTADO_COLUNA_W - RESULTADO_VALOR_MARGEM;
                for (int j = 0; j < valores.length; j++) {
                    float y = RESULTADO_LINHA1_Y - j * RESULTADO_LINHA_PASSO;
                    float largura = larguraTexto(valores[j], RESULTADO_ALTURA_NUMERO, ESPACAMENTO_NUMERO_TEMPO);
                    desenharTexto(valores[j], direita - largura / 2f, y,
                            RESULTADO_ALTURA_NUMERO, ESPACAMENTO_NUMERO_TEMPO);
                }
            }
            Texture frase = escolherFrase();
            lote.draw(frase, (LARGURA_MUNDO - RESULTADO_FRASE_W) / 2f, RESULTADO_FRASE_Y - RESULTADO_FRASE_H / 2f,
                    RESULTADO_FRASE_W, RESULTADO_FRASE_H);
            lote.end();

            if (DEBUG_HITBOX) {
                desenhador.begin(ShapeRenderer.ShapeType.Line);
                desenhador.setColor(1f, 1f, 1f, 1f);
                desenharRetangulo(BTN_RESULTADOS_MENU);
                desenharRetangulo(BTN_RESULTADOS_JOGAR);
                desenhador.end();
            }
        }

        // Frase de incentivo escolhida a partir da evolução em relação à fase 1
        private Texture escolherFrase() {
            if (!resultadoValido[0] || !resultadoValido[1] || !resultadoValido[2]) {
                return texFraseGeral;
            }
            if (resultadoVoltas[0] + resultadoVoltas[1] + resultadoVoltas[2] == 0) {
                return texFrasePrimeira;
            }
            float melhoraFirmeza2 = resultadoFirmeza[1] - resultadoFirmeza[0];
            float melhoraFirmeza3 = resultadoFirmeza[2] - resultadoFirmeza[0];
            if (Math.max(melhoraFirmeza2, melhoraFirmeza3) >= MELHORA_MINIMA_FIRMEZA) {
                return melhoraFirmeza3 >= melhoraFirmeza2 ? texFraseFirmeza3 : texFraseFirmeza2;
            }
            float melhoraChegada2 = resultadoChegada[0] - resultadoChegada[1];
            float melhoraChegada3 = resultadoChegada[0] - resultadoChegada[2];
            if (Math.max(melhoraChegada2, melhoraChegada3) >= MELHORA_MINIMA_CHEGADA) {
                return melhoraChegada3 >= melhoraChegada2 ? texFraseChegada3 : texFraseChegada2;
            }
            return texFraseGeral;
        }

        // 75 s -> "1:15"
        private String formatarTempo(float segundosTotais) {
            int segundos = (int) segundosTotais;
            return (segundos / 60) + ":" + (segundos % 60 < 10 ? "0" : "") + (segundos % 60);
        }

        // 2.14 s -> "2,1s"
        private String formatarSegundos(float segundos) {
            int decimos = Math.round(segundos * 10f);
            return (decimos / 10) + "," + (decimos % 10) + "s";
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
            fadaY = Y_FADA_MIN;
            subindo = false;
            descendo = false;
            tempoVoo = 0f;
            tocandoLuz = false;
            deslocamento = 0f;
            tempoFase = 0f;
            conclusaoPendente = false;
            configurarFlores(fase);
            musicaJardim.stop();
            somRegar.stop();
        }

        // Começar pela fase 1 = jogo novo: apaga os resultados anteriores
        private void iniciarFase1() {
            for (int i = 0; i < 3; i++) {
                resultadoValido[i] = false;
            }
            prepararNovaFase(1, Estado.FASE1);
        }

        private void iniciarFase2() {
            prepararNovaFase(2, Estado.FASE2);
        }

        private void iniciarFase3() {
            prepararNovaFase(3, Estado.FASE3);
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
            boolean sobreSom = BTN_SOM.contains(toque);
            atualizarCursor(sobrePlay || sobreSom ? Cursor.SystemCursor.Hand : Cursor.SystemCursor.Arrow);

            if (Gdx.input.justTouched() && sobreSom) {
                somMutado = !somMutado;
            } else if (Gdx.input.justTouched() && sobrePlay) {
                iniciarFase1();
                return;
            }

            lote.begin();
            lote.draw(texMenu, 0, 0, LARGURA_MUNDO, ALTURA_MUNDO);
            lote.draw(somMutado ? texSomDesligado : texSomLigado,
                    BTN_SOM.x, BTN_SOM.y, BTN_SOM.width, BTN_SOM.height);
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
                desenhador.rect(BTN_SOM.x, BTN_SOM.y, BTN_SOM.width, BTN_SOM.height);
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
                case FASE1:
                case FASE2:
                case FASE3: {
                    boolean estavaPausado = pausado;
                    atualizarEntradaFase();
                    if (estado == Estado.MENU) {
                        break;
                    }
                    if (!estavaPausado && !pausado) {
                        atualizarFase(Gdx.graphics.getDeltaTime());
                    }
                    if (estado == Estado.NIVEL_CONCLUIDO) {
                        desenharNivelConcluido();
                    } else if (estado == Estado.FIM_DE_JOGO) {
                        desenharTelaFinal();
                    } else {
                        desenharFase();
                    }
                    break;
                }
                case NIVEL_CONCLUIDO:
                    desenharNivelConcluido();
                    break;
                case FIM_DE_JOGO:
                    desenharTelaFinal();
                    break;
                case RESULTADOS:
                    desenharResultados();
                    break;
            }
            atualizarAudio();
        }

        private void atualizarAudio() {
            float volume = somMutado ? 0f : 1f;
            musicaMenu.setVolume(volume);
            musicaJardim.setVolume(volume);
            somPassouFase.setVolume(volume);
            somRegar.setVolume(volume);

            boolean emFase = estado == Estado.FASE1 || estado == Estado.FASE2 || estado == Estado.FASE3;
            boolean telaConclusao = estado == Estado.NIVEL_CONCLUIDO || estado == Estado.FIM_DE_JOGO;
            boolean tocarMenu = estado == Estado.MENU || estado == Estado.RESULTADOS || (emFase && pausado);
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
            texSomLigado.dispose();
            texSomDesligado.dispose();
            texPainelContador.dispose();
            texPainelTempo.dispose();
            texResultadosFundo.dispose();
            texEstrelaCheia.dispose();
            texEstrelaVazia.dispose();
            texFraseFirmeza2.dispose();
            texFraseFirmeza3.dispose();
            texFraseChegada2.dispose();
            texFraseChegada3.dispose();
            texFrasePrimeira.dispose();
            texFraseGeral.dispose();
            texNumerosContador.dispose();
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
            texLuzMagica.dispose();
            texBotaoPausar.dispose();
            texFadaParada.dispose();
            texFadaRegando.dispose();
            texFadaVoando1.dispose();
            texFadaVoando2.dispose();
        }
    }