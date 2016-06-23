package de.gwdg.europeanaqa.client.rest.controller;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import de.gwdg.europeanaqa.api.calculator.EdmCalculatorFacade;
import de.gwdg.europeanaqa.client.rest.DocumentTransformer;
import de.gwdg.europeanaqa.client.rest.config.ApplicationConfiguration;
import de.gwdg.europeanaqa.client.rest.model.Result;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
@Controller
@RestController
@RequestMapping(value = "/europeana-qa")
public class QAController {

	private final static String RECORDID_TPL = "/%s/%s";
	Logger logger = Logger.getLogger(QAController.class.getCanonicalName());

	@Autowired
	ApplicationConfiguration config;

	@Autowired
	private EdmCalculatorFacade calculatorFacade;

	// @Autowired
	// private MongoMappingDao mongoMappingDao;

	@Autowired
	private MongoDatabase mongoDb;

	@Autowired
	private DocumentCodec codec;

	@Autowired
	DocumentTransformer transformer;

	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public String hello() throws URISyntaxException, IOException {
		// return this.getClass().getClassLoader().getResource("europeana-qa.custom.properties").toString();
		// return System.getProperty("java.class.path");
		return config.getMongoDb() + ", " + config.getRunUniqueness();
	}

	@RequestMapping(value = "/{part1}/{part2}.csv", method = RequestMethod.GET,
		produces = "text/csv")
	public String getCsv(
			  @PathVariable("part1") String part1,
			  @PathVariable("part2") String part2
	)
			  throws URISyntaxException, IOException {
		String recordId = String.format(RECORDID_TPL, part1, part2);
		String json = getRecordAsJson(recordId);
		return calculatorFacade.measure(json);
	}

	@RequestMapping(value = "/{part1}/{part2}.json", method = RequestMethod.GET,
		produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Result getJson(
			  @PathVariable("part1") String part1,
			  @PathVariable("part2") String part2
	) throws URISyntaxException, IOException {
		String recordId = String.format(RECORDID_TPL, part1, part2);
		String json = getRecordAsJson(recordId);
		if (config.getRunUniqueness())
			calculatorFacade.collectTfIdfTerms(true);
		calculatorFacade.measure(json);

		Result result = new Result();
		result.setLabelledResults(calculatorFacade.getLabelledResults());
		result.setExistingFields(calculatorFacade.getExistingFields());
		result.setMissingFields(calculatorFacade.getMissingFields());
		result.setEmptyFields(calculatorFacade.getEmptyFields());
		if (config.getRunUniqueness())
			result.setTermsCollection(calculatorFacade.getTermsCollection());

		return result;
	}

	private String getRecordAsJson(String recordId) {
		Bson condition = Filters.eq("about", recordId);
		Document record = mongoDb.getCollection("record").find(condition).first();
		logger.info("record: " + record.toJson(codec));
		transformer.transform(record);
		String json = record.toJson(codec);
		return json;
	}

}
