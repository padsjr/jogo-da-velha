package jogodavelha.servidor;

import com.google.gson.Gson;
import jogodavelha.modelo.Mensagem;
import jogodavelha.modelo.Tabuleiro;
import jogodavelha.util.KafkaConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServidorJogo {
    private Tabuleiro tabuleiro;
    private KafkaConsumer<String, String> consumer;
    private KafkaProducer<String, String> producer;
    private Gson gson;
    private Map<String, Character> jogadores;
    private String jogadorAtual;
    private boolean jogoAtivo;
    private int numeroJogadores;

    public ServidorJogo() {
        this.tabuleiro = new Tabuleiro();
        this.gson = new Gson();
        this.jogadores = new ConcurrentHashMap<>();
        this.jogoAtivo = false;
        this.numeroJogadores = 0;
        configurarKafka();
    }

    private void configurarKafka() {

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.BOOTSTRAP_SERVERS);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfig.GROUP_ID_SERVIDOR);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(KafkaConfig.TOPICO_JOGADAS));

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.BOOTSTRAP_SERVERS);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer<>(producerProps);
    }

    public void iniciar() {
        System.out.println("[SERVIDOR] Servidor iniciado. Aguardando jogadores...");
        
        Thread processadorThread = new Thread(this::processarMensagens);
        processadorThread.start();

        try {
            processadorThread.join();
        } catch (InterruptedException e) {
            System.err.println("[SERVIDOR] Erro: " + e.getMessage());
        }
    }

    private void processarMensagens() {
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                
                for (ConsumerRecord<String, String> record : records) {
                    String mensagemJson = record.value();
                    Mensagem mensagem = gson.fromJson(mensagemJson, Mensagem.class);
                    
                    Thread handlerThread = new Thread(() -> tratarMensagem(mensagem));
                    handlerThread.start();
                }
            }
        } catch (Exception e) {
            System.err.println("[SERVIDOR] Erro ao processar mensagens: " + e.getMessage());
        } finally {
            fechar();
        }
    }

    private synchronized void tratarMensagem(Mensagem mensagem) {
        switch (mensagem.getTipo()) {
            case "CONECTAR":
                conectarJogador(mensagem.getJogadorId());
                break;
            case "JOGADA":
                processarJogada(mensagem);
                break;
            default:
                System.out.println("[SERVIDOR] Tipo de mensagem desconhecido: " + mensagem.getTipo());
        }
    }

    private void conectarJogador(String jogadorId) {
        if (numeroJogadores >= 2) {
            enviarMensagem(jogadorId, "ERRO", "Ja existem dois jogadores!");
            return;
        }

        char simbolo = numeroJogadores == 0 ? 'X' : 'O';
        jogadores.put(jogadorId, simbolo);
        numeroJogadores++;

        System.out.println("[SERVIDOR] Jogador " + jogadorId + " conectado como '" + simbolo + "' (" + numeroJogadores + "/2)");
        enviarMensagem(jogadorId, "CONECTADO", "Você é o jogador '" + simbolo + "'");

        if (numeroJogadores == 2) {
            iniciarJogo();
        } else {
            enviarMensagem(jogadorId, "AGUARDANDO", "Aguardando outro jogador...");
        }
    }

    private void iniciarJogo() {
        jogoAtivo = true;
        jogadorAtual = obterPrimeiroJogador();
        
        System.out.println("\n========================================");
        System.out.println("[SERVIDOR] JOGO INICIADO!");
        System.out.println("========================================");
        System.out.println(tabuleiro);
        
        for (String jogadorId : jogadores.keySet()) {
            enviarEstadoJogo(jogadorId);
        }
    }

    private void processarJogada(Mensagem mensagem) {
        if (!jogoAtivo) {
            enviarMensagem(mensagem.getJogadorId(), "ERRO", "Jogo não está ativo!");
            return;
        }

        if (!mensagem.getJogadorId().equals(jogadorAtual)) {
            enviarMensagem(mensagem.getJogadorId(), "ERRO", "Não é sua vez!");
            return;
        }

        char simbolo = jogadores.get(mensagem.getJogadorId());
        boolean jogadaValida = tabuleiro.fazerJogada(mensagem.getLinha(), mensagem.getColuna(), simbolo);

        if (!jogadaValida) {
            enviarMensagem(mensagem.getJogadorId(), "ERRO", "Jogada inválida!");
            return;
        }

        System.out.println("[SERVIDOR] Jogada realizada por " + mensagem.getJogadorId() + 
                         " na posição (" + mensagem.getLinha() + "," + mensagem.getColuna() + ")");
        System.out.println(tabuleiro);

        char vencedor = tabuleiro.verificarVencedor();
        if (vencedor != '-') {
            finalizarJogo("Jogador '" + vencedor + "' venceu!");
            return;
        }

        if (tabuleiro.estaCompleto()) {
            finalizarJogo("Empate!");
            return;
        }

        alternarJogador();
        
        for (String jogadorId : jogadores.keySet()) {
            enviarEstadoJogo(jogadorId);
        }
    }

    private void alternarJogador() {
        for (String jogadorId : jogadores.keySet()) {
            if (!jogadorId.equals(jogadorAtual)) {
                jogadorAtual = jogadorId;
                break;
            }
        }
    }

    private String obterPrimeiroJogador() {
        return jogadores.keySet().iterator().next();
    }

    private void enviarEstadoJogo(String jogadorId) {
        Mensagem msg = new Mensagem("ESTADO", "servidor");
        msg.setTabuleiro(tabuleiro.paraString());
        msg.setConteudo(jogadorId.equals(jogadorAtual) ? "Sua vez!" : "Aguarde sua vez...");
        
        String json = gson.toJson(msg);
        producer.send(new ProducerRecord<>(KafkaConfig.TOPICO_ESTADO, jogadorId, json));
        producer.flush();
    }

    private void enviarMensagem(String jogadorId, String tipo, String conteudo) {
        Mensagem msg = new Mensagem(tipo, "servidor");
        msg.setConteudo(conteudo);
        
        String json = gson.toJson(msg);
        producer.send(new ProducerRecord<>(KafkaConfig.TOPICO_ESTADO, jogadorId, json));
        producer.flush();
    }

    private void finalizarJogo(String resultado) {
        jogoAtivo = false;
        System.out.println("[SERVIDOR] Jogo finalizado: " + resultado);
        
        for (String jogadorId : jogadores.keySet()) {
            Mensagem msg = new Mensagem("FIM", "servidor");
            msg.setConteudo(resultado);
            msg.setTabuleiro(tabuleiro.paraString());
            
            String json = gson.toJson(msg);
            producer.send(new ProducerRecord<>(KafkaConfig.TOPICO_ESTADO, jogadorId, json));
        }
    }

    private void fechar() {
        if (consumer != null) consumer.close();
        if (producer != null) producer.close();
        System.out.println("[SERVIDOR] Servidor encerrado.");
    }

    public static void main(String[] args) {
        ServidorJogo servidor = new ServidorJogo();
        servidor.iniciar();
    }
}
