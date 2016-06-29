package de.gwdg.europeanaqa.client.rest.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class FileManager {

	private Charset charset = StandardCharsets.UTF_8;
	private String directory;

	public FileManager(String directory) {
		this.directory = directory;
	}

	public void writeOrAppend(String fileName, String content) throws IOException {
		Path path = Paths.get(directory + "/" + fileName);
		StandardOpenOption option = Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE;
		if (!content.endsWith("\n"))
			content += "\n";
		Files.write(path, content.getBytes(charset), option);
	}

	public String read(String fileName) throws IOException {
		Path path = Paths.get(directory, fileName);
		String content = new String(Files.readAllBytes(path), charset);
		return content;
	}

	public String getFileName(String sessionId) {
		return sessionId + ".csv";
	}

	public String getPath(String sessionId) {
		return Paths.get(directory, getFileName(sessionId)).toString();
	}
}
