package iudx.onboarding.server.apiserver.exceptions;

public class DxRuntimeException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private final int statusCode;
  private final String message;

  public DxRuntimeException(final int statusCode, final String message) {
    super(message);
    this.statusCode = statusCode;
    this.message = message;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getMessage() {
    return message;
  }
}
