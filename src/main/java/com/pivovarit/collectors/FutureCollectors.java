package com.pivovarit.collectors;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author Grzegorz Piwowarek
 */
class FutureCollectors {
    static <T, R> Collector<CompletableFuture<T>, ?, CompletableFuture<R>> toFuture(Collector<T, ?, R> collector) {
        return Collectors.collectingAndThen(toList(), list -> {
            CompletableFuture<R> future = CompletableFuture
              .allOf(list.toArray(new CompletableFuture[0]))
              .thenApply(__ -> list.stream()
                .map(CompletableFuture::join)
                .collect(collector));

            for (CompletableFuture<T> f : list) {
                f.exceptionally((throwable) -> {
                    future.completeExceptionally(throwable);
                    return null;
                });
            }

            return future;
        });
    }

    static <T> Collector<CompletableFuture<T>, ?, CompletableFuture<List<T>>> toFuture() {
        return toFuture(toList());
    }
}
