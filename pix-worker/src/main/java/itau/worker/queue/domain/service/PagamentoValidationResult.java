package itau.worker.queue.domain.service;

public class PagamentoValidationResult {
    private final boolean valid;
    private final String error;

    private PagamentoValidationResult(boolean valid, String error) {
        this.valid = valid;
        this.error = error;
    }

    public static PagamentoValidationResult ok() {
        return new PagamentoValidationResult(true, null);
    }

    public static PagamentoValidationResult fail(String error) {
        return new PagamentoValidationResult(false, error);
    }

    public boolean isValid() {
        return valid;
    }

    public String getError() {
        return error;
    }
}
