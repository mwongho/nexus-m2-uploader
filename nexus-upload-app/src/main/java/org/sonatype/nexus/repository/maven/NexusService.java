package org.sonatype.nexus.repository.maven;

import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import io.swagger.nexus3.client.ApiClient;
import io.swagger.nexus3.client.Configuration;
import io.swagger.nexus3.client.api.ComponentsApi;

public class NexusService {
    private static final Logger logger = LoggerFactory.getLogger(NexusService.class);

    @Value("${maven.directory}")
    protected String mavenDirectory;
    @Value("${nexus.repository:maven-public}")
    protected String repositoryId;
    @Value("${nexus.url}")
    protected String repositoryUrl;
    @Value("${nexus.username}")
    protected String username;
    @Value("${nexus.password}")
    protected String password;
    @Value("${debug:false}")
    protected boolean debug;

    public String getMavenDirectory() {
        if(this.mavenDirectory.startsWith("~")) {
            return mavenDirectory.replaceFirst("~", System.getProperty("user.home"));
        } else {
            return mavenDirectory;
        }
    }

    public void setMavenDirectory(String mavenDirectory) {
        this.mavenDirectory = mavenDirectory;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    protected String getEncoding() {
        if (this.username != null || this.password != null) {
            String auth = this.username + ":" + this.password;
            logger.debug("auth :{}", auth);
            return Base64.getEncoder().encodeToString(auth.getBytes());
        } else {
            return "";
        }
    }
    
    protected ApiClient getApiClient() {
        ApiClient apiClient = null;
        if(this.repositoryUrl != null) {
            apiClient = Configuration.getDefaultApiClient();
            apiClient.setBasePath(this.repositoryUrl);
            apiClient.setDebugging(this.debug);
            apiClient.addDefaultHeader("Authorization", "Basic " + getEncoding());
        }
        return apiClient;
    }
    
    protected ComponentsApi getComponentsApi() {
        ApiClient apiClient = getApiClient();
        if(apiClient != null) {
            return new ComponentsApi(apiClient);
        }
        return null;
    }
}
