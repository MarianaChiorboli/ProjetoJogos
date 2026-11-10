package br.mackenzie.telas;

import br.mackenzie.Main;
import br.mackenzie.recursos.Imagem;
import br.mackenzie.ui.AreaClicavel;
import br.mackenzie.ui.BotaoRetangular;

/** Tela "Você restaurou o jardim!" (fim da fase 3): ver resultados, jogar de novo ou menu. */
public class TelaJardimRestaurado extends TelaComBotoes {
    private final AreaClicavel verResultados = new BotaoRetangular(3.05f, 1.07f, 1.88f, 0.39f);
    private final AreaClicavel jogarDeNovo = new BotaoRetangular(2.98f, 0.60f, 0.98f, 0.35f);
    private final AreaClicavel menu = new BotaoRetangular(4.03f, 0.60f, 0.98f, 0.35f);
    private final AreaClicavel[] botoes = {verResultados, jogarDeNovo, menu};

    public TelaJardimRestaurado(Main jogo) {
        super(jogo, Imagem.JARDIM_RESTAURADO);
    }

    @Override
    protected AreaClicavel[] botoes() {
        return botoes;
    }

    @Override
    protected void aoClicar(AreaClicavel botao) {
        if (botao == verResultados) {
            jogo.mostrarResultados();
        } else if (botao == jogarDeNovo) {
            jogo.iniciarJogo();      // joga tudo de novo, desde a fase 1
        } else if (botao == menu) {
            jogo.irParaMenu();
        }
    }

    @Override
    public boolean ehTelaDeConclusao() {
        return true;
    }
}
