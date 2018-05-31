/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.google.common.base;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Platform;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

@GwtCompatible
public final class Suppliers {
    private Suppliers() {
    }

    public static <F, T> Supplier<T> compose(Function<? super F, T> function, Supplier<F> supplier) {
        Preconditions.checkNotNull(function);
        Preconditions.checkNotNull(supplier);
        return new SupplierComposition<F, T>(function, supplier);
    }

    public static <T> Supplier<T> memoize(Supplier<T> delegate) {
        return delegate instanceof MemoizingSupplier ? delegate : new MemoizingSupplier(Preconditions.checkNotNull(delegate));
    }

    public static <T> Supplier<T> memoizeWithExpiration(Supplier<T> delegate, long duration, TimeUnit unit) {
        return new ExpiringMemoizingSupplier<T>(delegate, duration, unit);
    }

    public static <T> Supplier<T> ofInstance(@Nullable T instance) {
        return new SupplierOfInstance<T>(instance);
    }

    public static <T> Supplier<T> synchronizedSupplier(Supplier<T> delegate) {
        return new ThreadSafeSupplier<T>(Preconditions.checkNotNull(delegate));
    }

    @Beta
    public static <T> Function<Supplier<T>, T> supplierFunction() {
        SupplierFunctionImpl sf = SupplierFunctionImpl.INSTANCE;
        return sf;
    }

    private static enum SupplierFunctionImpl implements SupplierFunction<Object>
    {
        INSTANCE;
        

        private SupplierFunctionImpl() {
        }

        @Override
        public Object apply(Supplier<Object> input) {
            return input.get();
        }

        public String toString() {
            return "Suppliers.supplierFunction()";
        }
    }

    private static interface SupplierFunction<T>
    extends Function<Supplier<T>, T> {
    }

    private static class ThreadSafeSupplier<T>
    implements Supplier<T>,
    Serializable {
        final Supplier<T> delegate;
        private static final long serialVersionUID = 0L;

        ThreadSafeSupplier(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public T get() {
            Supplier<T> supplier = this.delegate;
            synchronized (supplier) {
                return this.delegate.get();
            }
        }

        public String toString() {
            String string = String.valueOf(String.valueOf(this.delegate));
            return new StringBuilder(32 + string.length()).append("Suppliers.synchronizedSupplier(").append(string).append(")").toString();
        }
    }

    private static class SupplierOfInstance<T>
    implements Supplier<T>,
    Serializable {
        final T instance;
        private static final long serialVersionUID = 0L;

        SupplierOfInstance(@Nullable T instance) {
            this.instance = instance;
        }

        @Override
        public T get() {
            return this.instance;
        }

        public boolean equals(@Nullable Object obj) {
            if (obj instanceof SupplierOfInstance) {
                SupplierOfInstance that = (SupplierOfInstance)obj;
                return Objects.equal(this.instance, that.instance);
            }
            return false;
        }

        public int hashCode() {
            return Objects.hashCode(this.instance);
        }

        public String toString() {
            String string = String.valueOf(String.valueOf(this.instance));
            return new StringBuilder(22 + string.length()).append("Suppliers.ofInstance(").append(string).append(")").toString();
        }
    }

    @VisibleForTesting
    static class ExpiringMemoizingSupplier<T>
    implements Supplier<T>,
    Serializable {
        final Supplier<T> delegate;
        final long durationNanos;
        volatile transient T value;
        volatile transient long expirationNanos;
        private static final long serialVersionUID = 0L;

        ExpiringMemoizingSupplier(Supplier<T> delegate, long duration, TimeUnit unit) {
            this.delegate = Preconditions.checkNotNull(delegate);
            this.durationNanos = unit.toNanos(duration);
            Preconditions.checkArgument(duration > 0L);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public T get() {
            long nanos = this.expirationNanos;
            long now = Platform.systemNanoTime();
            if (nanos == 0L || now - nanos >= 0L) {
                ExpiringMemoizingSupplier expiringMemoizingSupplier = this;
                synchronized (expiringMemoizingSupplier) {
                    if (nanos == this.expirationNanos) {
                        T t = this.delegate.get();
                        this.value = t;
                        nanos = now + this.durationNanos;
                        this.expirationNanos = nanos == 0L ? 1L : nanos;
                        return t;
                    }
                }
            }
            return this.value;
        }

        public String toString() {
            String string = String.valueOf(String.valueOf(this.delegate));
            long l = this.durationNanos;
            return new StringBuilder(62 + string.length()).append("Suppliers.memoizeWithExpiration(").append(string).append(", ").append(l).append(", NANOS)").toString();
        }
    }

    @VisibleForTesting
    static class MemoizingSupplier<T>
    implements Supplier<T>,
    Serializable {
        final Supplier<T> delegate;
        volatile transient boolean initialized;
        transient T value;
        private static final long serialVersionUID = 0L;

        MemoizingSupplier(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public T get() {
            if (!this.initialized) {
                MemoizingSupplier memoizingSupplier = this;
                synchronized (memoizingSupplier) {
                    if (!this.initialized) {
                        T t = this.delegate.get();
                        this.value = t;
                        this.initialized = true;
                        return t;
                    }
                }
            }
            return this.value;
        }

        public String toString() {
            String string = String.valueOf(String.valueOf(this.delegate));
            return new StringBuilder(19 + string.length()).append("Suppliers.memoize(").append(string).append(")").toString();
        }
    }

    private static class SupplierComposition<F, T>
    implements Supplier<T>,
    Serializable {
        final Function<? super F, T> function;
        final Supplier<F> supplier;
        private static final long serialVersionUID = 0L;

        SupplierComposition(Function<? super F, T> function, Supplier<F> supplier) {
            this.function = function;
            this.supplier = supplier;
        }

        @Override
        public T get() {
            return this.function.apply(this.supplier.get());
        }

        public boolean equals(@Nullable Object obj) {
            if (obj instanceof SupplierComposition) {
                SupplierComposition that = (SupplierComposition)obj;
                return this.function.equals(that.function) && this.supplier.equals(that.supplier);
            }
            return false;
        }

        public int hashCode() {
            return Objects.hashCode(this.function, this.supplier);
        }

        public String toString() {
            String string = String.valueOf(String.valueOf(this.function));
            String string2 = String.valueOf(String.valueOf(this.supplier));
            return new StringBuilder(21 + string.length() + string2.length()).append("Suppliers.compose(").append(string).append(", ").append(string2).append(")").toString();
        }
    }

}

