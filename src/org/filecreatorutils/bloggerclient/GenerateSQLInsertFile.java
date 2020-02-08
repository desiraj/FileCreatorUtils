package org.filecreatorutils.bloggerclient;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class helps to migrate Blogger post to Wordpress post creating a sql
 * file for mysql database.
 * The only thing that you need to change is the commented "TODO" lines.
 * <p>
 * For further information, please consult the <a href=
 * "https://developers.google.com/blogger/docs/3.0/getting_started">Blogger
 * API<a>.
 * <p>
 * This class consumes the micro services thanks to the spring boot framework.
 * Spring boot framework was used instead of the Blogger libraries because the
 * Blogger libraries required a query complexity that was not necessary.
 * 
 * @author Desirée Abán
 *
 */
public class GenerateSQLInsertFile {

	// TODO replace appropriate blogger identifiers
	private static final String URL = "https://www.googleapis.com/blogger/v3/blogs/{BLOG_ID}/posts?key={OAUTH_ID}";
	private static final String REQUEST_SQL = "INSERT INTO wp_posts ( wp_posts.post_author, wp_posts.post_date, wp_posts.post_date_gmt, wp_posts.post_content, wp_posts.post_title, wp_posts.post_status, wp_posts.comment_status, wp_posts.ping_status, wp_posts.post_name, wp_posts.post_modified, wp_posts.post_modified_gmt, wp_posts.post_parent, wp_posts.guid, wp_posts.menu_order, wp_posts.post_type, wp_posts.comment_count, wp_posts.post_excerpt, wp_posts.to_ping, wp_posts.pinged, wp_posts.post_content_filtered) VALUES ";
	private static final String NEW_ELEMENT = "', '";
	private static final String SEVICE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
	private static final String SQL_DATE_FORMAT = "yyyy-MM-dd";

	public static void main(String[] args) {

		final RestTemplate restTemplate = new RestTemplate();
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT_CHARSET, "UTF-8");
		final HttpEntity<?> entity = new HttpEntity<>(headers);
		String pageToken = "";
		String requestUrl = URL;
		JsonNode posts = null;
		StringBuilder line = new StringBuilder();

		try {
			// TODO modify file path as you like
			FileOutputStream out = new FileOutputStream("the-file-name.sql");
			try {
				do {
					// Calling REST GET call
					ResponseEntity<String> result = restTemplate.exchange(requestUrl, HttpMethod.GET, entity,
							String.class);

					posts = new ObjectMapper().readTree(result.getBody());

					for (JsonNode post : posts.get("items")) {
						writeResponse(post, line);
					}

					// Pagination logic
					pageToken = posts.has("nextPageToken") ? posts.get("nextPageToken").asText() : null;
					requestUrl = URL + "&pageToken=" + pageToken;

				} while (!StringUtils.isEmpty(pageToken));
				out.write(line.toString().getBytes());
			} finally {
				out.close();
			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Writes sql statement according to the received data from the response
	 * 
	 * @param post
	 * @param line
	 * @throws ParseException
	 */
	private static void writeResponse(final JsonNode post, StringBuilder line) throws ParseException {
		final String title = post.get("title").asText();
		final String published = changeFormatDate(post.get("published").asText());
		final String content = post.get("content").asText();
		final String updated = changeFormatDate(post.get("updated").asText());
		final String formattedTitle = Normalizer.normalize(title, Normalizer.Form.NFKD).replaceAll("\\W", "")
				.replaceAll("\\s", "-").toLowerCase();

		line.append(REQUEST_SQL);
		// post_author
		line.append("('1', '");
		// post_date
		line.append(published);
		line.append(NEW_ELEMENT);
		// post_date_gmt
		line.append(published);
		line.append(NEW_ELEMENT);
		// post_content
		line.append(content.replace("\n", "</br>").replace("\r", "</br>").replace("'", "''"));
		line.append(NEW_ELEMENT);
		// post_title
		line.append(title);
		// post_status, comment_status, ping_status
		line.append("', 'publish', 'open', 'open', '");
		// post_name
		line.append(formattedTitle);
		line.append(NEW_ELEMENT);
		// post_modified
		line.append(updated);
		line.append(NEW_ELEMENT);
		// post_modified_gmt
		line.append(updated);
		// post_parent, guid
		//TODO please insert the Wordpress url
		line.append("', '0', 'https://{WORDPRESS_URL}/");
		line.append(formattedTitle);
		// menu_order, post_type, comment_count, post_excerpt, to_ping, pinged,
		// post_content_filtered
		line.append("', '0', 'post', '0', '', '', '', '');");
		line.append(String.format("%n"));
	}

	/**
	 * Changes service response date format to mysql date format
	 * 
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	private static String changeFormatDate(final String date) throws ParseException {

		final SimpleDateFormat sdf = new SimpleDateFormat(SEVICE_DATE_FORMAT);
		final Date d = sdf.parse(date);
		sdf.applyPattern(SQL_DATE_FORMAT);
		return sdf.format(d);
	}

}
