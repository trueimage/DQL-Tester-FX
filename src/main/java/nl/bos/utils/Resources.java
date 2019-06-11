package nl.bos.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static nl.bos.Constants.*;

public class Resources {
	private static final Logger LOGGER = Logger.getLogger(Resources.class.getName());

	private static Properties settings = null;

	private static File exportPath = null;

	private FXMLLoader fxmlLoader;

	private static File checkoutFile;

	public static File createFileFromFileChooser(String title) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		return fileChooser.showOpenDialog(null);
	}

	public static File selectFileFromFileChooser(String title, File path) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(path);
		fileChooser.setTitle(title);
		return fileChooser.showOpenDialog(null);
	}

	public static List<String> readLines(File file) {
		try {
			return Files.readAllLines(file.toPath());
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return new ArrayList<>();
	}

	public static void initHistoryFile() {
		File historyFile = new File(HISTORY_JSON);
		try {
			if (historyFile.createNewFile()) {
				JSONArray list = new JSONArray();
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(QUERIES, list);

				writeJsonToFile(jsonObject, historyFile);
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private static void initCheckoutFile() {
		checkoutFile = new File(CHECKOUT_JSON);
		try {
			if (checkoutFile.createNewFile()) {
				LOGGER.log(Level.INFO, "New Checkout file");
				JSONObject jsonObject = new JSONObject();
				writeJsonToFile(jsonObject, checkoutFile);
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public static byte[] readHistoryJsonBytes() {
		try {
			return Files.readAllBytes(Paths.get(HISTORY_JSON));
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return new byte[0];
	}

	public static void writeJsonDataToJsonHistoryFile(JSONObject jsonObject) {
		writeJsonToFile(jsonObject, new File(HISTORY_JSON));
	}

	private static void writeJsonToFile(JSONObject jsonObject, File jsonFile) {
		try (FileWriter file = new FileWriter(jsonFile)) {
			file.write(jsonObject.toString());
			file.flush();
		} catch (IOException ioe) {
			LOGGER.log(Level.SEVERE, ioe.getMessage(), ioe);
		}
	}

	private static JSONObject readCheckoutFile() {
		try {
			if (checkoutFile == null || !checkoutFile.exists()) {
				initCheckoutFile();
			}
			return new JSONObject(new String(Files.readAllBytes(Paths.get(CHECKOUT_JSON)), StandardCharsets.UTF_8));
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
	}

	public static void putContentPathToCheckoutFile(String objectId, String path) {
		JSONObject currentJson = readCheckoutFile();
		Objects.requireNonNull(currentJson).put(objectId, path);
		writeJsonToFile(currentJson, checkoutFile);
	}
	
	public static void removeContentPathFromCheckoutFile(String objectId) {
		JSONObject currentJson = readCheckoutFile();
		Objects.requireNonNull(currentJson).remove(objectId);
		writeJsonToFile(currentJson, checkoutFile);
	}
	
	public static String getContentPathFromCheckoutFile(String objectId) {
		JSONObject currentJson = readCheckoutFile();
		return Objects.requireNonNull(currentJson).getString(objectId);
	}

	public static File createTempFile(String prefix, String suffic) {
		try {
			return File.createTempFile(prefix, suffic);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	public FXMLLoader getFxmlLoader() {
		return fxmlLoader;
	}

	public String getProjectVersion() {
		try {
			final Properties properties = new Properties();
			properties.load(getClass().getClassLoader().getResourceAsStream("project.properties"));
			return properties.getProperty("version");
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return "";
	}

	public static File exportStringToFile(File tempFile, String tableResult) {
		try (InputStream tableResultContent = new ByteArrayInputStream(tableResult.getBytes(StandardCharsets.UTF_8));
				ReadableByteChannel readableByteChannel = Channels.newChannel(tableResultContent);
				FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
				FileChannel fileChannel = fileOutputStream.getChannel()) {
			fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}

		return tempFile;
	}

	public static File exportStreamToFile(File tempFile, ByteArrayInputStream jobLogContent) {
		int n = jobLogContent.available();
		byte[] bytes = new byte[n];
		String tableResult = new String(bytes, StandardCharsets.UTF_8);

		return exportStringToFile(tempFile, tableResult);
	}

	public static void openFile(File tempFile) {
		Desktop desktop = Desktop.getDesktop();
		if (tempFile.exists()) {
			try {
				desktop.open(tempFile);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	public InputStream getResourceStream(String name) {
		return getClass().getClassLoader().getResourceAsStream(name);
	}

	public Pane loadFXML(String fxml) {
		fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
		Pane pane = null;
		try {
			pane = fxmlLoader.load();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return pane;
	}

	private static String getSettingProperty(String property, String defaultValue) {
		return getSettings().getProperty(property, defaultValue);
	}

	private static Object setSettingProperty(String property, String value) {
		return getSettings().setProperty(property, value);
	}

	/**
	 * Get the application settings properties as a singleton
	 */
	private static Properties getSettings() {
		if (settings == null) {
			String rootPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath();
			File settingsFile = new File(rootPath, "settings.properties");

			try {
				if (!settingsFile.exists() || !settingsFile.isFile()) {
					if (settingsFile.createNewFile())
						LOGGER.info("New settings.properties file created");
				}
				settings = new Properties();
				settings.load(new FileInputStream(settingsFile));
			} catch (FileNotFoundException e) {
				LOGGER.warning("Could not find settings.properties");
			} catch (IOException e) {
				LOGGER.warning("Could not read settings.properties");
			}
		}
		return settings;
	}

	public static File getExportPath() {
		if (exportPath != null) {
			return exportPath;
		}
		return new File(getSettingProperty("export.path", System.getenv("TEMP")));
	}

	public static String setExportPath(String path) {
		return (String) setSettingProperty("export.path", path);
	}
}