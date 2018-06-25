/**
 * Copyright (c) 2018 Ujued and APISP.NET. All Rights Reserved.
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
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
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
public class SimpleClassScanner implements ClassScanner {
    private Set<Class<?>> classes = new HashSet<>();
    private Path rootPath;

    private Set<Class<?>> collect(Path root) {
        Stream<Path> list = null;
        try {
            list = Files.list(root);
            list.forEach(new Consumer<Path>() {
                @Override
                public void accept(Path t) {
                    if (!t.toString().endsWith(".class")) {
                        collect(t);
                        return;
                    }
                    String className = getClassNameByPath(t);
                    try {
                        classes.add(ClassLoader.getSystemClassLoader().loadClass(className));
                    } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    }
                }

                private String getClassNameByPath(Path t) {
                    String tmp = t.subpath(rootPath.getNameCount(), t.getNameCount()).toString();
                    return tmp.substring(0, tmp.length() - 6).replace('\\', '.').replace('/', '.');
                }
            });

        } catch (IOException e) {
        } finally {
            if (list != null) {
                list.close();
            }
        }
        return classes;
    }

    public static SimpleClassScanner create(URI uri, String packageName) {
        SimpleClassScanner classScanner = new SimpleClassScanner();
        if (uri.getScheme().equals("jar")) {
            try {
                FileSystem zipfs = FileSystems.newFileSystem(uri, new HashMap<>());
                classScanner.rootPath = zipfs.getPath("/");
            } catch (FileSystemAlreadyExistsException e) {
                classScanner.rootPath = Paths.get(uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            classScanner.rootPath = Paths.get(uri);
        }
        classScanner.collect(classScanner.rootPath.resolve(packageName.replace('.', '/')));
        return classScanner;
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
        List<Class<?>> clses = new ArrayList<>();
        Iterator<Class<?>> clsIter = classes.iterator();
        if (!ifce.isInterface()) {
            return null;
        }
        while (clsIter.hasNext()) {
            Class<?> class1 = (Class<?>) clsIter.next();
            if (ifce.isAssignableFrom(class1)) {
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
    public Class<?>[] get(Class<?> cls) {
        return null;
    }

}
