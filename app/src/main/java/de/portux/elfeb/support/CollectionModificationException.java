package de.portux.elfeb.support;

public class CollectionModificationException extends RuntimeException {

  public CollectionModificationException() {}

  public CollectionModificationException(String message) {
    super(message);
  }

  public CollectionModificationException(String message, Throwable cause) {
    super(message, cause);
  }

  public CollectionModificationException(Throwable cause) {
    super(cause);
  }
}
