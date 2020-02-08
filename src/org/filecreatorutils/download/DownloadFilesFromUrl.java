package org.filecreatorutils.download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Methods to download files from urls
 * 
 * @author Desirée Abán
 *
 */
public class DownloadFilesFromUrl {
	// TODO insert
	private static final String MAIN_DIRECTORY = "{insert url here}";
	private static final String MAIN_FOLDER = "{insert result folder here}";

	public static void main(final String[] args) {

		searchDirectory("", MAIN_FOLDER);
		System.out.println("END");
	}

	private static void searchDirectory(final String url, final String folder) {
		System.out.println("Buscando en url: " + url);
		try {
			if (!checkIfFolderExists(folder)) {
				System.out.println("Creando directorio: " + folder);
				(new File(folder)).mkdirs();
			}
			final Document doc = Jsoup.connect(MAIN_DIRECTORY + url).get();
			final Elements links = doc.getElementsByTag("a");
			for (Element link : links) {
				final String text = url + link.attr("href");
				if (text.endsWith("/")) {
					searchDirectory(text, MAIN_FOLDER + text);
				} else {
					final String[] split = text.split("/");
					final String file = split[split.length - 1];
					if (file.contains(".")) {
						downloadFile(text);
					}
				}
			}
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Downloads a single file from an url if it doesn't exist in the filesystem
	 * 
	 * @param url
	 */
	private static void downloadFile(final String url) {
		if (!checkIfFileExists(MAIN_FOLDER + url)) {
			System.out.println("Descargando archivo: " + url);
			try (BufferedInputStream inputStream = new BufferedInputStream(new URL(MAIN_DIRECTORY + url).openStream());
					FileOutputStream fileOS = new FileOutputStream(MAIN_FOLDER + url)) {
				final byte[] data = new byte[1024];
				int byteContent;
				while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
					fileOS.write(data, 0, byteContent);
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Checks if folder exists
	 * 
	 * @param folderName
	 * @return
	 */
	private static boolean checkIfFolderExists(final String folderName) {
		boolean found = false;

		final File file = new File(folderName);
		if (file.exists() && file.isDirectory()) {
			found = true;
		}

		return found;
	}

	/**
	 * Checks if file exists
	 * 
	 * @param fileName
	 * @return
	 */
	private static boolean checkIfFileExists(final String fileName) {
		boolean found = false;

		final File file = new File(fileName);
		if (file.exists() && file.isFile()) {
			found = true;
		}

		return found;
	}
}
