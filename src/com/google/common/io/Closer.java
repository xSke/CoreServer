/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.google.common.io;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

@Beta
public final class Closer
implements Closeable {
    private static final Suppressor SUPPRESSOR = SuppressingSuppressor.isAvailable() ? SuppressingSuppressor.INSTANCE : LoggingSuppressor.INSTANCE;
    @VisibleForTesting
    final Suppressor suppressor;
    private final Deque<Closeable> stack = new ArrayDeque<Closeable>(4);
    private Throwable thrown;

    public static Closer create() {
        return new Closer(SUPPRESSOR);
    }

    @VisibleForTesting
    Closer(Suppressor suppressor) {
        this.suppressor = Preconditions.checkNotNull(suppressor);
    }

    public <C extends Closeable> C register(@Nullable C closeable) {
        if (closeable != null) {
            this.stack.addFirst((Closeable)closeable);
        }
        return closeable;
    }

    public RuntimeException rethrow(Throwable e) throws IOException {
        Preconditions.checkNotNull(e);
        this.thrown = e;
        Throwables.propagateIfPossible(e, IOException.class);
        throw new RuntimeException(e);
    }

    public <X extends Exception> RuntimeException rethrow(Throwable e, Class<X> declaredType) throws IOException, Exception {
        Preconditions.checkNotNull(e);
        this.thrown = e;
        Throwables.propagateIfPossible(e, IOException.class);
        Throwables.propagateIfPossible(e, declaredType);
        throw new RuntimeException(e);
    }

    public <X1 extends Exception, X2 extends Exception> RuntimeException rethrow(Throwable e, Class<X1> declaredType1, Class<X2> declaredType2) throws IOException, Exception {
        Preconditions.checkNotNull(e);
        this.thrown = e;
        Throwables.propagateIfPossible(e, IOException.class);
        Throwables.propagateIfPossible(e, declaredType1, declaredType2);
        throw new RuntimeException(e);
    }

    @Override
    public void close() throws IOException {
        Throwable throwable = this.thrown;
        while (!this.stack.isEmpty()) {
            Closeable closeable = this.stack.removeFirst();
            try {
                closeable.close();
            }
            catch (Throwable e) {
                if (throwable == null) {
                    throwable = e;
                    continue;
                }
                this.suppressor.suppress(closeable, throwable, e);
            }
        }
        if (this.thrown == null && throwable != null) {
            Throwables.propagateIfPossible(throwable, IOException.class);
            throw new AssertionError(throwable);
        }
    }

    @VisibleForTesting
    static final class SuppressingSuppressor
    implements Suppressor {
        static final SuppressingSuppressor INSTANCE = new SuppressingSuppressor();
        static final Method addSuppressed = SuppressingSuppressor.getAddSuppressed();

        SuppressingSuppressor() {
        }

        static boolean isAvailable() {
            return addSuppressed != null;
        }

        private static Method getAddSuppressed() {
            try {
                return Throwable.class.getMethod("addSuppressed", Throwable.class);
            }
            catch (Throwable e) {
                return null;
            }
        }

        @Override
        public void suppress(Closeable closeable, Throwable thrown, Throwable suppressed) {
            if (thrown == suppressed) {
                return;
            }
            try {
                addSuppressed.invoke(thrown, suppressed);
            }
            catch (Throwable e) {
                LoggingSuppressor.INSTANCE.suppress(closeable, thrown, suppressed);
            }
        }
    }

    @VisibleForTesting
    static final class LoggingSuppressor
    implements Suppressor {
        static final LoggingSuppressor INSTANCE = new LoggingSuppressor();

        LoggingSuppressor() {
        }

        @Override
        public void suppress(Closeable closeable, Throwable thrown, Throwable suppressed) {
            String string = String.valueOf(String.valueOf(closeable));
            Closeables.logger.log(Level.WARNING, new StringBuilder(42 + string.length()).append("Suppressing exception thrown when closing ").append(string).toString(), suppressed);
        }
    }

    @VisibleForTesting
    static interface Suppressor {
        public void suppress(Closeable var1, Throwable var2, Throwable var3);
    }

}

