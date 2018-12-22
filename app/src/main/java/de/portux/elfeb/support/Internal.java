package de.portux.elfeb.support;

/**
 * Indicates that a visible method should not be used outside of some scope. It is only visible due
 * to some limitation of a framework or the used Java version.
 * @author Rico Bergmann
 */
public @interface Internal {

  enum Scope {
    PRIVATE, PACKAGE, PROTECTED
  }

  Scope scope() default Scope.PRIVATE;

}
