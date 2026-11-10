package br.mackenzie.telas;

import br.mackenzie.Main;
import br.mackenzie.recursos.Imagem;
import br.mackenzie.ui.AreaClicavel;

/**
 * Tela feita de uma imagem de fundo + alguns botões (Fase concluída, Jardim restaurado, Resultados).
 * Faz o trabalho repetido (mouse, mãozinha, clique, fundo, debug); as subclasses só dizem
 * quais são os botões e o que cada um faz.
 */
public abstract class TelaComBotoes extends TelaBase {
    private final Imagem fundo;

    protected TelaComBotoes(Main jogo, Imagem fundo) {
        super(jogo);
        this.fundo = fundo;
    }

    /** Os botões da tela, na ordem de prioridade do clique. */
    protected abstract AreaClicavel[] botoes();

    /** O que acontece ao clicar no botão. */
    protected abstract void aoClicar(AreaClicavel botao);

    /** Desenho extra por cima do fundo (ex.: números da tela de resultados). Chamado dentro de begin()/end(). */
    protected void desenharConteudo() {
        // por padrão, nada
    }

    @Override
    public void render(float delta) {
        lerMouse();
        AreaClicavel sobMouse = null;
        for (AreaClicavel botao : botoes()) {
            if (mouseSobre(botao)) {
                sobMouse = botao;
                break;
            }
        }
        mostrarMaozinha(sobMouse != null);

        if (clicou() && sobMouse != null) {
            aoClicar(sobMouse);
            return;
        }

        lote.begin();
        desenharTelaCheia(fundo);
        desenharConteudo();
        lote.end();

        desenharContornos(botoes());
    }
}
