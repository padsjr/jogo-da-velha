package jogodavelha.modelo;

/**
 * Classe que representa o tabuleiro do jogo da velha
 */
public class Tabuleiro {
    private char[][] grade;
    private static final int TAMANHO = 3;

    public Tabuleiro() {
        grade = new char[TAMANHO][TAMANHO];
        inicializar();
    }

    private void inicializar() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                grade[i][j] = '-';
            }
        }
    }

    public synchronized boolean fazerJogada(int linha, int coluna, char simbolo) {
        if (linha < 0 || linha >= TAMANHO || coluna < 0 || coluna >= TAMANHO) {
            return false;
        }
        if (grade[linha][coluna] != '-') {
            return false;
        }
        grade[linha][coluna] = simbolo;
        return true;
    }

    public synchronized char verificarVencedor() {
        // Verificar linhas
        for (int i = 0; i < TAMANHO; i++) {
            if (grade[i][0] != '-' && grade[i][0] == grade[i][1] && grade[i][1] == grade[i][2]) {
                return grade[i][0];
            }
        }

        // Verificar colunas
        for (int j = 0; j < TAMANHO; j++) {
            if (grade[0][j] != '-' && grade[0][j] == grade[1][j] && grade[1][j] == grade[2][j]) {
                return grade[0][j];
            }
        }

        // Verificar diagonais
        if (grade[0][0] != '-' && grade[0][0] == grade[1][1] && grade[1][1] == grade[2][2]) {
            return grade[0][0];
        }
        if (grade[0][2] != '-' && grade[0][2] == grade[1][1] && grade[1][1] == grade[2][0]) {
            return grade[0][2];
        }

        return '-';
    }

    public synchronized boolean estaCompleto() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                if (grade[i][j] == '-') {
                    return false;
                }
            }
        }
        return true;
    }

    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n  0 1 2\n");
        for (int i = 0; i < TAMANHO; i++) {
            sb.append(i).append(" ");
            for (int j = 0; j < TAMANHO; j++) {
                sb.append(grade[i][j]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public synchronized String paraString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                sb.append(grade[i][j]);
            }
        }
        return sb.toString();
    }
}
