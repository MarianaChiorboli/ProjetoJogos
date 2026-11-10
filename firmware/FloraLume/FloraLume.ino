// FloraLume - firmware do ESP32 + FSR 402 (SEM resistor externo)
//
// Ligação (usa o pull-up interno do ESP32 como parte do divisor):
//   GPIO32 ---- FSR ---- GND      (o FSR não tem polaridade: tanto faz a perna)
//
// Com pull-up, a leitura CAI quando o sensor é apertado.
// Aqui invertemos (4095 - leitura) para que, como o jogo espera,
// quanto mais força, maior o número enviado.
//
// Envia uma leitura (0 a 4095) por linha pela USB, ~100 vezes por segundo.

const int PINO_FSR = 32;          
const int AMOSTRAS = 8;           
const unsigned long INTERVALO_MS = 10;

void setup() {
  Serial.begin(115200);           
  analogReadResolution(12);      
  analogSetPinAttenuation(PINO_FSR, ADC_11db);
}

void loop() {
  pinMode(PINO_FSR, INPUT_PULLUP);  
  for (int i = 0; i < AMOSTRAS; i++) {
    soma += analogRead(PINO_FSR);
  }
  int leitura = soma / AMOSTRAS;
  Serial.println(4095 - leitura);    // inverte: mais força = número maior
  delay(INTERVALO_MS);
}
