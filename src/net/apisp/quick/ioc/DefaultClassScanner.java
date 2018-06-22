/**
 * Copyright 2018-present, APISP.NET.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.apisp.quick.ioc;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author UJUED
 * @date 2018-06-15 11:08:11
 */
public class DefaultClassScanner implements ClassScanner {
    private Set<Class<?>> classes = new HashSet<>();
    private Path rootPath;
    {
        try {
            rootPath = Paths.get(ClassLoader.getSystemResource("").toURI());
        } catch (URISyntaxException e) {
        }
    }

    public DefaultClassScanner(Path path) {
        collect(path);
    }

    private Set<Class<?>> collect(Path root) {
        Stream<Path> list = null;
        try {
            list = Files.list(root).filter((t) -> {
                if (t.toFile().isDirectory()) {
                    collect(t);
                    return false;
                }
                return true;
            });
            list.forEach(new Consumer<Path>() {
                @Override
                public void accept(Path t) {
                    if (!t.toString().endsWith(".class")) {
                        return;
                    }
                    String className = getClassNameByPath(t);
                    try {
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                    }
                }

                private String getClassNameByPath(Path t) {
                    String tmp = t.subpath(rootPath.getNameCount(), t.getNameCount()).toString();
                    return tmp.substring(0, tmp.length() - 6).replace('\\', '.');
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    public static DefaultClassScanner create(String packageName) {
        URI uri = null;
        try {
            uri = ClassLoader.getSystemResource(packageName.replace('.', '/')).toURI();
        } catch (NullPointerException e) {
            throw new RuntimeException("没有此包");
        } catch (URISyntaxException e) {
        }
        return new DefaultClassScanner(Paths.get(uri));
    }

    @Override
    public Class<?>[] getByAnnotation(Class<? extends Annotation> anno) {
        List<Class<?>> clses = new ArrayList<>();
        Iterator<Class<?>> clsIter = classes.iterator();
        if (!anno.isAnnotation()) {
            return null;
        }
        while (clsIter.hasNext()) {
            Class<?> class1 = (Class<?>) clsIter.next();
            if (class1.getAnnotation(anno) != null) {
                clses.add(class1);
            }
        }
        Class<?>[] clss = new Class<?>[clses.size()];
        for (int i = 0; i < clss.length; i++) {
            clss[i] = clses.get(i);
        }
        return clss;
    }

    @Override
    public Class<?>[] getByInterface(Class<?> ifce) {
        return null;
    }

    @Override
    public Class<?>[] get(Class<?> cls) {
        // TODO Auto-generated method stub
        return null;
    }

}
