/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractCheckedFuture;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.AsyncSettableFuture;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SerializingExecutor;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.common.util.concurrent.Uninterruptibles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

@Beta
public final class Futures {
    private static final AsyncFunction<ListenableFuture<Object>, Object> DEREFERENCER = new AsyncFunction<ListenableFuture<Object>, Object>(){

        @Override
        public ListenableFuture<Object> apply(ListenableFuture<Object> input) {
            return input;
        }
    };
    private static final Ordering<Constructor<?>> WITH_STRING_PARAM_FIRST = Ordering.natural().onResultOf(new Function<Constructor<?>, Boolean>(){

        @Override
        public Boolean apply(Constructor<?> input) {
            return Arrays.asList(input.getParameterTypes()).contains(String.class);
        }
    }).reverse();

    private Futures() {
    }

    public static <V, X extends Exception> CheckedFuture<V, X> makeChecked(ListenableFuture<V> future, Function<? super Exception, X> mapper) {
        return new MappingCheckedFuture<V, X>(Preconditions.checkNotNull(future), mapper);
    }

    public static <V> ListenableFuture<V> immediateFuture(@Nullable V value) {
        return new ImmediateSuccessfulFuture<V>(value);
    }

    public static <V, X extends Exception> CheckedFuture<V, X> immediateCheckedFuture(@Nullable V value) {
        return new ImmediateSuccessfulCheckedFuture(value);
    }

    public static <V> ListenableFuture<V> immediateFailedFuture(Throwable throwable) {
        Preconditions.checkNotNull(throwable);
        return new ImmediateFailedFuture(throwable);
    }

    public static <V> ListenableFuture<V> immediateCancelledFuture() {
        return new ImmediateCancelledFuture();
    }

    public static <V, X extends Exception> CheckedFuture<V, X> immediateFailedCheckedFuture(X exception) {
        Preconditions.checkNotNull(exception);
        return new ImmediateFailedCheckedFuture(exception);
    }

    public static <V> ListenableFuture<V> withFallback(ListenableFuture<? extends V> input, FutureFallback<? extends V> fallback) {
        return Futures.withFallback(input, fallback, MoreExecutors.directExecutor());
    }

    public static <V> ListenableFuture<V> withFallback(ListenableFuture<? extends V> input, FutureFallback<? extends V> fallback, Executor executor) {
        Preconditions.checkNotNull(fallback);
        return new FallbackFuture<V>(input, fallback, executor);
    }

    public static <I, O> ListenableFuture<O> transform(ListenableFuture<I> input, AsyncFunction<? super I, ? extends O> function) {
        ChainingListenableFuture output = new ChainingListenableFuture(function, input);
        input.addListener(output, MoreExecutors.directExecutor());
        return output;
    }

    public static <I, O> ListenableFuture<O> transform(ListenableFuture<I> input, AsyncFunction<? super I, ? extends O> function, Executor executor) {
        Preconditions.checkNotNull(executor);
        ChainingListenableFuture output = new ChainingListenableFuture(function, input);
        input.addListener(Futures.rejectionPropagatingRunnable(output, output, executor), MoreExecutors.directExecutor());
        return output;
    }

    private static Runnable rejectionPropagatingRunnable(final AbstractFuture<?> outputFuture, final Runnable delegateTask, final Executor delegateExecutor) {
        return new Runnable(){

            @Override
            public void run() {
                block2 : {
                    final AtomicBoolean thrownFromDelegate = new AtomicBoolean(true);
                    try {
                        delegateExecutor.execute(new Runnable(){

                            @Override
                            public void run() {
                                thrownFromDelegate.set(false);
                                delegateTask.run();
                            }
                        });
                    }
                    catch (RejectedExecutionException e) {
                        if (!thrownFromDelegate.get()) break block2;
                        outputFuture.setException(e);
                    }
                }
            }

        };
    }

    public static <I, O> ListenableFuture<O> transform(ListenableFuture<I> input, Function<? super I, ? extends O> function) {
        Preconditions.checkNotNull(function);
        ChainingListenableFuture output = new ChainingListenableFuture(Futures.asAsyncFunction(function), input);
        input.addListener(output, MoreExecutors.directExecutor());
        return output;
    }

    public static <I, O> ListenableFuture<O> transform(ListenableFuture<I> input, Function<? super I, ? extends O> function, Executor executor) {
        Preconditions.checkNotNull(function);
        return Futures.transform(input, Futures.asAsyncFunction(function), executor);
    }

    private static <I, O> AsyncFunction<I, O> asAsyncFunction(final Function<? super I, ? extends O> function) {
        return new AsyncFunction<I, O>(){

            @Override
            public ListenableFuture<O> apply(I input) {
                Object output = function.apply(input);
                return Futures.immediateFuture(output);
            }
        };
    }

    public static <I, O> Future<O> lazyTransform(final Future<I> input, final Function<? super I, ? extends O> function) {
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(function);
        return new Future<O>(){

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return input.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return input.isCancelled();
            }

            @Override
            public boolean isDone() {
                return input.isDone();
            }

            @Override
            public O get() throws InterruptedException, ExecutionException {
                return this.applyTransformation(input.get());
            }

            @Override
            public O get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return this.applyTransformation(input.get(timeout, unit));
            }

            private O applyTransformation(I input2) throws ExecutionException {
                try {
                    return (O)function.apply(input2);
                }
                catch (Throwable t) {
                    throw new ExecutionException(t);
                }
            }
        };
    }

    public static <V> ListenableFuture<V> dereference(ListenableFuture<? extends ListenableFuture<? extends V>> nested) {
        return Futures.transform(nested, DEREFERENCER);
    }

    @Beta
    public static /* varargs */ <V> ListenableFuture<List<V>> allAsList(ListenableFuture<? extends V> ... futures) {
        return Futures.listFuture(ImmutableList.copyOf(futures), true, MoreExecutors.directExecutor());
    }

    @Beta
    public static <V> ListenableFuture<List<V>> allAsList(Iterable<? extends ListenableFuture<? extends V>> futures) {
        return Futures.listFuture(ImmutableList.copyOf(futures), true, MoreExecutors.directExecutor());
    }

    public static <V> ListenableFuture<V> nonCancellationPropagating(ListenableFuture<V> future) {
        return new NonCancellationPropagatingFuture<V>(future);
    }

    @Beta
    public static /* varargs */ <V> ListenableFuture<List<V>> successfulAsList(ListenableFuture<? extends V> ... futures) {
        return Futures.listFuture(ImmutableList.copyOf(futures), false, MoreExecutors.directExecutor());
    }

    @Beta
    public static <V> ListenableFuture<List<V>> successfulAsList(Iterable<? extends ListenableFuture<? extends V>> futures) {
        return Futures.listFuture(ImmutableList.copyOf(futures), false, MoreExecutors.directExecutor());
    }

    @Beta
    public static <T> ImmutableList<ListenableFuture<T>> inCompletionOrder(Iterable<? extends ListenableFuture<? extends T>> futures) {
        final ConcurrentLinkedQueue delegates = Queues.newConcurrentLinkedQueue();
        ImmutableList.Builder listBuilder = ImmutableList.builder();
        SerializingExecutor executor = new SerializingExecutor(MoreExecutors.directExecutor());
        for (final ListenableFuture<T> future : futures) {
            AsyncSettableFuture delegate = AsyncSettableFuture.create();
            delegates.add(delegate);
            future.addListener(new Runnable(){

                @Override
                public void run() {
                    ((AsyncSettableFuture)delegates.remove()).setFuture(future);
                }
            }, executor);
            listBuilder.add(delegate);
        }
        return listBuilder.build();
    }

    public static <V> void addCallback(ListenableFuture<V> future, FutureCallback<? super V> callback) {
        Futures.addCallback(future, callback, MoreExecutors.directExecutor());
    }

    public static <V> void addCallback(final ListenableFuture<V> future, final FutureCallback<? super V> callback, Executor executor) {
        Preconditions.checkNotNull(callback);
        Runnable callbackListener = new Runnable(){

            @Override
            public void run() {
                Object value;
                try {
                    value = Uninterruptibles.getUninterruptibly(future);
                }
                catch (ExecutionException e) {
                    callback.onFailure(e.getCause());
                    return;
                }
                catch (RuntimeException e) {
                    callback.onFailure(e);
                    return;
                }
                catch (Error e) {
                    callback.onFailure(e);
                    return;
                }
                callback.onSuccess(value);
            }
        };
        future.addListener(callbackListener, executor);
    }

    public static <V, X extends Exception> V get(Future<V> future, Class<X> exceptionClass) throws Exception {
        Preconditions.checkNotNull(future);
        Preconditions.checkArgument(!RuntimeException.class.isAssignableFrom(exceptionClass), "Futures.get exception type (%s) must not be a RuntimeException", exceptionClass);
        try {
            return future.get();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw Futures.newWithCause(exceptionClass, e);
        }
        catch (ExecutionException e) {
            Futures.wrapAndThrowExceptionOrError(e.getCause(), exceptionClass);
            throw new AssertionError();
        }
    }

    public static <V, X extends Exception> V get(Future<V> future, long timeout, TimeUnit unit, Class<X> exceptionClass) throws Exception {
        Preconditions.checkNotNull(future);
        Preconditions.checkNotNull(unit);
        Preconditions.checkArgument(!RuntimeException.class.isAssignableFrom(exceptionClass), "Futures.get exception type (%s) must not be a RuntimeException", exceptionClass);
        try {
            return future.get(timeout, unit);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw Futures.newWithCause(exceptionClass, e);
        }
        catch (TimeoutException e) {
            throw Futures.newWithCause(exceptionClass, e);
        }
        catch (ExecutionException e) {
            Futures.wrapAndThrowExceptionOrError(e.getCause(), exceptionClass);
            throw new AssertionError();
        }
    }

    private static <X extends Exception> void wrapAndThrowExceptionOrError(Throwable cause, Class<X> exceptionClass) throws Exception {
        if (cause instanceof Error) {
            throw new ExecutionError((Error)cause);
        }
        if (cause instanceof RuntimeException) {
            throw new UncheckedExecutionException(cause);
        }
        throw Futures.newWithCause(exceptionClass, cause);
    }

    public static <V> V getUnchecked(Future<V> future) {
        Preconditions.checkNotNull(future);
        try {
            return Uninterruptibles.getUninterruptibly(future);
        }
        catch (ExecutionException e) {
            Futures.wrapAndThrowUnchecked(e.getCause());
            throw new AssertionError();
        }
    }

    private static void wrapAndThrowUnchecked(Throwable cause) {
        if (cause instanceof Error) {
            throw new ExecutionError((Error)cause);
        }
        throw new UncheckedExecutionException(cause);
    }

    private static <X extends Exception> X newWithCause(Class<X> exceptionClass, Throwable cause) {
        List<Constructor<X>> constructors = Arrays.asList(exceptionClass.getConstructors());
        for (Constructor<X> constructor : Futures.preferringStrings(constructors)) {
            Exception instance = (Exception)Futures.newFromConstructor(constructor, cause);
            if (instance == null) continue;
            if (instance.getCause() == null) {
                instance.initCause(cause);
            }
            return (X)instance;
        }
        String i$ = String.valueOf(String.valueOf(exceptionClass));
        throw new IllegalArgumentException(new StringBuilder(82 + i$.length()).append("No appropriate constructor for exception of type ").append(i$).append(" in response to chained exception").toString(), cause);
    }

    private static <X extends Exception> List<Constructor<X>> preferringStrings(List<Constructor<X>> constructors) {
        return WITH_STRING_PARAM_FIRST.sortedCopy(constructors);
    }

    @Nullable
    private static <X> X newFromConstructor(Constructor<X> constructor, Throwable cause) {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; ++i) {
            Class paramType = paramTypes[i];
            if (paramType.equals(String.class)) {
                params[i] = cause.toString();
                continue;
            }
            if (paramType.equals(Throwable.class)) {
                params[i] = cause;
                continue;
            }
            return null;
        }
        try {
            return constructor.newInstance(params);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
        catch (InstantiationException e) {
            return null;
        }
        catch (IllegalAccessException e) {
            return null;
        }
        catch (InvocationTargetException e) {
            return null;
        }
    }

    private static <V> ListenableFuture<List<V>> listFuture(ImmutableList<ListenableFuture<? extends V>> futures, boolean allMustSucceed, Executor listenerExecutor) {
        return new CombinedFuture(futures, allMustSucceed, listenerExecutor, new FutureCombiner<V, List<V>>(){

            @Override
            public List<V> combine(List<Optional<V>> values) {
                ArrayList<Object> result = Lists.newArrayList();
                for (Optional<V> element : values) {
                    result.add((element != null ? (Object)element.orNull() : null));
                }
                return Collections.unmodifiableList(result);
            }
        });
    }

    private static class MappingCheckedFuture<V, X extends Exception>
    extends AbstractCheckedFuture<V, X> {
        final Function<? super Exception, X> mapper;

        MappingCheckedFuture(ListenableFuture<V> delegate, Function<? super Exception, X> mapper) {
            super(delegate);
            this.mapper = Preconditions.checkNotNull(mapper);
        }

        @Override
        protected X mapException(Exception e) {
            return (X)((Exception)this.mapper.apply(e));
        }
    }

    private static class CombinedFuture<V, C>
    extends AbstractFuture<C> {
        private static final Logger logger = Logger.getLogger(CombinedFuture.class.getName());
        ImmutableCollection<? extends ListenableFuture<? extends V>> futures;
        final boolean allMustSucceed;
        final AtomicInteger remaining;
        FutureCombiner<V, C> combiner;
        List<Optional<V>> values;
        final Object seenExceptionsLock = new Object();
        Set<Throwable> seenExceptions;

        CombinedFuture(ImmutableCollection<? extends ListenableFuture<? extends V>> futures, boolean allMustSucceed, Executor listenerExecutor, FutureCombiner<V, C> combiner) {
            this.futures = futures;
            this.allMustSucceed = allMustSucceed;
            this.remaining = new AtomicInteger(futures.size());
            this.combiner = combiner;
            this.values = Lists.newArrayListWithCapacity(futures.size());
            this.init(listenerExecutor);
        }

        protected void init(Executor listenerExecutor) {
            int i;
            this.addListener(new Runnable(){

                @Override
                public void run() {
                    if (CombinedFuture.this.isCancelled()) {
                        for (ListenableFuture future : CombinedFuture.this.futures) {
                            future.cancel(CombinedFuture.this.wasInterrupted());
                        }
                    }
                    CombinedFuture.this.futures = null;
                    CombinedFuture.this.values = null;
                    CombinedFuture.this.combiner = null;
                }
            }, MoreExecutors.directExecutor());
            if (this.futures.isEmpty()) {
                this.set(this.combiner.combine(ImmutableList.of()));
                return;
            }
            for (i = 0; i < this.futures.size(); ++i) {
                this.values.add(null);
            }
            i = 0;
            for (final ListenableFuture listenable : this.futures) {
                final int index = i++;
                listenable.addListener(new Runnable(){

                    @Override
                    public void run() {
                        CombinedFuture.this.setOneValue(index, listenable);
                    }
                }, listenerExecutor);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void setExceptionAndMaybeLog(Throwable throwable) {
            boolean visibleFromOutputFuture = false;
            boolean firstTimeSeeingThisException = true;
            if (this.allMustSucceed) {
                visibleFromOutputFuture = super.setException(throwable);
                Object object = this.seenExceptionsLock;
                synchronized (object) {
                    if (this.seenExceptions == null) {
                        this.seenExceptions = Sets.newHashSet();
                    }
                    firstTimeSeeingThisException = this.seenExceptions.add(throwable);
                }
            }
            if (throwable instanceof Error || this.allMustSucceed && !visibleFromOutputFuture && firstTimeSeeingThisException) {
                logger.log(Level.SEVERE, "input future failed.", throwable);
            }
        }

        /*
         * Exception decompiling
         */
        private void setOneValue(int index, Future<? extends V> future) {
            // This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
            // org.benf.cfr.reader.util.ConfusedCFRException: Started 2 blocks at once
            // org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:395)
            // org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:473)
            // org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:2880)
            // org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:818)
            // org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:196)
            // org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:141)
            // org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:95)
            // org.benf.cfr.reader.entities.Method.analyse(Method.java:370)
            // org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:852)
            // org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:734)
            // org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:832)
            // org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:753)
            // org.benf.cfr.reader.Main.doJar(Main.java:134)
            // org.benf.cfr.reader.Main.main(Main.java:188)
            throw new IllegalStateException("Decompilation failed");
        }

    }

    private static interface FutureCombiner<V, C> {
        public C combine(List<Optional<V>> var1);
    }

    private static class NonCancellationPropagatingFuture<V>
    extends AbstractFuture<V> {
        NonCancellationPropagatingFuture(final ListenableFuture<V> delegate) {
            Preconditions.checkNotNull(delegate);
            Futures.addCallback(delegate, new FutureCallback<V>(){

                @Override
                public void onSuccess(V result) {
                    NonCancellationPropagatingFuture.this.set(result);
                }

                @Override
                public void onFailure(Throwable t) {
                    if (delegate.isCancelled()) {
                        NonCancellationPropagatingFuture.this.cancel(false);
                    } else {
                        NonCancellationPropagatingFuture.this.setException(t);
                    }
                }
            }, MoreExecutors.directExecutor());
        }

    }

    private static final class CombinerFuture<V>
    extends ListenableFutureTask<V> {
        ImmutableList<ListenableFuture<?>> futures;

        CombinerFuture(Callable<V> callable, ImmutableList<ListenableFuture<?>> futures) {
            super(callable);
            this.futures = futures;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            ImmutableList futures = this.futures;
            if (super.cancel(mayInterruptIfRunning)) {
                for (ListenableFuture future : futures) {
                    future.cancel(mayInterruptIfRunning);
                }
                return true;
            }
            return false;
        }

        @Override
        protected void done() {
            super.done();
            this.futures = null;
        }

        @Override
        protected void setException(Throwable t) {
            super.setException(t);
        }
    }

    private static final class WrappedCombiner<T>
    implements Callable<T> {
        final Callable<T> delegate;
        CombinerFuture<T> outputFuture;

        WrappedCombiner(Callable<T> delegate) {
            this.delegate = Preconditions.checkNotNull(delegate);
        }

        @Override
        public T call() throws Exception {
            try {
                return this.delegate.call();
            }
            catch (ExecutionException e) {
                this.outputFuture.setException(e.getCause());
            }
            catch (CancellationException e) {
                this.outputFuture.cancel(false);
            }
            return null;
        }
    }

    private static class ChainingListenableFuture<I, O>
    extends AbstractFuture<O>
    implements Runnable {
        private AsyncFunction<? super I, ? extends O> function;
        private ListenableFuture<? extends I> inputFuture;
        private volatile ListenableFuture<? extends O> outputFuture;

        private ChainingListenableFuture(AsyncFunction<? super I, ? extends O> function, ListenableFuture<? extends I> inputFuture) {
            this.function = Preconditions.checkNotNull(function);
            this.inputFuture = Preconditions.checkNotNull(inputFuture);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (super.cancel(mayInterruptIfRunning)) {
                this.cancel(this.inputFuture, mayInterruptIfRunning);
                this.cancel(this.outputFuture, mayInterruptIfRunning);
                return true;
            }
            return false;
        }

        private void cancel(@Nullable Future<?> future, boolean mayInterruptIfRunning) {
            if (future != null) {
                future.cancel(mayInterruptIfRunning);
            }
        }

        /*
         * Exception decompiling
         */
        @Override
        public void run() {
            // This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
            // java.lang.ClassCastException: org.benf.cfr.reader.bytecode.analysis.parse.statement.Nop cannot be cast to org.benf.cfr.reader.bytecode.analysis.parse.statement.TryStatement
            // org.benf.cfr.reader.bytecode.analysis.parse.utils.finalhelp.FinalAnalyzer.identifyFinally(FinalAnalyzer.java:163)
            // org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.FinallyRewriter.identifyFinally(FinallyRewriter.java:40)
            // org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:463)
            // org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:196)
            // org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:141)
            // org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:95)
            // org.benf.cfr.reader.entities.Method.analyse(Method.java:370)
            // org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:852)
            // org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:734)
            // org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:832)
            // org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:753)
            // org.benf.cfr.reader.Main.doJar(Main.java:134)
            // org.benf.cfr.reader.Main.main(Main.java:188)
            throw new IllegalStateException("Decompilation failed");
        }

    }

    private static class FallbackFuture<V>
    extends AbstractFuture<V> {
        private volatile ListenableFuture<? extends V> running;

        FallbackFuture(ListenableFuture<? extends V> input, final FutureFallback<? extends V> fallback, Executor executor) {
            this.running = input;
            Futures.addCallback(this.running, new FutureCallback<V>(){

                @Override
                public void onSuccess(V value) {
                    FallbackFuture.this.set(value);
                }

                @Override
                public void onFailure(Throwable t) {
                    if (FallbackFuture.this.isCancelled()) {
                        return;
                    }
                    try {
                        FallbackFuture.this.running = fallback.create(t);
                        if (FallbackFuture.this.isCancelled()) {
                            FallbackFuture.this.running.cancel(FallbackFuture.this.wasInterrupted());
                            return;
                        }
                        Futures.addCallback(FallbackFuture.this.running, new FutureCallback<V>(){

                            @Override
                            public void onSuccess(V value) {
                                FallbackFuture.this.set(value);
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                if (FallbackFuture.this.running.isCancelled()) {
                                    FallbackFuture.this.cancel(false);
                                } else {
                                    FallbackFuture.this.setException(t);
                                }
                            }
                        }, MoreExecutors.directExecutor());
                    }
                    catch (Throwable e) {
                        FallbackFuture.this.setException(e);
                    }
                }

            }, executor);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (super.cancel(mayInterruptIfRunning)) {
                this.running.cancel(mayInterruptIfRunning);
                return true;
            }
            return false;
        }

    }

    private static class ImmediateFailedCheckedFuture<V, X extends Exception>
    extends ImmediateFuture<V>
    implements CheckedFuture<V, X> {
        private final X thrown;

        ImmediateFailedCheckedFuture(X thrown) {
            super();
            this.thrown = thrown;
        }

        @Override
        public V get() throws ExecutionException {
            throw new ExecutionException((Throwable)this.thrown);
        }

        @Override
        public V checkedGet() throws Exception {
            throw this.thrown;
        }

        @Override
        public V checkedGet(long timeout, TimeUnit unit) throws Exception {
            Preconditions.checkNotNull(unit);
            throw this.thrown;
        }
    }

    private static class ImmediateCancelledFuture<V>
    extends ImmediateFuture<V> {
        private final CancellationException thrown = new CancellationException("Immediate cancelled future.");

        ImmediateCancelledFuture() {
            super();
        }

        @Override
        public boolean isCancelled() {
            return true;
        }

        @Override
        public V get() {
            throw AbstractFuture.cancellationExceptionWithCause("Task was cancelled.", this.thrown);
        }
    }

    private static class ImmediateFailedFuture<V>
    extends ImmediateFuture<V> {
        private final Throwable thrown;

        ImmediateFailedFuture(Throwable thrown) {
            super();
            this.thrown = thrown;
        }

        @Override
        public V get() throws ExecutionException {
            throw new ExecutionException(this.thrown);
        }
    }

    private static class ImmediateSuccessfulCheckedFuture<V, X extends Exception>
    extends ImmediateFuture<V>
    implements CheckedFuture<V, X> {
        @Nullable
        private final V value;

        ImmediateSuccessfulCheckedFuture(@Nullable V value) {
            super();
            this.value = value;
        }

        @Override
        public V get() {
            return this.value;
        }

        @Override
        public V checkedGet() {
            return this.value;
        }

        @Override
        public V checkedGet(long timeout, TimeUnit unit) {
            Preconditions.checkNotNull(unit);
            return this.value;
        }
    }

    private static class ImmediateSuccessfulFuture<V>
    extends ImmediateFuture<V> {
        @Nullable
        private final V value;

        ImmediateSuccessfulFuture(@Nullable V value) {
            super();
            this.value = value;
        }

        @Override
        public V get() {
            return this.value;
        }
    }

    private static abstract class ImmediateFuture<V>
    implements ListenableFuture<V> {
        private static final Logger log = Logger.getLogger(ImmediateFuture.class.getName());

        private ImmediateFuture() {
        }

        @Override
        public void addListener(Runnable listener, Executor executor) {
            Preconditions.checkNotNull(listener, "Runnable was null.");
            Preconditions.checkNotNull(executor, "Executor was null.");
            try {
                executor.execute(listener);
            }
            catch (RuntimeException e) {
                String string = String.valueOf(String.valueOf(listener));
                String string2 = String.valueOf(String.valueOf(executor));
                log.log(Level.SEVERE, new StringBuilder(57 + string.length() + string2.length()).append("RuntimeException while executing runnable ").append(string).append(" with executor ").append(string2).toString(), e);
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public abstract V get() throws ExecutionException;

        @Override
        public V get(long timeout, TimeUnit unit) throws ExecutionException {
            Preconditions.checkNotNull(unit);
            return this.get();
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }
    }

}

