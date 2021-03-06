package gov.energy.nrel.dataRepositoryApp.settings;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;

// DESIGN AND USAGE NOTE:
//
// The idea behind the settings is to have the app look for them in two files, one that contains
// defaults, (data-repository-app__defaults.properties), and one that overrides them,
// (data-repository-app__envSpecificOverrides.properties).  The idea is that the defaults will come
// bundled in the war, while the overrides will be placed somewhere on the server's classpath (for
// example, Tomcat's lib directory).

@Configuration
@PropertySource(value = {
        "classpath:data-repository-app__defaults.properties",
        "classpath:data-repository-app__envSpecificOverrides.properties"
})

@Component
@AutoConfigureBefore
public class Settings implements ISettings {

    // DESIGN NOTE: These are all values contained in the application's properties file.

    @Value("${mongoDb.host}")
    protected String mongoDbHost;

    @Value("${mongoDb.port}")
    protected String mongoDbPort;

    @Value("${mongoDb.databaseName}")
    protected String mongoDatabaseName;

    @Value("${app.rootDirectoryForUploadedDataFiles}")
    protected String rootDirectoryForUploadedDataFiles;

    @Value("${app.defaultSetOfDataCategories}")
    private String[] defaultSetOfDataCategories;

    @Value("${app.performanceLoggingEnabled}")
    private boolean performanceLoggingEnabled;

    @Value("${app.antiSamyPolicyFileName}")
    private String antiSamyPolicyFileName;


    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    public Settings() {

        init();
    }

    protected void init() {
    }

    @Override
    public String getMongoDatabaseName() {
        return mongoDatabaseName;
    }

    @Override
    public String getMongoDbHost() {
        return mongoDbHost;
    }

    @Override
    public String getMongoDbPort() {
        return mongoDbPort;
    }

    @Override
    public void setMongoDatabaseName(String mongoDatabaseName) {
        this.mongoDatabaseName = mongoDatabaseName;
    }

    @Override
    public void setSetMongoDbHost(String mongoDbHost) {
        this.mongoDbHost = mongoDbHost;
    }

    @Override
    public void setMongoDbHost(String mongoDbHost) {
        this.mongoDbHost = mongoDbHost;
    }

    @Override
    public void setMongoDbPort(String mongoDbPort) {
        this.mongoDbPort = mongoDbPort;
    }

    @Override
    public String getRootDirectoryForUploadedDataFiles() {
        return rootDirectoryForUploadedDataFiles;
    }

    @Override
    public void setRootDirectoryForUploadedDataFiles(String rootDirectoryForUploadedDataFiles) {
        this.rootDirectoryForUploadedDataFiles = rootDirectoryForUploadedDataFiles;
    }

    @Override
    public String[] getDefaultSetOfDataCategories() {
        return defaultSetOfDataCategories;
    }

    @Override
    public void setDefaultSetOfDataCategories(String[] defaultSetOfDataCategories) {
        this.defaultSetOfDataCategories = defaultSetOfDataCategories;
    }

    @Override
    public boolean getPerformanceLoggingEnabled() {
        return this.performanceLoggingEnabled;
    }

    @Override
    public void setPerformanceLoggingEnabled(Boolean performanceLoggingEnabled) {
        this.performanceLoggingEnabled = performanceLoggingEnabled;
    }

    @Override
    public String getAntiSamyPolicyFileName() {
        return this.antiSamyPolicyFileName;
    }

    @Override
    public void setAntiSamyPolicyFileName(String antiSamyPolicyFileName) {
        this.antiSamyPolicyFileName = antiSamyPolicyFileName;
    }

}
