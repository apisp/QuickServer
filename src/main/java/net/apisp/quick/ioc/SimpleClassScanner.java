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

import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;

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
 * 简单的类扫描器
 * 
 * @author Ujued
 * @date 2018-06-15 11:08:11
 */
public class SimpleClassScanner extends ClassLoader implements ClassScanner {
    private static final Log LOG = LogFactory.getLog(SimpleClassScanner.class);
    private Set<Class<?>> classes = new HashSet<>();
    private Path rootPath;

    /**
     * 根据一个根URI和既定包名创建一个类扫描器实例
     * 
     * @param uri
     * @param packageName
     * @return
     */
    public static ClassScanner create(URI uri, String packageName) {
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

    /**
     * 获取路径下中标注了此注解的类
     */
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

    /**
     * 获取路径下实现了此接口的类
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<T>[] getByInterface(Class<T> ifce) {
        List<Class<?>> clses = new ArrayList<>();
        Iterator<Class<?>> clsIter = classes.iterator();
        if (!ifce.isInterface()) {
            return null;
        }
        while (clsIter.hasNext()) {
            Class<?> class1 = clsIter.next();
            if (ifce.isAssignableFrom(class1)) {
                clses.add(class1);
            }
        }
        Class<?>[] clss = new Class<?>[clses.size()];
        for (int i = 0; i < clss.length; i++) {
            clss[i] = clses.get(i);
        }
        return (Class<T>[]) clss;
    }

    @Override
    public Class<?>[] get(Class<?> cls) {
        return null;
    }
    
    /**
     * 收集路径下的类
     * 
     * @param root
     * @return
     */
    private Set<Class<?>> collect(Path root) {
        Stream<Path> list = null;
        try {
            list = Files.list(root);
            list.forEach(path -> {
                if (!path.toString().endsWith(".class")) {
                    collect(path);
                    return;
                }
                try {
                    classes.add(loadClass(path));
                }catch (LinkageError error){
                    LOG.warn(error.getMessage());
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

    /**
     * 自定义类加载。无缓存，每次加载最新的类
     * 
     * @param path
     * @return
     */
    private Class<?> loadClass(Path path) {
        byte[] classBin = null;
        try {
            classBin = Files.readAllBytes(path);
        } catch (IOException e) {
            // impossible
        }
        return this.defineClass(getClassNameByPath(path), classBin, 0, classBin.length);
    }
    
    /**
     * 由PATH解析出类名
     * 
     * @param t
     * @return
     */
    private String getClassNameByPath(Path t) {
        String tmp = t.subpath(rootPath.getNameCount(), t.getNameCount()).toString();
        return tmp.substring(0, tmp.length() - 6).replace('\\', '.').replace('/', '.');
    }
}