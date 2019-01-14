package downloads;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import webdriver.*;

/**
 * This class manages tracks that can be downloaded in SoundCloud inside all the playlists a user has created.
 * @author Dennis Qiu
 */
public class DownloadsManagement {
	final private static String[] answer = {"Yes", "No"};
	public static int confirm = 0;
	final private static String[] options = {"Search By Playlist Link", "Search By Playlist Name", "Difference Between Playlist Link and Playlist Name?"};		
	private static int playlistOption = 0;
	private static boolean manage = false;
	private static boolean search = false;
	private static boolean emptyPlaylist = false;
	private static ArrayList<String> users = new ArrayList<String>();
	private static ArrayList<String> checkDuplicatePlaylists = new ArrayList<String>();
	private static ArrayList<String> allPlaylists = new ArrayList<String>();
	public static ArrayList<String> downloadableTracks = new ArrayList<String>();
	private	static LinkedHashSet<Integer> manageStart = new LinkedHashSet<Integer>();
	private static String baseUrl = "https://soundcloud.com";
	private static String userUrl = "";
	private static String playlistUrl = "";
	private static String user = "";
	
	public static void main(String[] args) throws IOException, AddressException {
		searchPlaylistsOptions(false);
		addUsers();
	}
	
	/**
	 * Sets up several options in finding user's playlists.
	 */
	public static void searchPlaylistsOptions(boolean search) {
		String option = (String) JOptionPane.showInputDialog(null, "Please choose an option:", "Playlist Options", JOptionPane.DEFAULT_OPTION, null, options, null);
		for (int i = 0; i < options.length; i++) {
			if (option != null) {
				if (option.equals(options[i])) {
					confirm = JOptionPane.showOptionDialog(null, "Are you sure you wish to continue?", options[i], JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
					if (confirm == JOptionPane.YES_OPTION) {
						playlistOption = i+1;
					} else {
						searchPlaylistsOptions(search);
					}
				}
			} else {
				confirm = JOptionPane.showOptionDialog(null, "Are you sure you wish to cancel?", "Options", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
				if (confirm == JOptionPane.YES_OPTION) {
					if (search) 
						return;
					else 
						System.exit(0);
				} else {
					option = "";
					searchPlaylistsOptions(search);
				}
			}
		}
		StringBuilder note = new StringBuilder("Searching through playlists by their links will always find the playlist.\n\n");
		note.append("Searching playlists by their names will not always find the playlists\nbut this will find out if the playlist has a misleading link,\n");
		note.append("i.e, playlist name: 'aaa' w / link: '" + baseUrl + "/user/sets/aa'.\n\n");
		note.append("If the playlist does have a misleading link PLEASE correct it.\n");
		note.append("Any person trying to find the playlist, besides using Selenium WebDriver,\nMAY have an issue with the playlist's misleading link as well.\n");
		if (playlistOption == 3) {
			playlistOption = 0;
			JOptionPane.showMessageDialog(null, note, "Playlist Options", JOptionPane.INFORMATION_MESSAGE);
			searchPlaylistsOptions(search);
		}
	}
	
	/**
	 * Sets up options in adding, editing, and removing users.
	 * @throws AddressException 
	 * @throws IOException
	 */
	private static void addUsers() throws AddressException, IOException {
		JPanel getUsers = new JPanel();
		JLabel enterUser = new JLabel("Enter Users: ");
		JTextField addUser = new JTextField(15);
		addUser.setMaximumSize(addUser.getPreferredSize());
		getUsers.add(enterUser);
		getUsers.add(addUser);
		JPanel show = new JPanel();
		JLabel showUsers = new JLabel("Users: " + users.toString().replace("[", "").replace("]", ""));
		show.add(showUsers);
		Object[] layout = {getUsers, show};
		Object[] userOptions = {"Add Users", "Edit User", "Remove Users", "Done", "Search Users"};
		if (users.isEmpty()) {
			show.setVisible(false);
			userOptions = new Object[] {userOptions[0], userOptions[4]};
		}
		int userInput = JOptionPane.showOptionDialog(null, layout, options[playlistOption-1] + " : Enter Users", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, userOptions, null);
		if (userInput != userOptions.length) {
			if (userInput == JOptionPane.YES_OPTION) {
				if (addUser.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "Please enter a User", "Enter Users", JOptionPane.ERROR_MESSAGE);
				} else {
					String[] split = addUser.getText().split(",");
					for (String s : split)
						users.add(s.trim());
				}
			} else if (userInput == JOptionPane.NO_OPTION) {
				if (users.isEmpty()) {
					searchUsers();
				} else {
					JPanel edit = new JPanel();
					enterUser = new JLabel("Edit Which User: ");
					JComboBox<String> editUsers = new JComboBox<String>();
					for (String u : users)
						editUsers.addItem(u);
					edit.add(enterUser);
					edit.add(editUsers);
					JPanel newUser = new JPanel();
					showUsers = new JLabel("Enter new User: ");
					newUser.add(showUsers);
					newUser.add(addUser);
					layout = new Object[] {edit, newUser};
					userOptions = new Object[] {"Done", "Back"};
					userInput = JOptionPane.showOptionDialog(null, layout, "Edit User", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, userOptions, null);
					if (userInput == JOptionPane.YES_OPTION)
						users.set(users.indexOf(editUsers.getSelectedItem()), addUser.getText());
				}
			} else if (userInput == JOptionPane.CANCEL_OPTION) {
				JPanel remove = new JPanel();
				enterUser = new JLabel("Remove Which Users: ");
				String[] removeArray = new String[users.size()];
				for (int i = 0; i < users.size(); i++)
					removeArray[i] = users.get(i);
				JList<String> removeUsers = SearchUsers.createJList(removeArray);
				JScrollPane scroll = SearchUsers.getScroll(removeUsers);
				remove.add(enterUser);
				remove.add(scroll);
				userOptions = new Object[] {"Done", "Back"};
				userInput = JOptionPane.showOptionDialog(null, remove, "Remove User", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, userOptions, null);
				if (userInput == JOptionPane.YES_OPTION) {
					List<String> getRemove = removeUsers.getSelectedValuesList();
					for (String r : getRemove)
						users.remove(users.indexOf(r));
				}
			} else if (userInput == JOptionPane.QUESTION_MESSAGE) {
				confirm = JOptionPane.showOptionDialog(null, "Are you sure you wish to continue?", showUsers.getText(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
				if (confirm == JOptionPane.YES_OPTION)
					return;
			} else if (userInput == JOptionPane.DEFAULT_OPTION){
				confirm = JOptionPane.showOptionDialog(null, "Are you sure you wish to cancel?", "Options", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
				if (confirm == JOptionPane.YES_OPTION)
					searchPlaylistsOptions(false);
			} else {
				searchUsers();
			}
			addUsers();
		}
		if (manage)
			return;
		checkUserFolderExists();
	}
	
	/**
	 * Checks if user's folder has already been created. If so, checks if txt or html file exists and proceeds to do something to them.
	 * @throws AddressException 
	 * @throws IOException
	 */
	private static void checkUserFolderExists() throws AddressException, IOException {
		String userFolder = Selenium.Files.SoundCloud(null, users.get(0), false);
		File checkUser = new File(userFolder);
		String text = Selenium.Files.SoundCloud("txt", users.get(0), false);
		File checkText = new File(text);
		String html = Selenium.Files.SoundCloud("html", users.get(0), false);
		File checkHtml = new File(html);
		if (checkUser != null) {
			if (checkUser.exists()) {
				if (!checkText.exists() && !checkHtml.exists())
					setUpWebDriver(false, false);
				if (!manage) {
					user = users.get(0);
					JOptionPane.showMessageDialog(null, "You have found downloadable tracks for " + user + " already", "Found downloadable tracks for " + user, JOptionPane.INFORMATION_MESSAGE);
					createDownloadsFile("", true, null);
				}
				if (!checkText.exists() || !checkText.exists() && manage) {
					confirm = JOptionPane.showOptionDialog(null, "You do not have a text file containing your downloadable tracks. Would you like to create one?", "Create Text File", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
					if (confirm == JOptionPane.YES_OPTION) {
						manage = false;
						baseUrl = html;
						setUpWebDriver(true, false);
						createDownloadsFile("txt", true, null);
					} else if (confirm == JOptionPane.NO_OPTION) {
						manage = false;
					} else {
						manage = true;
						JOptionPane.showMessageDialog(null, "Please choose an option", "Create Text File", JOptionPane.ERROR_MESSAGE);
						checkUserFolderExists();
					}
				}
				if (checkHtml.exists() || checkHtml.exists() && manage) {
					confirm = JOptionPane.showOptionDialog(null, "Do you wish to open your downloadable tracks automatically?", "Downloads", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
					if (confirm == JOptionPane.YES_OPTION) {
						manage = false;
						baseUrl = html;
						setUpWebDriver(true, true);
					} else if (confirm == JOptionPane.NO_OPTION) {
						manage = false;
						users.remove(0);
						return;
					} else {
						manage = true;
						JOptionPane.showMessageDialog(null, "Please choose an option", "Downloads Error", JOptionPane.ERROR_MESSAGE);
						checkUserFolderExists();
					}
				} else if (!checkHtml.exists() || manage){
					confirm = JOptionPane.showOptionDialog(null, "You do not have a html file containing your downloadable tracks. Would you like to create one?", "Create HTML File", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
					if (confirm == JOptionPane.YES_OPTION) {
						createDownloadsFile("html", true, null);
					} else if (confirm == JOptionPane.NO_OPTION) {
						manage = false;
						users.remove(0);
						return;
					} else {
						manage = true;
						JOptionPane.showMessageDialog(null, "Please choose an option", "Create HTML File", JOptionPane.ERROR_MESSAGE);
						checkUserFolderExists();
					}
				}
			} else {
				setUpWebDriver(false, false);
			}
		}
	}
	
	/**
	 * Sets up a Web Driver.
	 * @param downloads true if downloadable tracks have already been found for user, otherwise false
	 * @param openTabs true if html file exists and want to open tracks in new tabs automatically
	 * @throws AddressException
	 * @throws IOException
	 */
	private static void setUpWebDriver(boolean downloads, boolean openTabs) throws AddressException, IOException {
		if (!downloads) 
			System.out.println(options[playlistOption-1] + "\nUsers: " + users.toString().replace("[", "").replace("]", ""));
		System.setProperty(Selenium.WebDrivers.chrome, Selenium.Drivers.chrome);
		WebDriver driver = new ChromeDriver();
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		try {
			driver.manage().window().maximize();
			driver.get(baseUrl);
			multipleAdsTabs(driver);
			mainAdTab(driver, baseUrl);
			cookies(driver, executor);
			if (!downloads) 
				findUsers(driver, executor);
			else 
				downloads(driver, openTabs, 1);			
		}  catch (NoSuchWindowException n) {
			if (!downloads) {
				if (playlistUrl == "")
					allPlaylists.clear(); checkDuplicatePlaylists.clear(); 
				manageStart.clear(); 
			}
			System.out.println("Chrome window closed unexpectedly");
			setUpWebDriver(downloads, openTabs);
		} catch (WebDriverException w) {
			if (!downloads) {
				if (playlistUrl == "")
					allPlaylists.clear(); checkDuplicatePlaylists.clear(); 
				manageStart.clear(); 
			}
			System.out.println("WebDriver closed unexpectedly");
			setUpWebDriver(downloads, openTabs);
		}
	}

	/**
	 * Finds the url part containing the user's name.
	 * @param driver gets url to pull up the window of user's list of playlists
	 * @param executor allows click to go through successfully
	 * @throws AddressException 
	 * @throws IOException
	 */
	private static void findUsers(WebDriver driver, JavascriptExecutor executor) throws AddressException, IOException {
		if (users.isEmpty()) {
			driver.close();
			return;
		}
		userUrl = "";
		user = users.get(0);
		String usersUrlFormat = UrlFormats.formats(user);
		userUrl = baseUrl + "/" + usersUrlFormat + "/sets";
		driver.get(userUrl);
		multipleAdsTabs(driver);
		if (mainAdTab(driver, userUrl))
			findUsers(driver, executor);
		cookies(driver, executor);
		try {
			WebElement error = driver.findElement(By.className("errorTitle"));
			if (error.isDisplayed()) {
				manage = true;
				JOptionPane.showMessageDialog(null, "This user does not exist. Please change your user.", user, JOptionPane.ERROR_MESSAGE);
				addUsers();
				System.out.println(options[playlistOption - 1] + "\nUsers: " + users.toString().replace("[", "").replace("]", ""));
				manage = false;
				findUsers(driver, executor);
			}
		} catch (NoSuchElementException n) {
			if (playlistUrl == "")
				searchPlaylists(driver, executor, 1, null);
		}
		if (emptyPlaylist)
			return;
		eachPlaylist(driver, executor, null);
	}
	
	/**
	 * Finds the links to html file that was created. 
	 * @param driver gets the element of each link 
	 * @param openTabs if true, open the links in new tabs, otherwise add to ArrayList to create txt file
	 * @param startTab ensures all tabs in html file will open automatically
	 */
	private static void downloads(WebDriver driver, boolean openTabs, int startTab) {
		List<WebElement> allLinks = driver.findElements(By.tagName("li"));
		for (int i = startTab; i <= allLinks.size(); i++) {
			WebElement link = driver.findElement(By.xpath("//li[" + i + "]/a"));
			if (openTabs) {
				if (!manage)
					link.sendKeys(Keys.CONTROL, Keys.ENTER);
				if (i % 7 == 0 || manage) {
					int size = driver.getWindowHandles().size();
					if (size > 1) {
						manage = true;
						JOptionPane.showMessageDialog(null, "Please close all SoundCloud tabs before you can continue", "SoundCloud Windows", JOptionPane.INFORMATION_MESSAGE);
						downloads(driver, openTabs, i);
					}
					manage = false;
				}
			} else {
				downloadableTracks.add(link.getAttribute("href"));	
			}
		}
	}
	
	private static void searchUsers() throws AddressException, IOException {
		confirm = JOptionPane.showOptionDialog(null, "Are you sure you wish to search users?", "Search Users", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
		if (confirm == JOptionPane.YES_OPTION) {
			String split = Selenium.Files.splitRegex;
			String searchUsers = Selenium.Files.SoundCloud(null, "Search Users" + split, true);
			File search = new File(searchUsers);
			if (!search.exists())
				search.mkdir(); 
			SearchUsers.setUpWebDriver(baseUrl);
		} else if (confirm == JOptionPane.NO_OPTION) {
			return;
		}
	}
	
	/**
	 * Searches through all the playlists on the user's account.
	 * @param driver finds playlist elements
	 * @param executor scrolls down all playlists
	 * @param startPlaylist prevents the scroll function from scrolling upwards to a already visited playlist
	 * @throws AddressException 
	 * @throws IOException
	 */
	public static void searchPlaylists(WebDriver driver, JavascriptExecutor executor, int startPlaylist, String getSearchUrl) throws AddressException, IOException {
		String ul = "lazyLoadingList__list";
		if (getSearchUrl != null) {
			ul = "soundList";
			checkDuplicatePlaylists.clear(); 
			allPlaylists.clear();
			downloadableTracks.clear();
			search = false; emptyPlaylist = false;
		}
		int i = 0;
		try {
			for (i = startPlaylist; i > 0; i++) {
				try {
					WebElement end = driver.findElement(By.className("paging-eof"));
					if (end.isDisplayed()) {
						WebElement container = driver.findElement(By.cssSelector("ul." + ul));
						List<WebElement> allItems = container.findElements(By.cssSelector("li.soundList__item"));
						for (i = manageStart.size()+1; i <= allItems.size(); i++) {
							if (emptyPlaylist) {
								playlistOption = 0;
								break;
							}
							getPlaylists(driver, executor, i, getSearchUrl);
							foundCorrectLink(driver, executor, i);
						}
						manageStart.clear();
						if (playlistOption == 2) {
							LinkedHashSet<String> uniquePlaylists = new LinkedHashSet<String>(checkDuplicatePlaylists);
							allPlaylists.addAll(uniquePlaylists);
							for (String p : uniquePlaylists) {
								if (allPlaylists.size() == checkDuplicatePlaylists.size()) {
									Collections.sort(allPlaylists);
									return;
								}
								for (i = 1; i < Collections.frequency(checkDuplicatePlaylists, p); i++) {
									p = p + "-" + i;
									allPlaylists.add(p);
								}
							}
						}
						return;
					}
				} catch (NoSuchElementException n) {
					getPlaylists(driver, executor, i, getSearchUrl);
					foundCorrectLink(driver, executor, i);
				}
			}
		} catch (NoSuchElementException s) {
			searchPlaylists(driver, executor, i, getSearchUrl);
		}
	}
	
	private static void foundCorrectLink(WebDriver driver, JavascriptExecutor executor, int i) throws AddressException, IOException {
		if (manage && i == checkDuplicatePlaylists.indexOf(allPlaylists.get(0))+1) {
			manageStart.clear();
			correctPlaylistLink(driver, executor);
			eachPlaylist(driver, executor, null);
		}
	}
		
	/**
	 * Gets the element of the playlist based on the playlist options.
	 * @param driver finds playlist elements, either link or name
	 * @param executor scrolls a playlist into view 
	 * @param i the number assigned to the playlist's li element
	 * @throws AddressException 
	 * @throws IOException 
	 */
	private static void getPlaylists(WebDriver driver, JavascriptExecutor executor, int i, String getSearchUrl) throws AddressException, IOException {
		try {
			WebElement retry = driver.findElement(By.cssSelector("div.inlineError"));
			if (retry.isDisplayed()) 
				executor.executeScript("arguments[0].click();", retry);
			getPlaylists(driver, executor, i, getSearchUrl);
		} catch (NoSuchElementException n) {
			manageStart.add(i); 
			for (Integer m : manageStart) 
				i = m;
			String xPath = "/div/div/div[2]/div[1]/div/div/div[2]/a";
			if (getSearchUrl != null)
				xPath = "/div" + xPath;
			WebElement playlist = driver.findElement(By.xpath("//li[" + i + "]" + xPath));
			if (playlistOption == 1) {
				allPlaylists.add(playlist.getAttribute("href"));	
			} else if (playlistOption == 2) {
				playlist = playlist.findElement(By.tagName("span"));
				String getPlaylists = playlist.getText();
				String playlistUrlFormat = UrlFormats.formats(getPlaylists);
				/*System.out.println("#" + i + " " + playlistUrlFormat);*/
				checkDuplicatePlaylists.add(playlistUrlFormat);
			}
			executor.executeScript("arguments[0].scrollIntoView();", playlist);
			multipleAdsTabs(driver);
			if (getSearchUrl != null)
				userUrl = getSearchUrl;
			if (mainAdTab(driver, userUrl))
				findUsers(driver, executor);
			cookies(driver, executor);
		}
	}

	/**
	 * Goes through all playlists user has created.
	 * @param driver gets url of each playlist
	 * @param executor allows click to go through successfully
	 * @throws AddressException 
	 * @throws IOException
	 */
	public static void eachPlaylist(WebDriver driver, JavascriptExecutor executor, String getSearchUser) throws AddressException, IOException {
		if (getSearchUser != null) {
			search = true;
			user = getSearchUser;
			users.clear();
			users.add(user);
			userUrl = userUrl + "/sets";
		}
		if (allPlaylists.isEmpty()) {
			playlistUrl = "";
			if (!search) {
				createDownloadsFile(null, false, null);
				sendMail();
				users.remove(0);
				findUsers(driver, executor);
			} else {
				emptyPlaylist = true;
			}
			return;
		}
		try {
			String playlist = allPlaylists.get(0);
			if (playlistOption == 1) {
				playlistUrl = playlist;
			} else if (playlistOption == 2) {
				playlistUrl = userUrl;
				playlistUrl = playlistUrl + "/" + playlist;
			}
			/*System.out.println(allPlaylists);*/
			multipleAdsTabs(driver);
			driver.get(playlistUrl);
			if (mainAdTab(driver, playlistUrl)) 
				eachPlaylist(driver, executor, null);
			cookies(driver, executor);
			try {
				WebElement error = driver.findElement(By.className("errorTitle"));
				if (error.isDisplayed()) {
					correctPlaylistLink(driver, executor);
					eachPlaylist(driver, executor, null);
				}
			} catch (NoSuchElementException n) {
				stopPlay(driver, executor);
				searchDownloads(driver, executor, 1, null);
				allPlaylists.remove(0);
			}
			eachPlaylist(driver, executor, null);
		} catch (NoSuchElementException n) {
			allPlaylists.remove(0);
			eachPlaylist(driver, executor, null);
		}
	}
	
	/**
	 * Creates a file containing the URLs to the downloadable tracks.
	 * @param fileType type of file, either txt or html
	 * @throws IOException
	 * @throws AddressException 
	 */
	public static void createDownloadsFile(String fileType, boolean userExists, String getSearchUser) throws IOException {
		if (getSearchUser != null)
			user = getSearchUser;
		if (!userExists) {
			if (fileType == null) {
				JLabel files = new JLabel("Check the files you want to create to hold the URL's to the downloadable tracks.");
				JCheckBox text = new JCheckBox("Create a text file");
				JCheckBox html = new JCheckBox("Create a html file");
				Object[] check = {text, html, "Done"}; 
				int checkInput = JOptionPane.showOptionDialog(null, files, "Create File", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, check, null); 
				if (checkInput == JOptionPane.CANCEL_OPTION) {
					if (!text.isSelected() && !html.isSelected()) {
						createDownloadsFile(null, false, getSearchUser);
					} else if (text.isSelected() || html.isSelected()) {
						if (text.isSelected())
							createDownloadsFile("txt", false, getSearchUser);
						if (html.isSelected())
							createDownloadsFile("html", false, getSearchUser);
					}
				} else {
					JOptionPane.showMessageDialog(null, "Please check an option.", "Create Files Error", JOptionPane.ERROR_MESSAGE);
					createDownloadsFile(null, false, getSearchUser);
				}
			}
		} else {
			if (fileType != null) {
				if (fileType.equals("html")) {
					String makeFile = Selenium.Files.SoundCloud("txt", user, false);
					File f = new File(makeFile);
					Scanner s = new Scanner(f);
					while (s.hasNextLine())
						downloadableTracks.add(s.nextLine());
					s.close();
				}
			}
		}
		String makeFolder = Selenium.Files.SoundCloud(null, user, false);
		File userFolder = new File(makeFolder);
		if (!userFolder.exists())
			userFolder.mkdir();
		String makeFile = Selenium.Files.SoundCloud(fileType, user, false);
		File file = new File(makeFile);
		String makeDownloads = Selenium.Files.SoundCloud("", user, false);
		File downloadFolder = new File(makeDownloads);
		if (!downloadFolder.exists())
			downloadFolder.mkdir();
		Collections.sort(downloadableTracks);
		LinkedHashSet<String> downloads = new LinkedHashSet<String>(downloadableTracks);
		ArrayList<String> checkDuplicateContent = new ArrayList<String>();
		if (fileType != null) {
			if (file.exists()) {
				Scanner s = new Scanner(file);
				while (s.hasNextLine()) 
					checkDuplicateContent.add(s.nextLine());
				s.close();
			}
			FileWriter fw;
			if (fileType.equals("txt")) {
				fw = new FileWriter(makeFile, true);
				for (String d : downloads) {
					if (!checkDuplicateContent.toString().contains(d))
						fw.write(d + "\r\n");
					else if (checkDuplicateContent.isEmpty())
						fw.write(d + "\r\n");
				}
				fw.close();
				new ProcessBuilder("notepad", makeFile).start();
			} else if (fileType.equals("html")) {
	            StringBuilder makeHtml = new StringBuilder();
	            makeHtml.append("<!doctype html><html lang='en'><body><style>"); 
	            makeHtml.append("body{background-color:blue;}li{color:white;}a{color:white;}a:hover{color:yellow;}");
	            makeHtml.append("a:visited{color:cyan;}a:visited:hover{color:yellow;}</style><center>");
	            int i = 0;
	            for (String d : downloads) {
					if (!checkDuplicateContent.toString().contains(d)) {
						i++;
			            makeHtml.append("<li><a id = 'track" + i + "' href='" + d + "'>" + d + "</a></li><br>");
	            	} else if (checkDuplicateContent.isEmpty()) {
	            		i++;
			            makeHtml.append("<li><a id = 'track" + i + "' href='" + d + "'>" + d + "</a></li><br>");
	            	}
	            }
	            makeHtml.append("</center></body></html>");
				File htmlFile = new File(makeFile);
				fw = new FileWriter(htmlFile, true);
				fw.write(makeHtml.toString());
				fw.close();
				Desktop.getDesktop().browse(htmlFile.toURI());
			} 
		}
	}
	
	public static void sendMail() throws AddressException, IOException {
		confirm = JOptionPane.showOptionDialog(null, "Do you wish to send emails?", "Send Emails", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
		if (confirm == JOptionPane.YES_OPTION) {
			SendMail.addEmails(confirm, answer, user);
		} else if (confirm == JOptionPane.NO_OPTION) {
			return;
		} else {
			JOptionPane.showMessageDialog(null, "Please choose an option", "Send Emails", JOptionPane.ERROR_MESSAGE);
			createDownloadsFile(null, true, null);
		}
	}
	
	/**
	 * Finds the correct link to the playlist, either continues using it or finds a new one if user did not change it in SoundCloud.
	 * @param driver gets url of found playlist
	 * @param executor executes argument in playlist
	 * @throws AddressException 
	 * @throws IOException
	 */
	private static void correctPlaylistLink(WebDriver driver, JavascriptExecutor executor) throws AddressException, IOException {
		String input = ""; String[] select = null;
		if (!manage) {
			StringBuilder error = new StringBuilder("This playlist, " + allPlaylists.get(0) + ", has a misleading link.");
			error.append("\nPLEASE ask this user, " + user + ", to change the link. ");
			error.append("\nIf this is not possible, finding the playlist's link will be necessary. Select 'Option 1' to find this playlist's link.");
			error.append("\nIf the playlist's link has been corrected, select 'Option 2'.");
			select = new String[] {"Option 1: Find " + allPlaylists.get(0) + "'s link", "Option 2: Continue using " + playlistUrl};
			input = (String) JOptionPane.showInputDialog(null, error.toString(), "Playlist Link", JOptionPane.ERROR_MESSAGE, null, select, null);
		}
		if (input != null) {
			if (manage) {
				String link = allPlaylists.get(allPlaylists.size() - 1);
				link = link.substring(link.lastIndexOf("/") + 1);
				allPlaylists.set(0, link);
				Collections.reverse(allPlaylists);
				ArrayList<String> removeHttp = new ArrayList<String>();
				for (String p : allPlaylists) {
					if (p.contains(baseUrl)) {
						removeHttp.add(p);
					} else {
						allPlaylists.removeAll(removeHttp);
						Collections.reverse(allPlaylists);
						manage = false;
						playlistOption = 2;
						return;
					}
				}
			} else if (input.equals(select[0])) {
				playlistOption = 1;
				playlistUrl = "";
				manage = true;
				findUsers(driver, executor);
			} else if (input.equals(select[1])) {
				return;
			}  
		} else {
			JOptionPane.showMessageDialog(null, "Please choose an option.", "Playlist Link Error", JOptionPane.ERROR_MESSAGE);
			correctPlaylistLink(driver, executor);
		}
	}

	/**
	 * Searches through all downloads in a given playlist.
	 * @param driver checks if downloads are available or not
	 * @param executor executes argument to downloads
	 * @param startDownload prevents scroll from scrolling upwards
	 * @throws AddressException 
	 * @throws IOException 
	 */
	public static void searchDownloads(WebDriver driver, JavascriptExecutor executor, int startDownload, String searchPlaylistUrl) throws AddressException, IOException {
		if (searchPlaylistUrl != null)
			downloadableTracks.clear();
		int i = 0;
		try {
			try {
				WebElement empty = driver.findElement(By.cssSelector("div.emptyNetworkPage"));
				if (empty.isDisplayed())
					return;
			} catch (NoSuchElementException n) {
				for (i = startDownload; i > 0; i++) {
					try {
						WebElement end = driver.findElement(By.className("paging-eof"));
						if (end.isDisplayed()) {
							WebElement container = driver.findElement(By.cssSelector("ul.trackList__list"));
							List<WebElement> allItems = container.findElements(By.cssSelector("li.trackList__item"));
							for (i = manageStart.size()+1; i <= allItems.size(); i++) 
								getDownloads(driver, executor, i, searchPlaylistUrl);
							manageStart.clear();
							return;
						}
					} catch (NoSuchElementException s) {
						getDownloads(driver, executor, i, searchPlaylistUrl);
					}
				}
			}
		} catch (NoSuchElementException e) {
			searchDownloads(driver, executor, i, searchPlaylistUrl);
		}
	}
	
	/**
	 * Gets the downloads found inside each playlist.
	 * @param driver finds if track contains download or not
	 * @param executor executes a click and scroll into view 
	 * @param i the li element of each track
	 * @throws AddressException 
	 * @throws IOException 
	 */
	private static void getDownloads(WebDriver driver, JavascriptExecutor executor, int i, String searchPlaylistUrl) throws AddressException, IOException  {
		try {
			manageStart.add(i); 
			for (Integer m : manageStart) 
				i = m;
			WebElement more = driver.findElement(By.xpath("//li[" + i + "]/div/div[4]/div/div/div/button[4]"));
			executor.executeScript("arguments[0].click();", more);
			WebElement dropDown = driver.findElement(By.className("dropdownMenu"));
			if (dropDown.getText().contains("Download")) {
				WebElement trackLink = driver.findElement(By.xpath("//li[" + i + "]/div/div[3]/a[2]"));
				downloadableTracks.add(trackLink.getAttribute("href"));
			}
			WebDriverWait wait = new WebDriverWait(driver, 30);
			wait.until(ExpectedConditions.visibilityOf(more));
			executor.executeScript("arguments[0].scrollIntoView();", more);
			multipleAdsTabs(driver);
			if (searchPlaylistUrl != null) 
				playlistUrl = searchPlaylistUrl;
			if (mainAdTab(driver, playlistUrl))
				eachPlaylist(driver, executor, null);
			cookies(driver, executor);
		} catch (NoSuchElementException s) {
			return;
		}
	}
	
	/**
	 * Prevents ads popping up in new tabs from breaking code.
	 * @param driver gets window of ad
	 */
	public static void multipleAdsTabs(WebDriver driver) {
		String soundCloudWindow = driver.getWindowHandle();
		Set<String> s = driver.getWindowHandles();
		Iterator<String> i = s.iterator();
		while (i.hasNext()) {
			String adTabs = i.next();
			if (!soundCloudWindow.equalsIgnoreCase(adTabs)) {
				driver.switchTo().window(adTabs);
				driver.close();
			}
		}
		driver.switchTo().window(soundCloudWindow);
	}
	
	/**
	 * Brings back url of current SoundCloud Window if the window suddenly becomes an ad tab.
	 * @param driver compares current url of tab against recorded url and gets url if true
	 * @param url can either be baseUrl, userUrl, or playlistUrl
	 * @return true if ad appears, otherwise false
	 */
	public static boolean mainAdTab(WebDriver driver, String url) {
		if (!driver.getCurrentUrl().equals(url)) {
			manageStart.clear();
			driver.get(url);
			return true;
		}
		return false;
	}
	
	/**
	 * Clicks the cookie button.
	 * @param driver finds element of cookie
	 * @param executor allows click to go through successfully
	 */
	public static void cookies(WebDriver driver, JavascriptExecutor executor) {
		try {
			WebElement cookie = driver.findElement(By.className("announcement__ack"));
			executor.executeScript("arguments[0].click();", cookie);
		} catch (NoSuchElementException n) {
			return;
		}
	}

	/**
	 * Stops the music playing while looking for downloadable tracks inside a playlist.
	 * @param driver finds element of play button
	 * @param executor allows click to go through successfully
	 */
	public static void stopPlay(WebDriver driver, JavascriptExecutor executor) {
		WebElement play = driver.findElement(By.className("playControl"));
		executor.executeScript("arguments[0].click();", play);
	}
}
