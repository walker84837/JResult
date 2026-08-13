package org.winlogon.jresult;

/**
 * A supplier that may throw a checked exception of type {@code X}.
 * <p>
 * This is the exception-typed counterpart of {@link java.util.function.Supplier},
 * used by {@link Result#attempt(Class, ThrowingSupplier)} to capture only a
 * specific exception type while keeping the success path strongly typed.
 *
 * @param <X> the type of the exception that may be thrown.
 * @param <T> the type of the supplied value.
 */
@FunctionalInterface
public interface ThrowingSupplier<X extends Exception, T> {
    /**
     * Gets a result, possibly throwing a checked exception.
     *
     * @return the produced value.
     * @throws X if the computation fails.
     */
    T get() throws X;
}
