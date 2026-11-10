package br.mackenzie.recursos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Carrega e guarda todas as imagens e sons do jogo, num lugar só.
 * Assim nenhuma outra classe precisa saber nome de arquivo, e tudo é descartado junto no final.
 */
public class Recursos implements Disposable {
    private final Map<Imagem, Texture> texturas = new EnumMap<>(Imagem.class);
    private final Map<Som, Music> sons = new EnumMap<>(Som.class);

    public void carregar() {
        for (Imagem imagem : Imagem.values()) {
            Texture textura = new Texture(Gdx.files.internal(imagem.arquivo()));
            textura.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            texturas.put(imagem, textura);
        }
        for (Som som : Som.values()) {
            Music musica = Gdx.audio.newMusic(Gdx.files.internal(som.arquivo()));
            musica.setLooping(som.repetir());
            sons.put(som, musica);
        }
    }

    public Texture textura(Imagem imagem) {
        return texturas.get(imagem);
    }

    public Music som(Som som) {
        return sons.get(som);
    }

    @Override
    public void dispose() {
        for (Music musica : sons.values()) {
            musica.dispose();
        }
        for (Texture textura : texturas.values()) {
            textura.dispose();
        }
        sons.clear();
        texturas.clear();
    }
}
