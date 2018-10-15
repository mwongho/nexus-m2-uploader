package org.sonatype.nexus.repository.maven;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.swagger.nexus3.client.ApiException;
import io.swagger.nexus3.client.api.ComponentsApi;

@Service
public class ImporterService extends NexusService {
    private static final Logger logger = LoggerFactory.getLogger(ImporterService.class);
    
    public void importArtifacts() throws IOException, ApiException {
        logger.info("this.mavenDirectory :{}", this.mavenDirectory);
        Path searchPath = Paths.get(this.mavenDirectory.replaceFirst("~", System.getProperty("user.home")));
        logger.info("searchPath :{}", searchPath);
        if(searchPath != null && searchPath.toFile().exists()) {
            List<Path> pomPaths = getPomPaths(searchPath);
            logger.info("pomPaths :{}", pomPaths);
            for (Path pomPath : pomPaths) {
                Optional<Path> jarPath = getJarPath(searchPath, pomPath);
                Optional<Path> sourcesJarPath = getSourcesJarPath(searchPath, pomPath);
                try {
                    importArtifact(pomPath, jarPath, sourcesJarPath);
                } catch (ApiException e) {
                    logger.error("Error uploading artifcat", e);
                }
            }
        }
    }

    private List<Path> getPomPaths(final Path searchPath) throws IOException {
        PathMatcher pomMatcher = FileSystems.getDefault().getPathMatcher("glob:**.pom");
        PathMatcher snapshotMatcher = FileSystems.getDefault().getPathMatcher("glob:**SNAPSHOT.pom");

        List<Path> pomPaths = Files.walk(searchPath)
                .filter(a -> Files.isRegularFile(a) && pomMatcher.matches(a) && !snapshotMatcher.matches(a))
                .collect(Collectors.toList());

        return pomPaths;
    }

    private Optional<Path> getJarPath(final Path searchPath, final Path pomPath) throws IOException {
        String jarName = removeExtension(pomPath.getFileName().toString()) + ".jar";
        PathMatcher jarMatcher = FileSystems.getDefault().getPathMatcher("glob:**" + jarName);
        Optional<Path> jarPath = Files.walk(searchPath).filter(a -> a.toFile().isFile() && jarMatcher.matches(a)).findFirst();

        return jarPath;
    }
    
    private Optional<Path> getSourcesJarPath(final Path searchPath, final Path pomPath) throws IOException {
        String sourcesJarName = removeExtension(pomPath.getFileName().toString()) + "-sources.jar";
        PathMatcher sourcesJarMatcher = FileSystems.getDefault().getPathMatcher("glob:**" + sourcesJarName);
        Optional<Path> sourcesJarPath = Files.walk(searchPath).filter(a -> a.toFile().isFile() && sourcesJarMatcher.matches(a)).findFirst();

        return sourcesJarPath;
    }
    
    private static String removeExtension(final String fileName) {
        if (fileName.contains(".")) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            return fileName;
        }

    }

    private void importArtifact(final Path pomPath, final Optional<Path> jarPath, final Optional<Path> sourcesJarPath) throws ApiException {
        ComponentsApi componentsApi = getComponentsApi();
        uploadComponent(componentsApi, pomPath, jarPath, sourcesJarPath);
    }

    private void uploadComponent(final ComponentsApi componentsApi, final Path pomPath, final Optional<Path> jarPath, final Optional<Path> sourcesJarPath)
            throws ApiException {
        componentsApi.uploadComponent(this.repositoryId, null, null, null, null, null, null, false, null,
                pomPath.toFile(), null, "pom", jarPath.isPresent() ? jarPath.get().toFile() : null, null,
                jarPath.isPresent() ? "jar" : null, sourcesJarPath.isPresent() ? sourcesJarPath.get().toFile() : null, sourcesJarPath.isPresent() ? "sources" : null, sourcesJarPath.isPresent() ? "jar" : null, null, null, null, null, null, null, null, null);
    }
}
