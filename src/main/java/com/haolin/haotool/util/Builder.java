package com.haolin.haotool.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 通用的builder
 *
 * @param <T>
 * @author Ahaolin
 */
public class Builder<T> {
    private final Supplier<T> instance;
    private List<Consumer<T>> modifies;

    public Builder(Supplier<T> instance) {
        this.instance = instance;
    }

    public static <T> Builder<T> of(Supplier<T> instance) {
        return new Builder<>(instance);
    }

    public synchronized void initArray() {
        if (modifies == null) {
            modifies = new ArrayList<>();
        }
    }

    public <P1> Builder<T> with(Consumer1<T, P1> consumer, P1 p1) {
        Consumer<T> c = instance -> consumer.accept(instance, p1);
        initArray();
        modifies.add(c);
        return this;
    }

    public <P1, P2> Builder<T> with(Consumer2<T, P1, P2> consumer, P1 p1, P2 p2) {
        Consumer<T> c = instance -> consumer.accept(instance, p1, p2);
        initArray();
        modifies.add(c);
        return this;
    }

    public T build() {
        T value = instance.get();
        if (Objects.nonNull(modifies)) {
            modifies.forEach(modifies -> modifies.accept(value));
            modifies.clear();
        }
        return value;
    }

    @FunctionalInterface
    public interface Consumer1<T, P1> {
        void accept(T t, P1 p1);
    }

    @FunctionalInterface
    public interface Consumer2<T, P1, P2> {
        void accept(T t, P1 p1, P2 p2);
    }

}
