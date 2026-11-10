# FloraLume

Jogo desenvolvido em **libGDX** como ferramenta de apoio à **reabilitação e fisioterapia da função da mão em crianças com lúpus eritematoso sistêmico juvenil (LESj)**. O jogo trabalha o controle graduado da força de preensão: a criança controla a altura de Lumi, uma pequena fada, usando um sensor de pressão **FSR 402** conectado a um **ESP32**. A placa envia as leituras ao jogo pela porta serial USB.

Projeto Final da disciplina de Jogos Digitais — Sistemas de Informação, Universidade Presbiteriana Mackenzie.
Professor: Leandro Puppo.

> O FloraLume é uma ferramenta de apoio à reabilitação; não substitui tratamento. O uso deve respeitar os limites da criança e ter acompanhamento de um profissional de saúde.

## Sobre o jogo

Lumi voa pelo jardim e a força aplicada ao sensor controla sua altura: apertar mais faz Lumi subir; aliviar a pressão faz Lumi descer. Para despertar as flores, é preciso manter Lumi na altura delas enquanto passa por cada uma.

O foco é o controle graduado da força de preensão; coordenação motora fina e precisão são aspectos complementares. Os alvos devem ser moderados e calibrados individualmente, considerando conforto, dor e fadiga.

### Fases

| Fase | Desafio | Habilidade trabalhada |
|---|---|---|
| 1 | Flores na mesma altura | Controle de força constante |
| 2 | Explorar um cenário amplo e recuperar duas flores | Exploração e controle da força |
| 3 | Flores em alturas diferentes, maior velocidade e faixa-alvo mais estreita | Precisão |

O jogo registra métricas de velocidade de resposta e consistência da força em relação à faixa-alvo, e apresenta uma comparação do desempenho nas três fases.

## Tecnologias

- Java 8
- libGDX
- Gradle
- jSerialComm 2.11.0
- ESP32 com firmware para Arduino IDE
- Sensor de pressão FSR 402

## Hardware e montagem

### Componentes

| Componente | Quantidade |
|---|---:|
| ESP32 DevKit V1 | 1 |
| Sensor de pressão FSR 402 | 1 |
| Protoboard e fios jumper | 1 conjunto |
| Cabo USB de dados | 1 |

### Ligação padrão (sem resistor externo)

Conecte uma perna do FSR ao **GPIO32** e a outra ao **GND**. O FSR não tem polaridade. Esta montagem usa o pull-up interno do ESP32. Como a leitura elétrica cai quando o sensor é pressionado, o firmware inverte o valor antes de enviá-lo ao jogo.

O ESP32 envia uma leitura inteira por linha, de 0 a 4095, pela USB serial a **115200 baud**.

## Firmware do ESP32

1. Instale o pacote de placas **esp32**, da Espressif, no Arduino IDE.
2. Selecione **ESP32 Dev Module** como placa.
3. Abra [`firmware/FloraLume/FloraLume.ino`](firmware/FloraLume/FloraLume.ino) e grave o programa no ESP32.
4. Se quiser conferir as leituras, abra o Serial Monitor em **115200 baud**.
5. **Feche o Serial Monitor antes de iniciar o jogo.** Apenas um programa pode usar a porta serial de cada vez.

## Executar o jogo

Na raiz do repositório, inicie com detecção automática da porta serial:

```bash
./gradlew lwjgl3:run
```

No Windows PowerShell, o comando equivalente é:

```powershell
.\gradlew lwjgl3:run
```

Para escolher uma porta específica, passe seu nome como argumento (por exemplo, `COM3` ou `COM8`):

```bash
./gradlew lwjgl3:run --args="COM3"
```

## Leitura serial e calibração

A classe `SerialForceInput`, no módulo `lwjgl3`, implementa a interface de entrada de força usada pelo jogo. Ela:

- Detecta automaticamente dispositivos cuja descrição contenha CP210, CH340, CH910, Silicon Labs ou USB, ou usa a porta informada como argumento.
- Configura a comunicação para **115200 baud, 8 bits, sem paridade e 1 stop bit**.
- Ignora linhas inválidas e leituras fora do intervalo 0–4095; aplica um filtro de mediana em uma janela de cinco amostras.
- Tenta se reconectar a cada dois segundos se a porta não estiver disponível ou a conexão for interrompida.
- Exibe leituras e informações de conexão no console quando `DEBUG` está habilitado. Se a porta não abrir, o log inclui o código de erro; no Windows, código 5 indica acesso negado, geralmente porque outro programa está usando a porta.

Para calibrar, habilite `DEBUG` em `SerialForceInput.java`, anote os valores brutos com o sensor solto e com a força máxima confortável da criança, e ajuste `LEITURA_REPOUSO` e `LEITURA_MAXIMA`. A resposta também pode ser ajustada por `CURVA`, `VELOCIDADE_SUBIDA` e `VELOCIDADE_DESCIDA`. Faça a calibração com cuidado e sem exigir esforço desconfortável.

## Estrutura do repositório

```text
core/       Lógica do jogo, telas, fases e interface ForceInput
lwjgl3/     Aplicação desktop, launcher e leitura serial do sensor
assets/     Imagens, sons e demais recursos do jogo
firmware/   Código do ESP32
```

## Equipe

- Mariana Chiorboli
- Yasmin Mendes

## Licença

Projeto acadêmico, sem fins comerciais. Personagens, cenários e identidade visual são originais.
