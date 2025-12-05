# Jogo da Velha Distribuído com Apache Kafka

Sistema distribuído de Jogo da Velha desenvolvido em Java usando Apache Kafka para comunicação entre processos e threads para concorrência.

## 📋 Descrição do Projeto

Este projeto implementa um jogo da velha multiplayer onde:
- **Servidor**: Coordena o jogo, valida jogadas e determina vencedores
- **Jogadores**: Dois clientes que se conectam via Kafka e jogam alternadamente
- **Comunicação**: Apache Kafka com dois tópicos (jogadas e estado)
- **Concorrência**: Threads para processar múltiplas mensagens simultaneamente

## 🏗️ Arquitetura

```
Jogador 1 ──┐
            ├──> Kafka (Tópico: jogadas) ──> Servidor ──> Kafka (Tópico: estado) ──┐
Jogador 2 ──┘                                                                       ├──> Jogador 1
                                                                                    └──> Jogador 2
```

### Componentes:

1. **ServidorJogo**: Coordena o jogo usando threads para processar mensagens
2. **Jogador**: Cliente que envia jogadas e recebe atualizações via thread separada
3. **Tabuleiro**: Gerencia o estado do jogo com sincronização
4. **Mensagem**: Modelo de dados trocado via Kafka (serializado em JSON)

## 🔧 Tecnologias Utilizadas

- Java 11
- Apache Kafka 3.4.0
- Maven
- Gson (serialização JSON)
- Threads Java (concorrência)

## 📦 Pré-requisitos

1. **Java JDK 11+**
2. **Apache Kafka** instalado e rodando
3. **Maven** para compilar o projeto

### Instalação do Kafka (Windows)

1. Baixe o Kafka: https://kafka.apache.org/downloads
2. Extraia o arquivo
3. Inicie o Zookeeper:
   ```cmd
   bin\windows\zookeeper-server-start.bat config\zookeeper.properties
   ```
4. Em outro terminal, inicie o Kafka:
   ```cmd
   bin\windows\kafka-server-start.bat config\server.properties
   ```

## 🚀 Como Executar

### 1. Compilar o projeto
```cmd
mvn clean compile
```

### 2. Iniciar o Servidor
```cmd
mvn exec:java -Dexec.mainClass="jogodavelha.servidor.ServidorJogo"
```

### 3. Iniciar Jogador 1 (em outro terminal)
```cmd
mvn exec:java -Dexec.mainClass="jogodavelha.jogador.Jogador" -Dexec.args="Jogador1"
```

### 4. Iniciar Jogador 2 (em outro terminal)
```cmd
mvn exec:java -Dexec.mainClass="jogodavelha.jogador.Jogador" -Dexec.args="Jogador2"
```

## 🎮 Como Jogar

1. Aguarde os dois jogadores se conectarem
2. O servidor inicia o jogo automaticamente
3. Digite as coordenadas no formato: `linha coluna` (ex: `0 1`)
4. O tabuleiro é numerado de 0 a 2:
   ```
     0 1 2
   0 - - -
   1 - - -
   2 - - -
   ```
5. Jogador X começa, depois alterna para O
6. O jogo termina quando há um vencedor ou empate

## 🧵 Uso de Threads e Concorrência

### Servidor (ServidorJogo.java):
- **Thread principal**: Consome mensagens do Kafka
- **Threads de processamento**: Cada mensagem é processada em thread separada
- **Sincronização**: Método `tratarMensagem()` é sincronizado para evitar condições de corrida

### Jogador (Jogador.java):
- **Thread principal**: Lê entrada do usuário e envia jogadas
- **Thread receptora**: Recebe mensagens do servidor via Kafka continuamente
- **Daemon thread**: Thread receptora roda em background

### Tabuleiro (Tabuleiro.java):
- **Métodos sincronizados**: `fazerJogada()`, `verificarVencedor()`, etc.
- **Thread-safe**: Protege o estado compartilhado entre threads

## 📊 Fluxo de Mensagens

1. **CONECTAR**: Jogador → Servidor (solicita conexão)
2. **CONECTADO**: Servidor → Jogador (confirma conexão)
3. **AGUARDANDO**: Servidor → Jogador (aguarda segundo jogador)
4. **JOGADA**: Jogador → Servidor (envia coordenadas)
5. **ESTADO**: Servidor → Jogadores (atualiza tabuleiro)
6. **ERRO**: Servidor → Jogador (jogada inválida)
7. **FIM**: Servidor → Jogadores (jogo finalizado)

## 📝 Logs e Saída

O sistema exibe logs detalhados:
- `[SERVIDOR]`: Mensagens do servidor
- `[Jogador1]` / `[Jogador2]`: Mensagens dos jogadores
- Tabuleiro atualizado após cada jogada
- Notificações de vez, erros e resultado final

## ⚠️ Tratamento de Erros

- Validação de coordenadas (0-2)
- Verificação de posição ocupada
- Controle de vez dos jogadores
- Reconexão automática do Kafka
- Mensagens de erro claras

## 🎯 Requisitos Atendidos

✅ Comunicação entre processos via Apache Kafka  
✅ Uso de threads para concorrência (servidor e clientes)  
✅ Sincronização de estado compartilhado  
✅ Logs detalhados de interações  
✅ Tratamento de erros e validações  
✅ Jogo colaborativo em tempo real  
✅ Múltiplos clientes simultâneos  

## 📚 Estrutura do Código

```
src/main/java/jogodavelha/
├── servidor/
│   └── ServidorJogo.java      # Coordena o jogo
├── jogador/
│   └── Jogador.java            # Cliente jogador
├── modelo/
│   ├── Tabuleiro.java          # Lógica do jogo
│   └── Mensagem.java           # Modelo de dados
└── util/
    └── KafkaConfig.java        # Configurações Kafka
```

## 🔍 Exemplo de Execução

```
[SERVIDOR] Servidor iniciado. Aguardando jogadores...
[Jogador1] Conectando ao servidor...
[SERVIDOR] Jogador Jogador1 conectado como 'X'
[Jogador1] Você é o jogador 'X'
[Jogador1] Aguardando outro jogador...

[Jogador2] Conectando ao servidor...
[SERVIDOR] Jogador Jogador2 conectado como 'O'
[SERVIDOR] Jogo iniciado!

  0 1 2
0 - - -
1 - - -
2 - - -

[Jogador1] Sua vez!
> 1 1
[SERVIDOR] Jogada realizada por Jogador1 na posição (1,1)
[Jogador2] Sua vez!
```

## 👨‍💻 Autor

Projeto desenvolvido para disciplina de Sistemas Distribuídos - Análise e Desenvolvimento de Sistemas
