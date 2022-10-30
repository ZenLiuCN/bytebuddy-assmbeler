/*
 * Copyright (c) 2022. Zen Liu.
 * SPDX-License-Identifier: GPL-2.0-only WITH Classpath-exception-2.0
 */

package cn.zenliu.assembler;

import net.bytebuddy.build.BuildLogger;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Zen.Liu
 * @since 2022-10-30
 */
public class ByteBuddyPlugin implements Plugin {
    final File classRoot;
    final BuildLogger logger;
    final Junction<TypeDescription> matcher;
    final Map<Junction<TypeDescription>, List<Extension>> matchers;
    final List<Extension> extensions;

    public ByteBuddyPlugin(File classRoot, BuildLogger logger) {
        this.classRoot = classRoot;
        this.logger = logger;
        this.extensions = new ArrayList<>();
        ServiceLoader.load(Extension.class).forEach(this.extensions::add);
        matchers = new HashMap<>();
        Junction<TypeDescription> m = null;
        for (Extension ext : extensions) {
            Junction<TypeDescription> matcher = ext.matcher(classRoot, logger);
            matchers.computeIfAbsent(matcher, $ -> new ArrayList<>()).add(ext);
            m = m == null ? matcher : m.or(matcher);
        }
        matcher = m == null ? ElementMatchers.none() : m;
        matchers.values().forEach(Collections::sort);
    }
    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassFileLocator classFileLocator) {
        if (!extensions.isEmpty()){
            List<DynamicType.Builder<?>> extra=new ArrayList<>();
            for (Junction<TypeDescription> key : matchers.keySet()) {
                if (key.matches(typeDescription)) {
                    for (Extension ext : matchers.get(key)) {
                        builder = ext.apply(builder, typeDescription,extra, classFileLocator, classRoot, logger);
                    }
                }
            }

        }
        return builder;
    }
    @Override
    public void close() throws IOException {
        for (Extension ext : extensions) {
            ext.close();
        }
    }

    @Override
    public boolean matches(TypeDescription target) {
        return matcher.matches(target);
    }
}
