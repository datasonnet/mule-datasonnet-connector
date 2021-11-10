package org.mule.extension.datasonnet.internal;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;
import org.apache.commons.io.IOUtils;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.dsl.xml.ParameterDsl;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations(MuledatasonnetOperations.class)
//@ConnectionProviders(MulejsonnetConnectionProvider.class)
public class MuledatasonnetConfiguration {

    @Parameter
    @Optional
    @ParameterDsl(allowInlineDefinition = true)
    private List<String> librarySearchPaths;

    private Map<String, String> namedImports = new HashMap<>();
//    private List<String> supportedMimeTypes = new ArrayList<>();

    public Map<String, String> getNamedImports() {
        return namedImports;
    }

    public List<String> getLibrarySearchPaths() {
        return librarySearchPaths;
    }

//    public List<String> getSupportedMimeTypes() {
//        return supportedMimeTypes;
//    }
//
//    public void setSupportedMimeTypes(List<String> supportedMimeTypes) {
//        this.supportedMimeTypes = supportedMimeTypes;
//    }

    private static Logger logger = LoggerFactory.getLogger(MuledatasonnetConfiguration.class);

    public MuledatasonnetConfiguration() {
        //DataFormatService.getInstance().findAndRegisterPlugins();
/*
        DataFormatService dataFormatService = new DataFormatService();
        List<DataFormatPlugin> pluginsList = dataFormatService.findPlugins();
        for (DataFormatPlugin plugin : pluginsList) {
            supportedMimeTypes.addAll(Arrays.asList(plugin.getSupportedIdentifiers()));
        }
*/

        if (getLibrarySearchPaths() == null) {
            logger.debug("Explicit library path is not set, searching in the classpath...");
            try (ScanResult scanResult = new ClassGraph().whitelistPaths("/").scan()) {
                scanResult.getResourcesWithExtension("libsonnet")
                        .forEachByteArray(new ResourceList.ByteArrayConsumer() {
                            @Override
                            public void accept(Resource resource, byte[] bytes) {
                                logger.debug("Loading DataSonnet library: " + resource.getPath());
                                namedImports.put(
                                        resource.getPath(), new String(bytes, StandardCharsets.UTF_8));
                            }
                        });
            }
        } else {
            logger.debug("Explicit library path is " + getLibrarySearchPaths());

            for (String nextPath : getLibrarySearchPaths()) {
                final File nextLibDir = new File(nextPath);
                if (nextLibDir.isDirectory()) {
                    try {
                        Files.walkFileTree(nextLibDir.toPath(), new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                File f = file.toFile();
                                if (!f.isDirectory() && f.getName().toLowerCase().endsWith(".libsonnet")) {
                                    String content = IOUtils.toString(file.toUri());
                                    Path relative = nextLibDir.toPath().relativize(file);
                                    logger.debug("Loading DataSonnet library: " + relative);
                                    namedImports.put(relative.toString(), content);
                                }
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    } catch (IOException e) {
                        logger.error("Unable to load libraries from " + nextPath, e);
                    }
                }
            }
        }
    }

}
