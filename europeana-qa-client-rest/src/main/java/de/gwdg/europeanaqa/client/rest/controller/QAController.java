package de.gwdg.europeanaqa.client.rest.controller;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import de.gwdg.europeanaqa.api.calculator.EdmCalculatorFacade;
import de.gwdg.europeanaqa.client.rest.DocumentTransformer;
import de.gwdg.europeanaqa.client.rest.config.ApplicationConfiguration;
import de.gwdg.europeanaqa.client.rest.config.FileManager;
import de.gwdg.europeanaqa.client.rest.config.SessionDAO;
import de.gwdg.europeanaqa.client.rest.config.SessionManager;
import de.gwdg.europeanaqa.client.rest.model.Result;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
@Controller
@RestController
public class QAController {

	Logger logger = Logger.getLogger(QAController.class.getCanonicalName());

	private static final int BUFFER_SIZE = 4096;
	private final static String RECORDID_TPL = "/%s/%s";
	private File rDir; // = new File("/home/kiru/git/europeana-qa-r");
	private final static String R_SCRIPT = "get-histograms-and-stats.R";
	private final static List<String> jsonSuffixes = Arrays.asList("", ".collector", ".count", ".freq", ".hist");

	@Autowired
	ApplicationConfiguration config;

	@Autowired
	private EdmCalculatorFacade csvCalculator;

	@Autowired
	private EdmCalculatorFacade jsonCalculator;

	// @Autowired
	// private MongoMappingDao mongoMappingDao;
	@Autowired
	private MongoDatabase mongoDb;

	@Autowired
	private DocumentCodec codec;

	@Autowired
	DocumentTransformer transformer;

	@Autowired
	private SessionManager sessionManager;

	@Autowired
	private FileManager fileManager;

	public QAController() {
	}

	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public String hello() throws URISyntaxException, IOException {
		// return this.getClass().getClassLoader().getResource("europeana-qa.custom.properties").toString();
		return System.getProperty("java.class.path");
		// return config.getMongoDb() + ", " + config.getRunUniqueness();
	}

	@RequestMapping(value = "/record/{part1}/{part2}", method = RequestMethod.GET,
			  produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String getRecord(
			  @PathVariable("part1") String part1,
			  @PathVariable("part2") String part2,
			  @RequestParam(value = "sessionId", required = false) String sessionId
	)
			  throws URISyntaxException, IOException {
		String recordId = String.format(RECORDID_TPL, part1, part2);
		return getRecordAsJson(recordId);
	}

	@RequestMapping(value = "/batch/{part1}/{part2}", method = RequestMethod.GET,
			  produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody Result getBatchCsv(
			  @PathVariable("part1") String part1,
			  @PathVariable("part2") String part2,
			  @RequestParam(value = "sessionId", required = false) String sessionId
	)
			  throws URISyntaxException, IOException {
		Result result;
		if (sessionId == null) {
			result = buildResult(sessionId, "failure", "Missing sessionId.");
		} else {
			if (!sessionManager.validate(sessionId)) {
				result = buildResult(sessionId, "failure", "Invalid sessionId.");
			} else {
				String recordId = String.format(RECORDID_TPL, part1, part2);
				String json = getRecordAsJson(recordId);
				String csv = csvCalculator.measure(json);
				if (sessionManager.getState(sessionId).equals(SessionDAO.State.MEASURING)) {
					fileManager.writeOrAppend(sessionId + ".csv", csv);
					result = buildResult(sessionId, "success");
				} else {
					// throw new IllegalArgumentException("The workflow is not in the 'measuring' state!");
					result = buildResult(sessionId, "failure", "The workflow is not in the 'measuring' state!");
				}
			}
		}
		return result;
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
		String csv = csvCalculator.measure(json);
		return csv;
	}

	@RequestMapping(value = "/{part1}/{part2}.json", method = RequestMethod.GET,
			  produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody
	Result getJson(
			  @PathVariable("part1") String part1,
			  @PathVariable("part2") String part2,
			  @RequestParam(value = "sessionId", required = false) String sessionId
	) throws URISyntaxException, IOException {
		String recordId = String.format(RECORDID_TPL, part1, part2);
		String json = getRecordAsJson(recordId);
		if (config.getRunUniqueness()) {
			jsonCalculator.collectTfIdfTerms(true);
		}
		jsonCalculator.measure(json);

		Result result = new Result();
		result.setLabelledResults(jsonCalculator.getLabelledResults());
		result.setExistingFields(jsonCalculator.getExistingFields());
		result.setMissingFields(jsonCalculator.getMissingFields());
		result.setEmptyFields(jsonCalculator.getEmptyFields());
		if (config.getRunUniqueness()) {
			result.setTermsCollection(jsonCalculator.getTermsCollection());
		}

		return result;
	}

	@RequestMapping(value = "/batch/measuring/start", method = RequestMethod.GET)
	public @ResponseBody Result startMeasuring() throws URISyntaxException, IOException {
		String sessionId = sessionManager.create();
		return buildResult(sessionId, "success");
	}

	private Result buildResult(String sessionId, String status) {
		Result result = new Result();
		result.setSessionId(sessionId);
		result.setStatus(sessionManager.getState(sessionId).name());
		result.setResult(status);
		return result;
	}

	private Result buildResult(String sessionId, String status, String message) {
		Result result = buildResult(sessionId, status);
		result.setMessage(message);
		return result;
	}

	@RequestMapping(value = "/batch/measuring/{sessionId}/stop", method = RequestMethod.GET)
	public @ResponseBody Result stopMeasuring(
			  @PathVariable("sessionId") String sessionId
	) throws URISyntaxException, IOException {
		Result result;
		if (sessionId == null || !sessionManager.validate(sessionId)) {
			result = buildResult(sessionId, "failure", "Missing or invalid session id.");
		} else {
			sessionManager.setState(sessionId, SessionDAO.State.IDLE);
			result = buildResult(sessionId, "success");
		}
		return result;
	}

	@RequestMapping(value = "/batch/analyzing/{sessionId}/start", method = RequestMethod.GET)
	public @ResponseBody Result startAnalzying(
			  @PathVariable("sessionId") String sessionId
	) throws URISyntaxException, IOException {
		Result result;
		if (sessionId == null || !sessionManager.validate(sessionId)) {
			result = buildResult(sessionId, "failure", "Missing or invalid session id.");
		} else {
			if (!sessionManager.getState(sessionId).equals(SessionDAO.State.IDLE)) {
				result = buildResult(sessionId, "failure", "The current session state doesn't allow to run this operation.");
			} else {
				if (rDir == null)
					rDir = new File(config.getrDirectory());

				logger.info("rDir: " + rDir.getAbsolutePath());
				String inputFile = fileManager.getPath(sessionId);
				String command = String.format("Rscript %s %s true true true", R_SCRIPT, inputFile);
			// String command = String.format("cd \"%s\"", rDir);
				logger.info("Launching command: " + command);
				Process process = Runtime.getRuntime().exec(command, null, rDir);
				sessionManager.setState(sessionId, SessionDAO.State.ANALYZING);
				sessionManager.setAnalyzingProcess(sessionId, process);
				result = buildResult(sessionId, "success");
			}
		}
		return result;
	}

	@RequestMapping(value = "/batch/analyzing/{sessionId}/status", method = RequestMethod.GET)
	public @ResponseBody Result getAnlyzingStatus(
			  @PathVariable("sessionId") String sessionId
	) throws URISyntaxException, IOException {
		Result result;
		if (sessionId == null || !sessionManager.validate(sessionId)) {
			result = buildResult(sessionId, "failure", "Missing or invalid session id.");
		} else {
			if (!sessionManager.getState(sessionId).equals(SessionDAO.State.ANALYZING)) {
				result = buildResult(sessionId, "failure", "The current session state doesn't allow to run this operation.");
			} else {
				Process process = sessionManager.getAnalyzingProcess(sessionId);
				if (process.isAlive()) {
					result = buildResult(sessionId, "in progress");
				} else {
					result = buildResult(sessionId, "ready");
				}
			}
		}
		return result;
	}

	@RequestMapping(value = "/batch/analyzing/{sessionId}/retrieve", method = RequestMethod.GET)
	public void retrieveAnlyzingResult(
			  @PathVariable("sessionId") String sessionId,
			  HttpServletResponse response
	) throws URISyntaxException, IOException {
		if (sessionManager.getState(sessionId).equals(SessionDAO.State.ANALYZING)) {
			Process process = sessionManager.getAnalyzingProcess(sessionId);
			if (!process.isAlive()) {
				logger.info("process returned with exit value: " + process.exitValue());
				if (process.exitValue() == 0) {
					response.setContentType("application/zip");
					response.setHeader("Content-Disposition", "attachment; filename=analysis.zip");
					logger.info("go on");
					ZipOutputStream zipStream = new ZipOutputStream(response.getOutputStream());
					zipImageFiles(sessionId, zipStream);
					zipJsonFiles(sessionId, zipStream);
					zipStream.close();
				} else {
					logger.severe("errors: " + read(process.getErrorStream()));
					logger.severe("output: " + process.getOutputStream().toString());
				}
			}
		}
	}

	private void zipImageFiles(String sessionId, ZipOutputStream zipStream) throws IOException {
		File imageDir = new File(rDir, "img" + File.separator + sessionId);
		if (imageDir.exists()) {
			File[] files = imageDir.listFiles();
			for (File file : files) {
				ZipEntry zipEntry = new ZipEntry(file.getName().replace(sessionId + "-", ""));
				copyContent(file, zipStream, zipEntry);
			}
		} else {
			logger.info("image dir doesn't exist");
		}
	}

	private void zipJsonFiles(String sessionId, ZipOutputStream zipStream) throws IOException {
		File jsonDir = new File(rDir, "json");
		if (jsonDir.exists()) {
			for (String suffix : jsonSuffixes) {
				File file = new File(jsonDir, sessionId + suffix + ".json");
				if (file.exists()) {
					ZipEntry zipEntry = new ZipEntry(file.getName());
					copyContent(file, zipStream, zipEntry);
				} else {
					logger.info("json file doesn't exist " + file.getAbsolutePath());
					
				}
			}
		} else {
			logger.info("json dir doesn't exist");
		}
	}

	private void copyContent(File file, ZipOutputStream zipStream, ZipEntry zipEntry)
			throws IOException, FileNotFoundException {
		int bytesIn;
		byte[] readBuffer = new byte[BUFFER_SIZE];
		try (FileInputStream fis = new FileInputStream(file)) {
			zipStream.putNextEntry(zipEntry);
			while ((bytesIn = fis.read(readBuffer)) != -1) {
				zipStream.write(readBuffer, 0, bytesIn);
			}
		}
	}

	private String getRecordAsJson(String recordId) {
		Bson condition = Filters.eq("about", recordId);
		Document record = mongoDb.getCollection("record").find(condition).first();
		transformer.transform(record);
		String json = record.toJson(codec);
		// logger.info("record: " + json);
		return json;
	}

	private static String read(InputStream input) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		}
	}

}
