package jogodavelha.jogador;

import com.google.gson.Gson;
import jogodavelha.modelo.Mensagem;
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
import java.util.Collections;
import java.util.Properties;
import java.util.Scanner;

public class Jogador {
    private String jogadorId;
    private KafkaConsumer<String, String> consumer;
    private KafkaProducer<String, String> producer;
    private Gson gson;
    private boolean conectado;
    private boolean jogoAtivo;
    private Scanner scanner;

    public Jogador(String jogadorId) {
        this.jogadorId = jogadorId;
        this.gson = new Gson();
        this.conectado = false;
        this.jogoAtivo = false;
        this.scanner = new Scanner(System.in);
        configurarKafka();
    }

    private void configurarKafka() {

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.BOOTSTRAP_SERVERS);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfig.GROUP_ID_JOGADOR + jogadorId);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(KafkaConfig.TOPICO_ESTADO));

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.BOOTSTRAP_SERVERS);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer<>(producerProps);
    }

    public void conectar() {
        System.out.println("[" + jogadorId + "] Conectando ao servidor...\n");
        
        Thread receptorThread = new Thread(this::receberMensagens);
        receptorThread.setDaemon(true);
        receptorThread.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Mensagem msg = new Mensagem("CONECTAR", jogadorId);
        String json = gson.toJson(msg);
        producer.send(new ProducerRecord<>(KafkaConfig.TOPICO_JOGADAS, jogadorId, json));
        producer.flush();
    }

    private void receberMensagens() {
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                
                for (ConsumerRecord<String, String> record : records) {
                    if (record.key() != null && !record.key().equals(jogadorId)) {
                        continue;
                    }

                    String mensagemJson = record.value();
                    Mensagem mensagem = gson.fromJson(mensagemJson, Mensagem.class);
                    
                    tratarMensagem(mensagem);
                }
            }
        } catch (Exception e) {
            System.err.println("[" + jogadorId + "] Erro ao receber mensagens: " + e.getMessage());
        }
    }

    private void tratarMensagem(Mensagem mensagem) {
        switch (mensagem.getTipo()) {
            case "CONECTADO":
                conectado = true;
                System.out.println("========================================");
                System.out.println(mensagem.getConteudo());
                System.out.println("========================================\n");
                break;
            case "AGUARDANDO":
                System.out.println(mensagem.getConteudo() + "\n");
                break;
            case "ESTADO":
                jogoAtivo = true;
                System.out.println("\n========================================");
                exibirTabuleiro(mensagem.getTabuleiro());
                System.out.println(">>> " + mensagem.getConteudo() + " <<<");
                System.out.println("========================================\n");
                break;
            case "ERRO":
                System.out.println("\n❌ ERRO: " + mensagem.getConteudo() + "\n");
                break;
            case "FIM":
                jogoAtivo = false;
                System.out.println("\n========================================");
                System.out.println("         JOGO FINALIZADO!");
                System.out.println("========================================");
                exibirTabuleiro(mensagem.getTabuleiro());
                System.out.println(">>> " + mensagem.getConteudo() + " <<<");
                System.out.println("========================================");
                System.out.println("Pressione Ctrl + C para sair...\n");
                break;
        }
    }

    private void exibirTabuleiro(String tabuleiroStr) {
        if (tabuleiroStr == null || tabuleiroStr.length() != 9) {
            return;
        }

        System.out.println("\n  0 1 2");
        for (int i = 0; i < 3; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < 3; j++) {
                System.out.print(tabuleiroStr.charAt(i * 3 + j) + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public void jogar() {
        System.out.println("Digite suas jogadas no formato: linha coluna (ex: 0 1)");
        System.out.println("Pressione Ctrl + C para encerrar\n");

        while (true) {
            try {
                String entrada = scanner.nextLine().trim();

                if (entrada.equalsIgnoreCase("sair")) {
                    break;
                }

                if (!jogoAtivo) {
                    continue;
                }

                String[] partes = entrada.split(" ");
                if (partes.length != 2) {
                    System.out.println("Formato inválido! Use: linha coluna");
                    continue;
                }

                int linha = Integer.parseInt(partes[0]);
                int coluna = Integer.parseInt(partes[1]);

                enviarJogada(linha, coluna);

            } catch (NumberFormatException e) {
                System.out.println("Digite números válidos!");
            } catch (Exception e) {
                System.err.println("Erro: " + e.getMessage());
            }
        }

        fechar();
    }

    private void enviarJogada(int linha, int coluna) {
        Mensagem msg = new Mensagem("JOGADA", jogadorId);
        msg.setLinha(linha);
        msg.setColuna(coluna);

        String json = gson.toJson(msg);
        producer.send(new ProducerRecord<>(KafkaConfig.TOPICO_JOGADAS, jogadorId, json));
    }

    private void fechar() {
        if (consumer != null) consumer.close();
        if (producer != null) producer.close();
        if (scanner != null) scanner.close();
        System.out.println("Desconectado.");
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java Jogador <nome_jogador>");
            System.exit(1);
        }

        String nomeJogador = args[0];
        Jogador jogador = new Jogador(nomeJogador);
        jogador.conectar();
        jogador.jogar();
    }
}
