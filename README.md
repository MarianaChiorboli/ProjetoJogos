# 🌸 FloraLume

**Jogo sério para reabilitação da função da mão em crianças com lúpus eritematoso sistêmico juvenil (LESj), controlado por um sensor de pressão (ESP32 + FSR 402) e desenvolvido em libGDX.**

> Projeto Final da disciplina de Jogos Digitais — Sistemas de Informação, Universidade Presbiteriana Mackenzie.
> Professor: Leandro Puppo 

---

## 📖 Sobre o jogo

No FloraLume, a criança guia **Lumi**, uma pequena fada, por um jardim encantado que adormeceu e perdeu suas cores. Lumi voa sozinha para a frente, e **a força que a criança aplica no sensor controla a altura dela**: apertar mais faz Lumi subir e aliviar faz Lumi descer.

Para despertar cada flor, a criança precisa levar Lumi até a altura da flor e mantê-la ali. Assim, o exercício de **controle graduado da força de preensão** é a própria mecânica do jogo.

### Problema de reabilitação

O LESj frequentemente causa artrite e dor nas articulações das mãos e punhos, com redução da força de preensão e fadiga. O FloraLume trabalha:

- **Foco principal:** controle graduado da força de preensão (aplicar a força certa, nem fraca nem forte demais, e mantê-la estável).
- **Aspecto secundário:** coordenação motora fina e precisão.

Os alvos de força são **moderados** e definidos por **calibração individual**, para respeitar a dor articular e a fadiga de cada criança.

> ⚠️ O FloraLume é uma ferramenta de apoio à reabilitação. Ele não substitui o tratamento e deve ser usado fora de períodos de crise, com acompanhamento de um profissional de saúde.

---

## 🎮 Como jogar

| Ação | Como fazer |
|---|---|
| Subir / descer | Aumentar / diminuir a força no sensor |
| Despertar uma flor | Manter Lumi na altura da flor enquanto passa por ela |
| Trocar opção no menu | Aperto curto |
| Confirmar opção | Aperto longo |
| Pausar | Soltar o sensor por 3 segundos |

### Fases

| Fase | Desafio | Habilidade treinada |
|---|---|---|
| **1** | Flores todas na mesma altura | Força constante |
| **2** | Jardim em recuperação: explorar um cenário amplo e recuperar duas flores | Exploração e controle da força |
| **3** | Alturas diferentes, mais rápido e faixa-alvo mais estreita | Precisão |

### Pontuação e métricas

- **Velocidade:** tempo de resposta até Lumi atingir a altura de cada flor.
- **Consistência:** variação da força em relação à faixa-alvo enquanto Lumi passa pela flor.

Ao final, uma tela compara o desempenho nas 3 fases para mostrar a evolução da jogadora.

---

## 🔌 Hardware

| Componente | Quantidade |
|---|---|
| ESP32 DevKit V1 | 1 |
| Sensor de pressão FSR 402 | 1 |
| Protoboard | 1 |
| Resistor de 10 kΩ | 1 (opcional; recomendado para maior linearidade e estabilidade) |
| Fios jumper (macho-macho / macho-fêmea) | 1 kit |
| Cabo USB de dados | 1 |

### Montagem

Ligue uma perna do FSR ao GPIO32 e a outra ao GND, sem resistor externo, usando o pull-up interno do ESP32. Como o pull-up faz a leitura cair ao apertar o sensor, o firmware inverte o valor antes de enviá-lo. A comunicação USB serial usa 115200 baud.

Para maior linearidade e estabilidade, recomenda-se um resistor de 10 kΩ: nesse caso, ligue o FSR entre 3V3 e o GPIO34 e o resistor entre o GPIO34 e o GND. Nessa montagem alternativa, remova a inversão da leitura no firmware.

---

## 🛠️ Tecnologias

- libGDX
- Java 8
- Gradle
- jSerialComm
- ESP32 (Arduino IDE)
- FSR 402

---

## 🚀 Como executar

### 1. Firmware do ESP32

1. Instale o pacote **esp32** da Espressif no Arduino IDE.
2. Selecione a placa **ESP32 Dev Module**.
3. Abra e grave [`firmware/FloraLume/FloraLume.ino`](firmware/FloraLume/FloraLume.ino).
4. Confira os valores no Serial Monitor a 115200 baud.
5. Feche o Serial Monitor antes de iniciar o jogo: apenas um programa por vez pode usar a porta serial.

### 2. Jogo

Na raiz do projeto, execute:

```bash
./gradlew lwjgl3:run
```

O jogo detecta automaticamente a porta do ESP32. Para especificar uma porta:

```bash
./gradlew lwjgl3:run --args="COM3"
```

O ESP32 deve estar conectado para que o jogo receba leituras do sensor.

### Calibração

Com `DEBUG = true`, os valores brutos aparecem no console. Anote a leitura com o sensor solto e com a força máxima confortável da criança; ajuste `LEITURA_REPOUSO` e `LEITURA_MAXIMA` em `SerialForceInput` para esses valores.

---

## 📁 Estrutura do projeto

```
floralume/
├── core/        # Lógica do jogo (telas, Lumi, fases, métricas, leitura do sensor)
├── lwjgl3/      # Launcher desktop
├── assets/      # Sprites, fundos, fontes e sons
├── firmware/    # Código do ESP32 (.ino)
└── docs/        # OnePage, short paper e prints de tela
```

---

## 🗂️ Organização

- **Kanban** 
- **Commits:** `feat:`, `fix:`, `docs:`, `refactor:`, `chore:`

---

## 👩‍💻 Equipe

| Integrante |
|---|
| Mariana Chiorboli |
| Yasmin Mendes |

---

## 📄 Licença

Projeto acadêmico, sem fins comerciais. Personagens, cenários e identidade visual são originais.
