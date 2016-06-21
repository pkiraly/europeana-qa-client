package de.gwdg.europeanaqa.client.rest.config;

import com.mongodb.MongoClient;
import eu.europeana.metis.mapping.Mapping;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class MongoMappingDao { //extends BasicDAO<Mapping, String> {

	public MongoMappingDao(Morphia morphia, MongoClient mongo, String database) {
		// super(mongo, morphia, database);
	}
}
