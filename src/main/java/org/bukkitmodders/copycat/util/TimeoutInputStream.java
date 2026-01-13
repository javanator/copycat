package org.bukkitmodders.copycat.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.*;

public class TimeoutInputStream extends InputStream {
    private final InputStream delegate;
    private final long timeoutMillis;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TimeoutInputStream(InputStream delegate, long timeoutMillis) {
        this.delegate = delegate;
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public int read() throws IOException {
        return executeWithTimeout(() -> delegate.read());
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return executeWithTimeout(() -> delegate.read(b, off, len));
    }

    @Override
    public void close() throws IOException {
        try {
            delegate.close();
        } finally {
            executor.shutdownNow();
        }
    }

    private <T> T executeWithTimeout(Callable<T> task) throws IOException {
        Future<T> future = executor.submit(task);
        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new IOException("Read timeout exceeded", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Read interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(cause);
        }
    }
}
