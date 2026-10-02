package itau.pix.commons.enums;

public enum StatusPagamento {
    PROCESSANDO,
    SUCESSO,
    FALHOU;

    public boolean isTerminal() {
        return this == SUCESSO || this == FALHOU;
    }
}
