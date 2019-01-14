package downloads;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;*/
import org.openqa.selenium.chrome.ChromeOptions;
/*import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;*/
import org.openqa.selenium.remote.DesiredCapabilities;
/*import org.openqa.selenium.WebDriver;*/
import webdriver.*;
import webdriver.Selenium.Files;

public class OtherMethods {
	/*private static ArrayList<String> checkDuplicatePlaylists = new ArrayList<String>();
	private static ArrayList<String> allPlaylists = new ArrayList<String>();
	private static String userUrl = "";*/

	public static void ChromeOptions() {
		 ChromeOptions options = new ChromeOptions(); 
		 options.addExtensions(new File(Selenium.Extensions.adBlockPlus)); 
		 DesiredCapabilities capabilities = new DesiredCapabilities(); 
		 options.merge(capabilities);
	}
	
	/*private static boolean isAlertPresent(WebDriver driver) {
		try {
			driver.switchTo().alert(); 
			return true;
		} catch (NoAlertPresentException e) {
			return false;
		}
	}
	
	private static int maxCount(ArrayList<String> containDuplicatePlaylists2) {
		HashMap<String, Integer> m = new HashMap<String, Integer>();
		for (String p : containDuplicatePlaylists2) {
			if (m.containsKey(p)) {
				int frequency = m.get(p);
				frequency++;
				m.put(p, frequency);
			} else {
				m.put(p, 1);
			}
		}
		int maxFrequency = 0;
		for (Entry<String, Integer> max : m.entrySet()) {
			if (maxFrequency < max.getValue()) 
				maxFrequency = max.getValue();
		}
		return maxFrequency;
	}
	
	private static void findDuplicatePlaylistsByHTTP(WebDriver driver, JavascriptExecutor executor, int i, String p) throws IOException {
		if (!isAlertPresent(driver))
			executor.executeScript("alert('Finding duplicate playlists.');");
		for (i = 1; i < maxCount(checkDuplicatePlaylists); i++) {
			p = p + "-" + i;
			URL u = new URL(userUrl + "/" + p);
			HttpURLConnection h = (HttpURLConnection) u.openConnection();
			h.setRequestMethod("HEAD");
			h.connect();
			int code = h.getResponseCode();
			if (code == 200) {
				//System.out.println(code + ": " + u);
				allPlaylists.add(p);
			}
		}
	}*/
	
	public static class Generators {	
		public static String Random(Object selectedItem) {
			try {
				String textRandom = Files.SoundCloud("txt", "Random", true);
				File f = new File(textRandom);
				FileWriter fw = new FileWriter(textRandom);
				String getRandomString = "";
				if (f.exists()) {
					Scanner s = new Scanner(f);
					while (s.hasNextLine())
						getRandomString = s.nextLine();
					s.close();
					return getRandomString;
				} 
				String textFolder = Files.SoundCloud(null, "Search User", true);
				f = new File(textFolder);
				File[] findSelectedItem = f.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File f, String s) {
						return s.contains(selectedItem.toString());
					}
				});
				if (findSelectedItem != null) {
					for (File s : findSelectedItem) {
						getRandomString = Pattern.quote("_Random_") + "(.*?)" + Pattern.quote("_String");
						Matcher m = Pattern.compile(getRandomString).matcher(s.getName());
						while (m.find()) 
							getRandomString = m.group(1);
						fw.write(getRandomString);
						fw.close();
						return getRandomString;
					}
				}
				byte[] random = new byte[256];
				new Random().nextBytes(random);
				int randomSize = new Random().nextInt(256) + 1;
				String randomString = new String(random, Charset.forName("UTF-8"));
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < randomString.length(); i++) {
					char c = randomString.charAt(i);
					boolean isAlphabet = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
					if (isAlphabet && randomSize > 0) {
						sb.append(c);
						randomSize--;
					}
				}
				fw.write(sb.toString());
				fw.close();
				return sb.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "File Error";
		}
	}
}
