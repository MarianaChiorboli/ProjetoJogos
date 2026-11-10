package br.mackenzie.lwjgl3;

import br.mackenzie.ForceInput;
import com.fazecast.jSerialComm.SerialPort;
import com.badlogic.gdx.Gdx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

/** Lê a força do FSR por uma porta serial conectada ao ESP32. */
public class SerialForceInput implements ForceInput {
    public static final int BAUD = 115200;
    // Estes valores devem ser calibrados individualmente para cada criança.
    public static final int LEITURA_REPOUSO = 100;
    public static final int LEITURA_MAXIMA = 2500;
    public static final float TAXA_SUAVIZACAO = 15f;
    public static final boolean DEBUG = true;

    private static final long INTERVALO_RECONEXAO_MS = 2000L;
    private static final long INTERVALO_DEBUG_MS = 500L;

    private final String nomePortaFixa;
    private volatile int leituraBruta;
    private volatile boolean conectado;
    private float forca;
    private long ultimoDebugMs; // usado apenas pela thread de leitura

    /** Inicia a leitura em segundo plano; null seleciona automaticamente a porta do ESP32. */
    public SerialForceInput(String nomePortaFixa) {
        this.nomePortaFixa = nomePortaFixa;
        Thread leitorSerial = new Thread(this::lerSerial, "Leitor serial do FSR");
        leitorSerial.setDaemon(true);
        leitorSerial.start();
    }

    @Override
    public float getForce() {
        float alvo = 0f;
        if (conectado) {
            alvo = (float) (leituraBruta - LEITURA_REPOUSO)
                    / (LEITURA_MAXIMA - LEITURA_REPOUSO);
            alvo = Math.max(0f, Math.min(1f, alvo));
        }

        float fator = (float) (1.0 - Math.exp(-TAXA_SUAVIZACAO * Gdx.graphics.getDeltaTime()));
        forca += (alvo - forca) * fator;
        return forca;
    }

    @Override
    public void reset() {
        forca = 0f;
    }

    private void lerSerial() {
        while (!Thread.currentThread().isInterrupted()) {
            SerialPort porta = localizarPorta();
            if (porta == null) {
                System.out.println("[Serial] Porta do ESP32 nao encontrada; tentando novamente.");
            } else {
                conectarELer(porta);
            }

            try {
                Thread.sleep(INTERVALO_RECONEXAO_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private SerialPort localizarPorta() {
        SerialPort[] portas = SerialPort.getCommPorts();
        if (nomePortaFixa != null) {
            for (SerialPort porta : portas) {
                if (nomePortaFixa.equalsIgnoreCase(porta.getSystemPortName())) {
                    return porta;
                }
            }
            return null;
        }

        String[] descritores = {"CP210", "CH340", "CH910", "SILICON LABS", "USB"};
        for (SerialPort porta : portas) {
            String descricao = porta.getDescriptivePortName();
            if (descricao == null) {
                continue;
            }
            String descricaoMaiuscula = descricao.toUpperCase(Locale.ROOT);
            for (String descritor : descritores) {
                if (descricaoMaiuscula.contains(descritor)) {
                    return porta;
                }
            }
        }
        return null;
    }

    private void conectarELer(SerialPort porta) {
        try {
            porta.setComPortParameters(BAUD, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
            porta.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
            if (!porta.openPort()) {
                System.out.println("[Serial] Nao foi possivel abrir " + porta.getSystemPortName()
                        + " (codigo " + porta.getLastErrorCode() + "). "
                        + "Feche o Serial Monitor do Arduino IDE e outros programas que usam a porta.");
                return;
            }

            conectado = true;
            System.out.println("[Serial] Conectado a porta " + porta.getSystemPortName() + ".");
            try (BufferedReader leitor = new BufferedReader(
                    new InputStreamReader(porta.getInputStream()))) {
                String linha;
                while ((linha = leitor.readLine()) != null) {
                    processarLinha(linha);
                }
            }
        } catch (IOException e) {
            System.out.println("[Serial] Erro ao ler a porta " + porta.getSystemPortName()
                    + ": " + e.getMessage());
        } finally {
            conectado = false;
            porta.closePort();
            System.out.println("[Serial] Conexao encerrada; tentando reconectar.");
        }
    }

    private void processarLinha(String linha) {
        int valor;
        try {
            valor = Integer.parseInt(linha.trim());
        } catch (NumberFormatException e) {
            return;
        }
        if (valor < 0 || valor > 4095) {
            return;
        }

        leituraBruta = valor;
        long agoraMs = System.currentTimeMillis();
        if (DEBUG && agoraMs - ultimoDebugMs >= INTERVALO_DEBUG_MS) {
            System.out.println("[Serial] bruto = " + valor);
            ultimoDebugMs = agoraMs;
        }
    }
}
