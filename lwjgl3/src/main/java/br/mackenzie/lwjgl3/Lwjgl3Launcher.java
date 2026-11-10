package br.mackenzie.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import br.mackenzie.ForceInput;
import br.mackenzie.Main;

/**
 * Inicia o jogo usando a porta serial detectada automaticamente, uma porta específica
 * (por exemplo, COM8).
 */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication(args);
    }

    private static Lwjgl3Application createApplication(String[] args) {
        return new Lwjgl3Application(new Main(criarEntradaForca(args)), getDefaultConfiguration());
    }

    private static ForceInput criarEntradaForca(String[] args) {
        return new SerialForceInput(args.length > 0 ? args[0] : null);
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("PrimeiroJogo");
       
        configuration.useVsync(true);
       
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);

        configuration.setWindowedMode(640, 480);
 
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}