package jogodavelha.util;

public class KafkaConfig {
    public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String TOPICO_JOGADAS = "jogo-velha-jogadas";
    public static final String TOPICO_ESTADO = "jogo-velha-estado";
    public static final String GROUP_ID_SERVIDOR = "servidor-jogo";
    public static final String GROUP_ID_JOGADOR = "jogador-";
}
