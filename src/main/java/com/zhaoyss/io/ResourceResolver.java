package com.zhaoyss.io;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 扫描指定包下的 .class 文件
 */
public class ResourceResolver {


    String basePackage;

    public ResourceResolver(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * 扫描 basePackage,返回 basePackage 下的所有 Class
     */
    public List<Class<?>> scan() {
        final String pkgPath = basePackage.replace('.', '/');
        final ArrayList<Class<?>> allClasses = new ArrayList<>();
        try {
            final URI pkg = Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(pkgPath).toURI());
            Path root;
            if (pkg.toString().startsWith("jar:")) {
                try {
                    root = FileSystems.getFileSystem(pkg).getPath(pkgPath);
                } catch (final FileSystemNotFoundException e) {
                    root = FileSystems.newFileSystem(pkg, Collections.emptyMap()).getPath(pkgPath);
                }
            } else {
                root = Paths.get(pkg);
            }

            final String extension = ".class";
            try (final Stream<Path> allPaths = Files.walk(root)) {
                allPaths.filter(Files::isRegularFile).forEach(file -> {
                    try {
                        final String path = file.toString().replace('\\', '.').replace("//", ".");
                        final String name = path.substring(path.indexOf(basePackage), path.length() - extension.length());
                        allClasses.add(Class.forName(name));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        return allClasses;
    }
}
