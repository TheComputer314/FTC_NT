// Copyright (c) 2024-2025 FTC 8696
// All rights reserved.

package ftc.lib.trobotix;

/**
 * Represents an operation that accepts a single boolean-valued argument and returns no result. This
 * is the primitive type specialization of {@link java.util.function.Consumer} for boolean. Unlike
 * most other functional interfaces, BooleanConsumer is expected to operate via side effects.
 *
 * <p>This is a functional interface whose functional method is {@link #accept(boolean)}.
 */
@FunctionalInterface
public interface BooleanConsumer {
  /**
   * Performs this operation on the given argument.
   *
   * @param value the input argument
   */
  void accept(boolean value);
}
