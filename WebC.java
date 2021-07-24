
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
public class WebC {
	/*
	 * Pattern to identify URL Refer - http://urlregex.com/
	 */
	
	private static final String URL_PATTERN_REGEX = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	private static final Pattern URL_PATTERN = Pattern.compile(URL_PATTERN_REGEX);

	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the Starting URL");
		String inputURL = sc.next();
		// Validate if the entered String is a valid URL
		if (!URL_PATTERN.matcher(inputURL).matches()) {
			throw new IllegalArgumentException(String.format("%s isn't a valid URL", inputURL));
		}
		System.out.println("Enter the maximum depth you want to continue crawling");
		int maxLevel = sc.nextInt();
		/*
		 * maxLevel is required for program to terminate
		 */
		if(maxLevel <= 0) {
			throw new IllegalArgumentException(String.format("maxLevel should be greater than 0", maxLevel));
		}
		/*
		 * Collection containing original URL's, The pages corresponding to it, will be
		 * Opened, Parsed and the URL's found in the page will be stored in#{childLinks}
		 * Set - because, it will avoid duplicates
		 */
		Set < String > seedURLs = new HashSet < > ();
		/*
		 * Collection containing all the links found in a page for a given String in
		 * #{originalLinks} Set -  because, it will avoid duplicates
		 */
		Set < String > childLinks = new HashSet < > ();
		// 1. Start with putting the entered URL into
		seedURLs.add(inputURL);
		int currentLevel = 1;
		while (currentLevel <= maxLevel) {
			for(String link: seedURLs) {
				// 2. For each link in originalLinks, get the content of the page
				String content = getWebPageContentAsString(link);
				if(content == null) {
					// If there is no content available continue with next
					continue;
				}
				// 3. Find all the valid URL's embedded in the page
				List < String > cLinks = getAllLinksInString(content);
				for (String childLink: cLinks) {
					//4. If the link is already traversed, don't add it. THis will avoid cyclic loop
					if(!seedURLs.contains(childLink)) {
						childLinks.add(childLink);
					}
				}
				
			}
			printOutput(currentLevel, childLinks);
			/*
			 * 5. After completing a level, a. clear original links, their childLinks
			 * already are printed b. move all childLinks to originalLinks, it is now their
			 * turned to be parsed c. clear the childLinks d. increment currentLevel
			 */
			seedURLs.clear();
			seedURLs.addAll(childLinks);
			childLinks.clear();
			currentLevel++;
		}
		System.out.println("**********End Of Program**********");
	}
	public static void printOutput(int currentLevel, Set < String > links) {
		System.out.println(String.format("===============START : DEPTH = %s ================", currentLevel));
		int index = 1;
		for(String link : links) {
			System.out.println(index + ".> " + link);
			index++;
		}
		System.out.println(String.format("===============END : DEPTH = %s ===============", currentLevel));
	}
	/*
	 * This function uses java networking API (java.net) and java IO API (java.io)
	 * to 1. Open the page for the given URL link 2. Store the contents in String 3.
	 * Return the String content
	 * 
	 * @return
	 * @throws exception
	 */
	private static String getWebPageContentAsString(String urlLink) throws Exception {
		URL url = new URL(urlLink);
		String result = null;
		try {
			URLConnection urlConnection = url.openConnection();
			InputStream is = urlConnection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			int numCharsRead;
			char[] charArray = new char[1024];
			StringBuffer sb = new StringBuffer();
			while((numCharsRead = isr.read(charArray)) > 0) {
				sb.append(charArray, 0, numCharsRead);
			}
			result = sb.toString();
		} catch (Exception e) {
			
		}
	return result;
	}
	private static List < String > getAllLinksInString(String content) throws IOException {
		Reader reader = new StringReader(content);
		HTMLEditorKit.Parser parser = new ParserDelegator();
		final List < String > links = new ArrayList < String > ();
		parser.parse(reader, new HTMLEditorKit.ParserCallback() {
			public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos)
			{
				if(t == HTML.Tag.A) {
					Object link = a.getAttribute(HTML.Attribute.HREF);
					if(link != null) {
						String linkValue = String.valueOf(link);
						if(linkValue.startsWith("http") || linkValue.startsWith("www")) {
							links.add(linkValue);
						}
					}
				}
			}
				
		}, true);
		reader.close();
		return links;
	}
	
	

}
/*
INPUT
Enter the Starting URL
http://codepad.org/
Enter the maximum depth you want to continue crawling
2

OUTPUT
===============START : DEPTH = 1 ================
1.> http://www.vim.org/scripts/script.php?script_id=2298
2.> http://saucelabs.com/
3.> http://github.com/ruediger/emacs-codepad
===============END : DEPTH = 1 ===============
===============START : DEPTH = 2 ================
1.> https://www.vim.org/scripts/script.php?script_id=2298
===============END : DEPTH = 2 ===============
**********End Of Program**********
*/