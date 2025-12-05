# Instruções Detalhadas - Jogo da Velha Distribuído

## 🎯 Objetivo do Projeto

Demonstrar conceitos de sistemas distribuídos usando:
- **Apache Kafka** para comunicação entre processos
- **Threads Java** para concorrência
- **Sincronização** para proteger estado compartilhado

## 📖 Conceitos Implementados

### 1. Comunicação Distribuída (Kafka)

O projeto usa dois tópicos Kafka:
- `jogo-velha-jogadas`: Jogadores enviam jogadas para o servidor
- `jogo-velha-estado`: Servidor envia atualizações para jogadores

**Por que Kafka?**
- Desacopla produtor e consumidor
- Permite múltiplos consumidores
- Garante ordem das mensagens
- Escalável e confiável

### 2. Concorrência com Threads

**Servidor (ServidorJogo.java)**:
```java
// Thread principal consome mensagens
Thread processadorThread = new Thread(this::processarMensagens);

// Cada mensagem é processada em thread separada
Thread handlerThread = new Thread(() -> tratarMensagem(mensagem));
```

**Jogador (Jogador.java)**:
```java
// Thread para receber mensagens em background
Thread receptorThread = new Thread(this::receberMensagens);
receptorThread.setDaemon(true); // Não bloqueia encerramento
```

### 3. Sincronização

**Tabuleiro.java** usa `synchronized` para evitar condições de corrida:
```java
public synchronized boolean fazerJogada(int linha, int coluna, char simbolo) {
    // Acesso exclusivo ao tabuleiro
}
```

**ServidorJogo.java** sincroniza tratamento de mensagens:
```java
private synchronized void tratarMensagem(Mensagem mensagem) {
    // Garante que apenas uma mensagem é processada por vez
}
```

## 🔄 Fluxo de Execução

### Fase 1: Conexão
1. Servidor inicia e aguarda no tópico `jogo-velha-jogadas`
2. Jogador 1 envia mensagem `CONECTAR`
3. Servidor responde `CONECTADO` e `AGUARDANDO`
4. Jogador 2 envia mensagem `CONECTAR`
5. Servidor responde `CONECTADO` e inicia o jogo

### Fase 2: Jogo
1. Servidor envia `ESTADO` para ambos jogadores
2. Jogador da vez envia `JOGADA` com coordenadas
3. Servidor valida e atualiza tabuleiro
4. Servidor verifica vencedor/empate
5. Servidor envia novo `ESTADO` para todos
6. Repete até fim do jogo

### Fase 3: Finalização
1. Servidor detecta vencedor ou empate
2. Envia mensagem `FIM` para todos
3. Jogadores exibem resultado final

## 🧪 Testando o Sistema

### Teste 1: Jogo Normal
```
Jogador1: 0 0  (X no canto superior esquerdo)
Jogador2: 1 1  (O no centro)
Jogador1: 0 1  (X no topo centro)
Jogador2: 1 0  (O no meio esquerda)
Jogador1: 0 2  (X no canto superior direito - VENCE!)
```

### Teste 2: Jogada Inválida
```
Jogador1: 0 0  (X)
Jogador2: 0 0  (Erro: posição ocupada)
```

### Teste 3: Fora de Vez
```
Jogador1: 0 0  (X)
Jogador1: 1 1  (Erro: não é sua vez)
```

## 🐛 Tratamento de Erros

O sistema valida:
- ✅ Coordenadas dentro do tabuleiro (0-2)
- ✅ Posição não ocupada
- ✅ Vez correta do jogador
- ✅ Jogo ativo
- ✅ Formato de entrada válido

## 📊 Logs e Monitoramento

Cada componente registra suas ações:

**Servidor**:
```
[SERVIDOR] Kafka configurado com sucesso!
[SERVIDOR] Servidor iniciado. Aguardando jogadores...
[SERVIDOR] Mensagem recebida: Mensagem{tipo='CONECTAR', jogadorId='Jogador1'}
[SERVIDOR] Jogador Jogador1 conectado como 'X'
[SERVIDOR] Jogo iniciado!
[SERVIDOR] Jogada realizada por Jogador1 na posição (1,1)
```

**Jogador**:
```
[Jogador1] Kafka configurado!
[Jogador1] Conectando ao servidor...
[Jogador1] Você é o jogador 'X'
[Jogador1] Sua vez!
[Jogador1] Jogada enviada: (1,1)
```

## 🎓 Pontos de Avaliação

### 1. Comunicação Distribuída ✅
- Usa Apache Kafka para troca de mensagens
- Dois tópicos separados (jogadas e estado)
- Serialização JSON com Gson

### 2. Concorrência ✅
- Servidor usa threads para processar mensagens
- Jogador usa thread separada para receber atualizações
- Não bloqueia durante I/O

### 3. Sincronização ✅
- Métodos `synchronized` no Tabuleiro
- Método `synchronized` no tratamento de mensagens
- Previne condições de corrida

### 4. Logs ✅
- Todas interações são registradas
- Identificação clara de cada componente
- Facilita debugging

### 5. Tratamento de Erros ✅
- Validações de entrada
- Mensagens de erro claras
- Sistema não quebra com entrada inválida

### 6. Complexidade Adequada ✅
- Simples o suficiente para entender
- Completo o suficiente para demonstrar conceitos
- Código bem organizado e comentado

## 💡 Possíveis Melhorias (Opcional)

1. **Persistência**: Salvar histórico de jogos
2. **Reconexão**: Permitir jogador reconectar após desconexão
3. **Múltiplas Partidas**: Suportar várias partidas simultâneas
4. **Interface Gráfica**: Adicionar GUI com JavaFX
5. **Ranking**: Sistema de pontuação
6. **Replay**: Assistir jogos anteriores

## 📝 Documentação do Código

Cada classe tem:
- Comentários JavaDoc
- Explicação do propósito
- Descrição de threads usadas
- Sincronização aplicada

## ✅ Checklist de Entrega

- [x] Código Java funcional
- [x] Usa Apache Kafka
- [x] Implementa threads
- [x] Sincronização adequada
- [x] Logs detalhados
- [x] Tratamento de erros
- [x] README com instruções
- [x] Comentários no código
- [x] Projeto Maven configurado
- [x] Scripts de execução

## 🎉 Conclusão

Este projeto demonstra de forma simples e didática os principais conceitos de sistemas distribuídos e programação concorrente, adequado para o nível de Análise e Desenvolvimento de Sistemas.
