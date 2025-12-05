package jogodavelha.modelo;

/**
 * Classe que representa uma mensagem trocada via Kafka
 */
public class Mensagem {
    private String tipo; // CONECTAR, JOGADA, ESTADO, FIM
    private String jogadorId;
    private int linha;
    private int coluna;
    private String conteudo;
    private String tabuleiro;

    public Mensagem() {
    }

    public Mensagem(String tipo, String jogadorId) {
        this.tipo = tipo;
        this.jogadorId = jogadorId;
    }

    // Getters e Setters
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getJogadorId() {
        return jogadorId;
    }

    public void setJogadorId(String jogadorId) {
        this.jogadorId = jogadorId;
    }

    public int getLinha() {
        return linha;
    }

    public void setLinha(int linha) {
        this.linha = linha;
    }

    public int getColuna() {
        return coluna;
    }

    public void setColuna(int coluna) {
        this.coluna = coluna;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getTabuleiro() {
        return tabuleiro;
    }

    public void setTabuleiro(String tabuleiro) {
        this.tabuleiro = tabuleiro;
    }

    @Override
    public String toString() {
        return "Mensagem{" +
                "tipo='" + tipo + '\'' +
                ", jogadorId='" + jogadorId + '\'' +
                ", linha=" + linha +
                ", coluna=" + coluna +
                ", conteudo='" + conteudo + '\'' +
                '}';
    }
}
