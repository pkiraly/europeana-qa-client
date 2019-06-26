package de.gwdg.europeanaqa.client.rest.config;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import de.gwdg.europeanaqa.api.calculator.EdmCalculatorFacade;
import de.gwdg.europeanaqa.client.rest.DocumentTransformer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
@Configuration
@EnableWebMvc
@PropertySource({
  "classpath:europeana-qa.custom.properties",
  "classpath:europeana-qa.properties",
})
@ComponentScan(basePackages = "de.gwdg.europeanaqa.client.rest")
public class ApplicationConfiguration {

  static final Logger logger = Logger.getLogger(ApplicationConfiguration.class.getCanonicalName());

  @Value("${mongo.host:localhost}")
  String mongoHost;

  @Value("${mongo.port:27017}")
  Integer mongoPort;

  @Value("${mongo.db:europeana}")
  String mongoDatabase;

  @Value("${solr.host:localhost}")
  String solrHost;

  @Value("${solr.port:8983}")
  Integer solrPort;

  @Value("${solr.path:solr/europeana}")
  String solrPath;

  @Value("${run.uniqueness:false}")
  Boolean runUniqueness;

  @Value("${output.directory}")
  String outputDirectory;

  @Value("${r.directory:~/git/europeana-qa-r}")
  String rDirectory;

  MongoDatabase mongoDb;

  @Bean
  DocumentCodec getDocumentCodec() {
    CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry());
    return new DocumentCodec(codecRegistry, new BsonTypeClassMap());
  }

  @Bean
  MongoDatabase getMongoDatabase() {
    if (mongoDb == null) {
      MongoClient mongoClient = new MongoClient(mongoHost, mongoPort);
      mongoDb = mongoClient.getDatabase(mongoDatabase); //
    }
    return mongoDb;
  }

  @Bean
  SessionManager getSessionManager() {
    SessionManager sessionManager = new SessionManager();
    return sessionManager;
  }

  @Bean
  FileManager getFileManager() {
    logger.log(Level.INFO, "outputDirectory: {0}", outputDirectory);
    FileManager fileManager = new FileManager(outputDirectory);
    return fileManager;
  }

  /*
  @Bean
  MongoMappingDao getMongoMappingDao() {
    Morphia morphia = new Morphia();
    MongoClient client = new MongoClient(mongoHost, mongoPort);
    morphia.mapPackage("eu.europeana.metis.mapping", true);
    return new MongoMappingDao(morphia, client, mongoDatabase);
  }
   */
  @Bean(name = "csvCalculator")
  EdmCalculatorFacade getCalculatorFacadeForCsv() {
    EdmCalculatorFacade calculator = new EdmCalculatorFacade();
    calculator.enableFieldCardinalityMeasurement(false);
    calculator.enableProblemCatalogMeasurement(true);
    calculator.enableLanguageMeasurement(false);
    calculator.completenessCollectFields(false);
    if (runUniqueness)
      calculator.enableTfIdfMeasurement(true);
    calculator.configure();
    if (runUniqueness)
      calculator.configureSolr(solrHost, solrHost, solrPath);
    return calculator;
  }

  @Bean(name = "jsonCalculator")
  EdmCalculatorFacade getCalculatorFacadeForJson() {
    EdmCalculatorFacade calculator = new EdmCalculatorFacade();
    calculator.enableFieldCardinalityMeasurement(true);
    calculator.enableProblemCatalogMeasurement(true);
    calculator.enableLanguageMeasurement(true);
    calculator.enableProxyBasedCompleteness(true);
    calculator.enableMultilingualSaturationMeasurement(true);
    calculator.setExtendedFieldExtraction(true);
    calculator.setExtendedFieldExtraction(true);
    calculator.completenessCollectFields(true);
    if (runUniqueness)
      calculator.enableTfIdfMeasurement(true);
    calculator.configure();
    if (runUniqueness)
      calculator.configureSolr(solrHost, solrHost, solrPath);
    return calculator;
  }

  @Bean(name = "PropertiesFile")
  public static PropertyPlaceholderConfigurer properties() {
    PropertyPlaceholderConfigurer placeholder = new PropertyPlaceholderConfigurer();
    ClassPathResource[] value = new ClassPathResource[]{
      new ClassPathResource("europeana-qa.properties"),
      new ClassPathResource("europeana-qa.custom.properties")
    };
    placeholder.setLocations(value);
    return placeholder;
  }

  @Bean
  DocumentTransformer getDocumentTransformer() {
    return new DocumentTransformer(getMongoDatabase());
  }

  public String getMongoHost() {
    return mongoHost;
  }

  public Integer getMongoPort() {
    return mongoPort;
  }

  public String getMongoDb() {
    return mongoDatabase;
  }

  public Boolean getRunUniqueness() {
    return runUniqueness;
  }

  public String getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public String getrDirectory() {
    return rDirectory;
  }

  public void setrDirectory(String rDirectory) {
    this.rDirectory = rDirectory;
  }
}
