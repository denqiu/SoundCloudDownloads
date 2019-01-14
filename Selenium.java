package webdriver;

import java.io.IOException;

/**
 * This class holds the following classes: Drivers, Extensions, Files, and WebDrivers to be used in Selenium WebDriver.
 * @author Dennis Qiu
 */
public class Selenium {
	final public static String path = "C:\\Users\\Dennis\\Downloads\\Selenium\\Selenium Webdriver\\";
	final public static String drivers = "Drivers\\";
	final public static String extensions = "Extensions\\";

	/**
	 * This class holds the execution files for drivers.
	 * @author Dennis Qiu
	 */
	public class Drivers {
		final public static String chrome = path + drivers + "chromedriver.exe";
		final public static String fireFox = path + drivers + "geckodriver.exe";
	}
	
	/**
	 * This class holds extensions used in Selenium WebDriver.
	 * @author Dennis Qiu
	 */
	public class Extensions {
		final public static String adBlock = path + extensions + "AdBlock_v3.36.0.crx";
		final public static String adBlockPlus = path + extensions + "Adblock-Plus_v3.4.2.crx";
	}
	
	/**
	 * This class holds files used in Selenium Driver.
	 * @author Dennis Qiu
	 */
	public static class Files {
		public static String splitRegex = "SIFJKSR3874dkffksdfjsdHSDSFjdkjfkwIJFIU847jdjl";
		
		/**
		 * A String array of special characters that are not allowed when naming files.
		 * @return array of special characters not allowed when naming files.
		 */
		private static String[] chars() {
			Character[] specials = {' ','/','?','<','>','\\','|',':','*','"'};
			String[] chars = new String[11];
			for (int i = 0; i < specials.length; i++)
				chars[i] = specials[i].toString();
			chars[10] = "--";
			return chars;
		}
		
		/**
		 * Creates directories and filenames for SoundCloud Downloads Project.
		 * @param fileType can be txt or html 
		 * @param user for making a folder containing files for this particular user
		 * @return directories or filenames created for this particular user.
		 * @throws IOException 
		 */
		public static String SoundCloud(String fileType, String user, boolean search) {
			for (String s : chars())
				user = user.replace(s, "-");
			String folder = path + "Projects\\SoundCloudDownloads\\Users\\" + user;
			String file = folder + "\\" + user + "_Downloadable-Tracks." + fileType;
			String downloads = file.replace("." + fileType, "");
			if (search) {
				String[] split = user.split(splitRegex);
				folder = folder.replace("Users\\" + user, split[0]);
				if (split.length == 2) 
					file = folder + "\\" + split[1] + "." + fileType;
			}
			if (fileType == null)
				return folder;
			else if (fileType == "")
				return downloads;
			return file;
		}
	}
	
	/**
	 * This class holds webdrivers.
	 * @author Dennis Qiu
	 */
	public class WebDrivers {
		final public static String chrome = "webdriver.chrome.driver";
		final public static String fireFox = "webdriver.firefox.marionette";
	}
}