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
| **2** | Flores em alturas diferentes | Ajuste da força |
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
| Resistor de 10 kΩ | 1 (2 a 3 de reserva) |
| Fios jumper (macho-macho / macho-fêmea) | 1 kit |
| Cabo USB de dados | 1 |

### Montagem (divisor de tensão)


O ESP32 lê o valor analógico do FSR (0 a 4095) e envia pela porta serial (USB, 9600 baud), uma leitura por linha. O jogo lê esses valores, aplica calibração e suavização e converte em altura da Lumi.

> 📝 O pino e o formato da mensagem serão confirmados na implementação (tarefa T10).

---

## 🛠️ Tecnologias

ADICIONAR 

---

## 🚀 Como executar

### 1. Firmware do ESP32

ADICIONAR

### 2. Jogo

ADICIONAR

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
