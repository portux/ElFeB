package de.portux.elfeb.support;

/**
 * Collection of useful assertions. Failure will usually result in an {@link
 * IllegalArgumentException}.
 *
 * @author Rico Bergmann
 */
public class Assert {

  /**
   * Asserts that some object is not {@code null}.
   *
   * @param obj the object to check
   * @param message message to attach to the exception in case the assertion failed
   */
  public static void notNull(Object obj, String message) {
    if (obj == null) {
      reportFailure(message);
    }
  }

  /**
   * Asserts that none of the elements in an {@link Iterable}, nor the {@code iterable} itself are
   * {@code null}.
   *
   * @param objs the iterable to check
   * @param message message to attach to the exception in case the assertion failed
   */
  public static void noNullElements(Iterable<?> objs, String message) {
    notNull(objs, "Iterable was null");
    for (Object obj : objs) {
      notNull(obj, message);
    }
  }

  /**
   * Asserts that a condition holds.
   * @param cond the condition
   * @param message message to attach to the exception in case the assertion failed
   */
  public static void isTrue(boolean cond, String message) {
    if (!cond) {
      reportFailure(message);
    }
  }

  /**
   * Throws an {@link IllegalArgumentException} with a dedicated message.
   */
  private static void reportFailure(String msg) {
    throw new IllegalArgumentException(msg);
  }

}
