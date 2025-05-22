package HelpArticles;

import java.net.MalformedURLException;
import java.net.URI;

public class UrlValidator { 

	public static boolean isValidURL(String url) {

	    try {
	        URI.create(url).toURL();
	    } catch (MalformedURLException | IllegalArgumentException e) {
	        return false;
	    }

	    return true;
	}
}
