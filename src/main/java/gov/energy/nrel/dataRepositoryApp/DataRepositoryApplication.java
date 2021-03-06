package gov.energy.nrel.dataRepositoryApp;

import com.mongodb.MongoTimeoutException;
import gov.energy.nrel.dataRepositoryApp.bo.IBusinessObjectsInventory;
import gov.energy.nrel.dataRepositoryApp.bo.mongodb.singleCellCollectionApproach.sc_BusinessObjectsInventory;
import gov.energy.nrel.dataRepositoryApp.context.TomcatConnectorCustomizer_threadShutdown;
import gov.energy.nrel.dataRepositoryApp.servletFilter.HeadersSecurityFilter;
import gov.energy.nrel.dataRepositoryApp.servletFilter.MakeSureAllParametersAreSanitaryFilter;
import gov.energy.nrel.dataRepositoryApp.settings.ISettings;
import gov.energy.nrel.dataRepositoryApp.utilities.PerformanceLogger;
import gov.energy.nrel.dataRepositoryApp.utilities.valueSanitizer.IValueSanitizer;
import gov.energy.nrel.dataRepositoryApp.utilities.valueSanitizer.ValueSanitizer_usingOwaspJavaHtmlSanitizer;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;

/**
 * The Data Repository Application was originally designed and coded by Mike Brown (mike.public@superbrown.com)
 * October through December 2015 while on contract at NREL.  Feel free to reach out to him for any assistance.  (Include
 * "Data Repository Application" in the subject line.)
 */

// "@Component" indicates to Spring that the class is a "component".  Such classes are considered as candidates for
// auto-detection when using annotation-based configuration and classpath scanning.
//
// Reference: http://docs.spring.io/spring-framework/docs/2.5.x/api/org/springframework/stereotype/Component.html
//
// "@EnableAutoConfiguration" enable auto-configuration of the Spring Application Context, attempting to guess and
// configure beans that you are likely to need. Auto-configuration classes are usually applied based on your classpath
// and what beans you have defined. For example, If you have tomcat-embedded.jar on your classpath you are likely to
// want a TomcatEmbeddedServletContainerFactory (unless you have defined your own EmbeddedServletContainerFactory bean).
//
// Auto-configuration tries to be as intelligent as possible and will back-away as you define more of your own
// configuration. You can always manually exclude() any configuration that you never want to apply (use excludeName() if
// you don't have access to them). You can also exclude them via the spring.autoconfigure.exclude property. Auto-
// configuration is always applied after user-defined beans have been registered.
//
// The package of the class that is annotated with @EnableAutoConfiguration has specific significance and is often used
// as a 'default'. For example, it will be used when scanning for @Entity classes. It is generally recommended that you
// place @EnableAutoConfiguration in a root package so that all sub-packages and classes can be searched.
//
// Auto-configuration classes are regular Spring Configuration beans.  They are located using the SpringFactoriesLoader
// mechanism (keyed against this class). Generally auto-configuration beans are @Conditional beans (most often using
// @ConditionalOnClass and @ConditionalOnMissingBean annotations).
//
// Reference: http://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/autoconfigure/EnableAutoConfiguration.html

@SpringBootApplication
@ComponentScan
@EnableSwagger2
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration.class
})
public class DataRepositoryApplication extends SpringApplication {

    public volatile static Boolean cleanupOperationIsOccurring = false;

    @Autowired
    private ISettings settings;

    private IValueSanitizer valueSanitizer;

    protected IBusinessObjectsInventory businessObjects;

    protected static Logger log = Logger.getLogger(DataRepositoryApplication.class);

    // Spring will call this constructor at app startup by virtue of this being annotated as a Component.
    public DataRepositoryApplication() {
    }

    // Spring will call this after it calls the constructor.
    @PostConstruct
    protected void init() {

        // DESIGN NOTE: The use of AntiSamy was dropped because it is no longer maintained. I don't know how important
        // this is.

//        valueSanitizer = new ValueSanitizer_usingAntiSamy(settings.getAntiSamyPolicyFileName());
        valueSanitizer = new ValueSanitizer_usingOwaspJavaHtmlSanitizer();

        try {
            IBusinessObjectsInventory businessObjects = createBusinessObjects();
            setBusinessObjects(businessObjects);

            String[] defaultSetOfDataCategories = settings.getDefaultSetOfDataCategories();
            businessObjects.getDataCategoryBO().assureCategoriesAreInTheDatabase(defaultSetOfDataCategories);

            businessObjects.getDatasetBO().attemptToCleanupDataFromAllPreviouslyIncompleteDatasetUploads();
        }
        catch (MongoTimeoutException e) {
            // This is written like this so it stands out in the log.
            throw new RuntimeException("\n" +
                    "\n" +
                    "T H E    M O N G O   D A T A B A S E   I S   N O T   R E S P O N D I N G .\n" +
                    "\n" +
                    "M A K E   S U R E   I T   I S   R U N N I N G .\n",
                    e);
        }
    }


    public IBusinessObjectsInventory createBusinessObjects() {

        // This line is very significant. It determines what business objects the app will use.
        //
        // During development I experimented with a couple of different approaches to the DAO layer, and changing
        // what class was instantiated here determined which one would be used.

        IBusinessObjectsInventory businessObjects = new sc_BusinessObjectsInventory(this);
        return businessObjects;
    }

    public ISettings getSettings() {

        return settings;
    }

    public void setBusinessObjects(IBusinessObjectsInventory businessObjects) {

        this.businessObjects = businessObjects;
    }

    public IBusinessObjectsInventory getBusinessObjects() {

        return businessObjects;
    }

    public IValueSanitizer getValueSanitizer() {

        return valueSanitizer;
    }

    @Bean
    public EmbeddedServletContainerCustomizer createTomcatCustomizer() {

        EmbeddedServletContainerCustomizer tomcatCustomizer =

                new EmbeddedServletContainerCustomizer() {

                    @Override
                    public void customize(ConfigurableEmbeddedServletContainer container) {

                        log.debug("===== customize(): " + container);

                        if (container instanceof TomcatEmbeddedServletContainerFactory) {

                            TomcatEmbeddedServletContainerFactory tomcatEmbeddedServletContainerFactory =
                                    (TomcatEmbeddedServletContainerFactory) container;

                            TomcatConnectorCustomizer_threadShutdown threadShutdownCustomizer =
                                    new TomcatConnectorCustomizer_threadShutdown();

                            tomcatEmbeddedServletContainerFactory.addConnectorCustomizers(threadShutdownCustomizer);
                        }
                    }
                };

        return tomcatCustomizer;
    }

    // DESIGN NOTE:
    //
    // At first I had the app sanitize incoming requests.  But I decided it made more sense to
    // reject unsanitary requests to avoid adding data to the database from a malicious source
    // (because it would likely be junk data). Also, it avoids the data being "silently" modified by
    // the app without the user’s awareness. We want to avoid scenarios where the data may actually
    // be the way the user intends, but the app nevertheless changes it because it interprets it to
    // be potentially malicious. If the app thinks it needs to change legitimate values, the user
    // should have visibility of that so the design of the application’s XSS detection functionality
    // can be revisited.
    //    @Bean
    //    public Filter createParameterSanitizingFilter() {
    //
    //        // This filter sanitizes the data coming in to the REST endpoints.
    //
    //        // DESIGN NOTE: The use of AntiSamy was dropped because it is no longer maintained. I didn't
    //        // do a lot of research into this, so I don't know how important this is.
    //
    ////        String antiSamyPolicyFileName = this.getAntiSamyPolicyFileName();
    ////        IValueSanitizer valueSanitizer = new ValueSanitizer_usingAntiSamy(antiSamyPolicyFileName);
    //
    //        IValueSanitizer valueSanitizer = new ValueSanitizer_usingOwaspJavaHtmlSanitizer();
    //
    //        return new ParameterSanitizingFilter(valueSanitizer);
    //    }

    // See design note above.
    @Bean
    public Filter createMakeSureAllParametersAreSanitaryFilter() {

        // DESIGN NOTE: The use of AntiSamy was dropped because it is no longer maintained. I didn't
        // do a lot of research into this, so I don't know how important this is.

        //        String antiSamyPolicyFileName = this.getAntiSamyPolicyFileName();
        //        IValueSanitizer valueSanitizer = new ValueSanitizer_usingAntiSamy(antiSamyPolicyFileName);

        IValueSanitizer valueSanitizer = new ValueSanitizer_usingOwaspJavaHtmlSanitizer();

        return new MakeSureAllParametersAreSanitaryFilter(valueSanitizer);
    }

    // DESIGN NOTE: This cleans up the headers to eliminate security vulnerabilities.
    @Bean
    public Filter createHeadersSecurityFilter() {

        return new HeadersSecurityFilter();
    }

    // This constructor is used by unit tests.
    public DataRepositoryApplication(ISettings settings) {

        this.settings = settings;

        if (settings.getPerformanceLoggingEnabled() == true) {
            PerformanceLogger.enable();
        }
        else {
            PerformanceLogger.disable();
        }

        valueSanitizer = new ValueSanitizer_usingOwaspJavaHtmlSanitizer();
    }
}
