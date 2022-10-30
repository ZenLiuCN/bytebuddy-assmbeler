/*
 * Copyright (c) 2022. Zen Liu.
 * SPDX-License-Identifier: GPL-2.0-only WITH Classpath-exception-2.0
 */

package cn.zenliu.assembler;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

/**
 * TypeBuilder use to Wrap {@link net.bytebuddy.dynamic.DynamicType.Builder}
 *
 * @author Zen.Liu
 * @since 2022-10-29
 */
@SuppressWarnings("rawtypes")
public interface TypeBuilder {
    AtomicReference<DynamicType.Builder> $();

    default TypeBuilder sync(UnaryOperator<DynamicType.Builder<?>> act) {
        AtomicReference<DynamicType.Builder> h = $();
        h.set(act.apply(h.get()));
        return this;
    }

    default TypeBuilder method(MethodDescription m, Implementation impl) {
        return sync(b -> b.define(m).intercept(impl));
    }
    default TypeBuilder methodManual(MethodDescription m, UnaryOperator<Assembler.Manual> impl) {
        return sync(b -> b.define(m).intercept(impl.apply(Assembler.manual()).implementation()));
    }
    default TypeBuilder methodCompute(MethodDescription m, UnaryOperator<Assembler.Compute> impl) {
        return sync(b -> b.define(m).intercept(impl.apply(Assembler.compute(b.toTypeDescription(),m)).implementation()));
    }

    static TypeBuilder with(DynamicType.Builder b) {
        AtomicReference<DynamicType.Builder> h = new AtomicReference<>(b);
        return () -> h;
    }
}
