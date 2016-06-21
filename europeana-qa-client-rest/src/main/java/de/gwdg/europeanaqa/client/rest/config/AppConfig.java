package de.gwdg.europeanaqa.client.rest.config;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import de.gwdg.europeanaqa.api.calculator.EdmCalculatorFacade;
import de.gwdg.europeanaqa.client.rest.DocumentTransformer;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.mongodb.morphia.Morphia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
@Configuration
@PropertySource("classpath:europeana-qa.properties")
public class AppConfig {

	@Value("${mongo.host}")
	String host;

	@Value("${mongo.port}")
	Integer port;

	@Value("${mongo.db}")
	String db;

	MongoDatabase mongoDb;

	@Bean
	DocumentCodec getDocumentCodec() {
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry());
		return new DocumentCodec(codecRegistry, new BsonTypeClassMap());
	}

	@Bean
	MongoDatabase getMongoDatabase() {
		if (mongoDb == null) {
			MongoClient mongoClient = new MongoClient(host, port);
			mongoDb = mongoClient.getDatabase(db);
		}
		return mongoDb;
	}

	/*
	@Bean
	MongoMappingDao getMongoMappingDao() {
		Morphia morphia = new Morphia();
		MongoClient client = new MongoClient(host, port);
		morphia.mapPackage("eu.europeana.metis.mapping", true);
		return new MongoMappingDao(morphia, client, db);
	}
	*/

	@Bean
	EdmCalculatorFacade getCalculatorFacade() {
		EdmCalculatorFacade calculator = new EdmCalculatorFacade();
		calculator.runFieldCardinality(true);
		calculator.runProblemCatalog(true);
		calculator.runLanguage(true);
		calculator.completenessCollectFields(true);
		calculator.configure();
		return calculator;
	}

	@Bean
	DocumentTransformer getDocumentTransformer() {
		return new DocumentTransformer(getMongoDatabase());
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	public String getDb() {
		return db;
	}
}
