# SoundCloudDownloads
Implements Selenium WebDriver and GUIs to find users in SoundCloud and find tracks that can be downloaded in the user's playlists.
# Contains features such as adding, editing, and removing users.
![alt text](https://github.com/dwq9172/SoundCloudDownloads/blob/master/AddUsers.JPG)
# Selenium Webdriver is used to find the user's url link, and thus from there can find all the playlists the user has created. It will find all the tracks that can be downloaded in each playlist and write their links to a txt or html file. 
# It is impossible to automate logging in to SoundCloud via WebDriver to download tracks directly, hence all tracks that can be downloaded are written to a file. The tracks in the html file will have to be manually clicked to get access to the download button in SoundCloud. Apparently login works in Selenium IDE but it does not record the html file. Interestingly enough, WebDriver can automate clicking on each of the links inside the html file. This is all in the DownloadsManagement class.
# The SendMail class allows sending txt and html files to multiple recipients, if you want to share the downloads you've found.
# OtherMethods contain unused methods. They're interesting methods that I don't need but may be useful to keep.
# The search users feature is currently in progress. Contains more complicated implementations such as placing a default text/prompt on the JTextField and giving the JTextField the properties of a JComboBox. 
![alt text](https://github.com/dwq9172/SoundCloudDownloads/blob/master/Default.JPG)
![alt text](https://github.com/dwq9172/SoundCloudDownloads/blob/master/SearchEngine.JPG)
![alt text](https://github.com/dwq9172/SoundCloudDownloads/blob/master/Selections.JPG)
