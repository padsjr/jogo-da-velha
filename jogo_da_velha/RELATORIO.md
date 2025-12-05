# Relatório do Projeto - Jogo da Velha Distribuído

## 1. Introdução

Este projeto implementa um sistema distribuído de Jogo da Velha usando Apache Kafka para comunicação entre processos e threads Java para concorrência. O objetivo é demonstrar conceitos fundamentais de sistemas distribuídos de forma simples e didática.

## 2. Tecnologia Escolhida: Apache Kafka

### Por que Kafka?

- **Desacoplamento**: Produtores e consumidores não precisam conhecer uns aos outros
- **Escalabilidade**: Suporta múltiplos produtores e consumidores
- **Confiabilidade**: Mensagens são persistidas e podem ser reprocessadas
- **Ordem garantida**: Mensagens em uma partição mantêm ordem
- **Simplicidade**: API Java bem documentada e fácil de usar

### Arquitetura Kafka no Projeto

```
Tópico: jogo-velha-jogadas
├── Produtores: Jogador1, Jogador2
└── Consumidor: ServidorJogo

Tópico: jogo-velha-estado
├── Produtor: ServidorJogo
└── Consumidores: Jogador1, Jogador2
```

## 3. Uso de Threads e Concorrência

### 3.1 Servidor (ServidorJogo.java)

**Thread Principal**:
```java
Thread processadorThread = new Thread(this::processarMensagens);
processadorThread.start();
```
- Consome mensagens do tópico `jogo-velha-jogadas`
- Roda em loop infinito aguardando novas mensagens

**Threads de Processamento**:
```java
Thread handlerThread = new Thread(() -> tratarMensagem(mensagem));
handlerThread.start();
```
- Cada mensagem recebida é processada em thread separada
- Permite processar múltiplas requisições simultaneamente
- Não bloqueia o consumidor principal

### 3.2 Jogador (Jogador.java)

**Thread Principal**:
- Lê entrada do usuário (Scanner)
- Envia jogadas para o servidor

**Thread Receptora**:
```java
Thread receptorThread = new Thread(this::receberMensagens);
receptorThread.setDaemon(true);
receptorThread.start();
```
- Roda em background (daemon)
- Consome mensagens do tópico `jogo-velha-estado`
- Atualiza interface do jogador em tempo real

### 3.3 Sincronização

**Tabuleiro.java** - Métodos sincronizados:
```java
public synchronized boolean fazerJogada(int linha, int coluna, char simbolo)
public synchronized char verificarVencedor()
public synchronized boolean estaCompleto()
```

**Por quê?**
- Múltiplas threads podem acessar o tabuleiro
- Previne condições de corrida
- Garante consistência do estado

**ServidorJogo.java** - Tratamento sincronizado:
```java
private synchronized void tratarMensagem(Mensagem mensagem)
```

**Por quê?**
- Garante que apenas uma mensagem é processada por vez
- Evita conflitos ao alterar estado do jogo
- Mantém integridade das regras (vez do jogador, etc.)

## 4. Fluxo de Comunicação

### 4.1 Fase de Conexão

1. **Servidor** inicia e subscreve ao tópico `jogo-velha-jogadas`
2. **Jogador1** envia mensagem `CONECTAR` via Kafka
3. **Servidor** recebe, registra jogador como 'X'
4. **Servidor** envia `CONECTADO` e `AGUARDANDO` para Jogador1
5. **Jogador2** envia mensagem `CONECTAR`
6. **Servidor** recebe, registra jogador como 'O'
7. **Servidor** envia `CONECTADO` e inicia o jogo

### 4.2 Fase de Jogo

1. **Servidor** envia `ESTADO` com tabuleiro para ambos jogadores
2. **Jogador da vez** envia `JOGADA` com coordenadas
3. **Servidor** valida jogada:
   - Verifica se é a vez correta
   - Verifica se posição é válida
   - Verifica se posição está livre
4. **Servidor** atualiza tabuleiro
5. **Servidor** verifica condições de fim:
   - Três símbolos iguais em linha/coluna/diagonal = Vitória
   - Tabuleiro completo sem vencedor = Empate
6. **Servidor** envia novo `ESTADO` para todos
7. Repete até fim do jogo

### 4.3 Fase de Finalização

1. **Servidor** detecta fim do jogo
2. **Servidor** envia mensagem `FIM` com resultado
3. **Jogadores** exibem resultado final
4. **Jogadores** aguardam comando para sair

## 5. Tratamento de Erros

### Validações Implementadas:

1. **Coordenadas inválidas**:
   ```java
   if (linha < 0 || linha >= 3 || coluna < 0 || coluna >= 3)
       return false;
   ```

2. **Posição ocupada**:
   ```java
   if (grade[linha][coluna] != '-')
       return false;
   ```

3. **Jogador fora de vez**:
   ```java
   if (!mensagem.getJogadorId().equals(jogadorAtual))
       enviarMensagem(jogadorId, "ERRO", "Não é sua vez!");
   ```

4. **Jogo não ativo**:
   ```java
   if (!jogoAtivo)
       enviarMensagem(jogadorId, "ERRO", "Jogo não está ativo!");
   ```

5. **Formato de entrada inválido**:
   ```java
   try {
       int linha = Integer.parseInt(partes[0]);
   } catch (NumberFormatException e) {
       System.out.println("Digite números válidos!");
   }
   ```

## 6. Logs e Rastreabilidade

Todos os componentes registram suas ações:

**Servidor**:
```
[SERVIDOR] Kafka configurado com sucesso!
[SERVIDOR] Servidor iniciado. Aguardando jogadores...
[SERVIDOR] Mensagem recebida: Mensagem{tipo='CONECTAR', jogadorId='Jogador1'}
[SERVIDOR] Jogador Jogador1 conectado como 'X'
[SERVIDOR] Jogo iniciado!
[SERVIDOR] Jogada realizada por Jogador1 na posição (1,1)
[SERVIDOR] Jogo finalizado: Jogador 'X' venceu!
```

**Jogador**:
```
[Jogador1] Kafka configurado!
[Jogador1] Conectando ao servidor...
[Jogador1] Você é o jogador 'X'
[Jogador1] Sua vez!
[Jogador1] Jogada enviada: (1,1)
[Jogador1] JOGO FINALIZADO: Jogador 'X' venceu!
```

## 7. Complexidade e Adequação

### Pontos Fortes:

✅ **Simplicidade**: Código fácil de entender  
✅ **Completo**: Demonstra todos os conceitos necessários  
✅ **Funcional**: Sistema totalmente operacional  
✅ **Didático**: Comentários e logs explicativos  
✅ **Organizado**: Estrutura clara de pacotes  

### Conceitos Demonstrados:

✅ Comunicação distribuída via Kafka  
✅ Produtores e consumidores  
✅ Múltiplas threads concorrentes  
✅ Sincronização de estado compartilhado  
✅ Tratamento de erros  
✅ Logs detalhados  
✅ Serialização de dados (JSON)  

## 8. Possíveis Extensões (Futuras)

1. **Persistência**: Salvar histórico de partidas em banco de dados
2. **Reconexão**: Permitir jogador reconectar após queda
3. **Múltiplas Partidas**: Suportar várias partidas simultâneas
4. **Ranking**: Sistema de pontuação e estatísticas
5. **Interface Gráfica**: GUI com JavaFX ou Swing
6. **Chat**: Comunicação entre jogadores
7. **Observadores**: Permitir espectadores assistirem partidas

## 9. Conclusão

Este projeto demonstra de forma simples e eficaz os principais conceitos de sistemas distribuídos e programação concorrente. A escolha do Apache Kafka como middleware de comunicação e o uso adequado de threads Java tornam o sistema escalável e robusto, enquanto mantém a simplicidade necessária para fins didáticos.

O código está bem estruturado, documentado e pronto para ser apresentado como trabalho acadêmico de Análise e Desenvolvimento de Sistemas.
