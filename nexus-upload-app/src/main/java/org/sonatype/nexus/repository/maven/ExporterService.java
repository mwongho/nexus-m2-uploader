package org.sonatype.nexus.repository.maven;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.swagger.nexus3.client.ApiException;
import io.swagger.nexus3.client.api.ComponentsApi;
import io.swagger.nexus3.client.model.AssetXO;
import io.swagger.nexus3.client.model.ComponentXO;
import io.swagger.nexus3.client.model.PageComponentXO;

@Service
public class ExporterService extends NexusService {
    private static final Logger logger = LoggerFactory.getLogger(ExporterService.class);
    
    private static final Pattern MVN_FILTER = Pattern.compile(".*(\\.(pom|jar))$");
    
    public void exportArtifacts() throws ApiException, IOException {

        String continuationToken = processPageComponent(null);
        while (continuationToken != null) {
            String nextContinuationToken = processPageComponent(continuationToken);
            continuationToken = nextContinuationToken;
        }
        
    }
    
    private String processPageComponent(final String continuationToken) throws ApiException, IOException {
        String nextContinuationToken = null;
        
        ComponentsApi componentsApi = getComponentsApi();
        PageComponentXO pageComponent = componentsApi.getComponents(this.repositoryId, continuationToken);
        nextContinuationToken = pageComponent.getContinuationToken();
        List<ComponentXO> components = pageComponent.getItems();
        for (ComponentXO component : components) {
            saveComponent(component);
        }
        return nextContinuationToken;
    }
    
    private void saveComponent(ComponentXO component) throws IOException {
        logger.debug("component :{}", component.getName());
        List<AssetXO> mavenAssets = component.getAssets().stream().filter(a -> MVN_FILTER.matcher(a.getPath()).matches()).collect(Collectors.toList());
        for (AssetXO asset : mavenAssets) {
            writeAssetToFile(asset);
        }
    }
    
    private void writeAssetToFile(AssetXO asset) throws IOException {
            Path path = Paths.get(getMavenDirectory(), asset.getPath());
            String filePath = path.toFile().getPath();
            logger.debug("filePath :{}",filePath);
            
            FileUtils.copyURLToFile(new URL(asset.getDownloadUrl()), new File(filePath));
    }
    

}
