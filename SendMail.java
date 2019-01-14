package downloads;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import webdriver.Selenium;

/**
 * This class models sending an email to multiple recipients with the files that were created in the user folder.
 * @author Dennis Qiu
 */
public class SendMail {
	private static ArrayList<String> emails = new ArrayList<String>();
	private static String host = "";
	private static String sender = "";
	private static String password = "";

	/**
	 * Sets up options in adding, editing, and removing emails.
	 * @param confirm message to confirm cancellation or continuation
	 * @param answer either yes or no
	 * @param user parameter to be used in sendMail
	 * @throws AddressException
	 */
	public static void addEmails(int confirm, String[] answer, String user) throws AddressException {
		JPanel getEmail = new JPanel();
		JLabel enterEmail = new JLabel("Enter Recipient Emails: ");
		JTextField addEmail = new JTextField(25);
		getEmail.add(enterEmail);
		getEmail.add(addEmail);
		JPanel show = new JPanel();
		JLabel showEmails = new JLabel("Emails: " + emails.toString().replace("[", "").replace("]", ""));
		show.add(showEmails);
		Object[] layout = {getEmail, show};
		Object[] emailOptions = {"Add Emails", "Edit Email", "Remove Emails", "Done"};
		if (emails.isEmpty()) {
			show.setVisible(false);
			emailOptions = new Object[] {emailOptions[0]};
		}
		int emailInput = JOptionPane.showOptionDialog(null, layout, "Recipients", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, emailOptions, null);
		if (emailInput != emailOptions.length) {
			if (emailInput == JOptionPane.YES_OPTION) {
				if (addEmail.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "Please enter an Email!", "Enter Email", JOptionPane.ERROR_MESSAGE);
				} else {
	        		String[] split = addEmail.getText().split(",");
	        		for (String s : split) {
	        			 if (checkEmail(s.trim())) {
	 			        	if (Collections.frequency(emails, s.trim()) > 0) 
	 							JOptionPane.showMessageDialog(null, "You already added " + s.trim(), "Duplicate Email", JOptionPane.ERROR_MESSAGE);
	 			        	else 
	 			        		emails.add(s.trim());
	 			        } else {
	 						JOptionPane.showMessageDialog(null, s.trim() + " is not an email!", "Enter Email", JOptionPane.ERROR_MESSAGE);
	 			        }
	        		}
				}
			} else if (emailInput == JOptionPane.NO_OPTION) {
				JPanel edit = new JPanel();
				enterEmail = new JLabel("Edit Which Email: ");
				JComboBox<String> editEmails = new JComboBox<String>();
				for (String e : emails)
					editEmails.addItem(e);
				edit.add(enterEmail);
				edit.add(editEmails);
				JPanel newEmail = new JPanel();
				showEmails = new JLabel("Enter new Email: ");
				newEmail.add(showEmails);
				newEmail.add(addEmail);
				layout = new Object[] {edit, newEmail};
				emailOptions = new Object[] {"Done", "Back"};
				emailInput = JOptionPane.showOptionDialog(null, layout, "Edit Email", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, emailOptions, null);
				if (emailInput == JOptionPane.YES_OPTION)
					emails.set(emails.indexOf(editEmails.getSelectedItem()), addEmail.getText());
			} else if (emailInput == JOptionPane.CANCEL_OPTION) {
				JPanel remove = new JPanel();
				enterEmail = new JLabel("Remove Which Email: ");
				String[] emailsArray = new String[emails.size()];
				for (int i = 0; i < emails.size(); i++)
					emailsArray[i] = emails.get(i);
				JList<String> removeEmails = SearchUsers.createJList(emailsArray);
				JScrollPane scroll = SearchUsers.getScroll(removeEmails);
				remove.add(enterEmail);
				remove.add(scroll);
				emailOptions = new Object[] {"Done", "Back"};
				emailInput = JOptionPane.showOptionDialog(null, remove, "Remove Email", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, emailOptions, null);
				if (emailInput == JOptionPane.YES_OPTION) {
					List<String> getRemoveEmails = removeEmails.getSelectedValuesList();
					for (String e : getRemoveEmails)
						emails.remove(emails.indexOf(e));
				}
			} else if (emailInput == JOptionPane.QUESTION_MESSAGE) {
				confirm = JOptionPane.showOptionDialog(null, "Are you sure you wish to continue?", showEmails.getText(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
				if (confirm == JOptionPane.YES_OPTION) {
					sendMail(confirm, answer, user);
					host = "";
					if (emails.isEmpty())
						return;
				}
			} else {
				confirm = JOptionPane.showOptionDialog(null, "Are you sure you wish to cancel?", "Options", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
				if (confirm == JOptionPane.YES_OPTION)
					return;
			}
			addEmails(confirm, answer, user);
		}
	}
	
	/**
	 * Sends emails containing files to downloadable tracks found in SoundCloud. 
	 * @param confirm message to confirm cancellation or continuation
	 * @param answer either yes or no
	 * @param user used to create files containing downloadable tracks from user's playlists
	 * @throws AddressException
	 */
	public static void sendMail(int confirm, String[] answer, String user) throws AddressException {
		if (host == "") {
			host = (String) JOptionPane.showInputDialog(null, "Enter Email Host Domain: ", "Email Host Domain", JOptionPane.DEFAULT_OPTION, null, null, null);
			if (host == null) {
				host = "";
				addEmails(confirm, answer, user);
			} else if (host.isEmpty()) {
				host = "";
				JOptionPane.showMessageDialog(null, "Please enter a Host Email Domain", "Enter Host Domain", JOptionPane.ERROR_MESSAGE);
				sendMail(confirm, answer, user);
			}  
			host = host.toLowerCase();
		}
		if (sender == "") {
			sender = (String) JOptionPane.showInputDialog(null, "Enter Sender Email: ", "Sender Email", JOptionPane.DEFAULT_OPTION, null, null, null);
			if (sender == null) {
				host = ""; sender = "";
				sendMail(confirm, answer, user);
			} else if (sender.isEmpty()) {
				sender = "";
				JOptionPane.showMessageDialog(null, "Please enter a Sender Email", "Enter Sender", JOptionPane.ERROR_MESSAGE);
				sendMail(confirm, answer, user);
			}
		}
		if (checkEmail(sender)) {
			if (password == "") {
				password = (String) JOptionPane.showInputDialog(null, "Enter Sender Password: ", "Sender Password", JOptionPane.DEFAULT_OPTION, null, null, null);
				if (password == null) {
					sender = ""; password = "";
					sendMail(confirm, answer, user);
				} else if (password.isEmpty()) {
					password = "";
					JOptionPane.showMessageDialog(null, "Please enter a password!", "Enter Password", JOptionPane.ERROR_MESSAGE);
					sendMail(confirm, answer, user);
				}
			}
		} else {
			JOptionPane.showMessageDialog(null, sender + " is not an email!", "Enter Sender", JOptionPane.ERROR_MESSAGE);
			sender = "";
			sendMail(confirm, answer, user);
		}
		confirm = JOptionPane.showOptionDialog(null, "Are you sure you wish to continue?", host + ": " + sender, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
		if (confirm == JOptionPane.YES_OPTION) {
				Properties p = new Properties();
				p.put("mail.smtp.host", "smtp." + host + ".com");
				p.put("mail.smtp.port", "587");
				p.put("mail.smtp.auth", "true");
				p.put("mail.smtp.starttls.enable", "true");
				/* p.put("mail.debug", "true"); */
				Authenticator a = new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(sender, password);
					}
				};
				Session session = Session.getInstance(p, a);
				try {
					MimeMessage message = new MimeMessage(session);
					message.setFrom(new InternetAddress(sender)); 
			    for (String e : emails) 
			    	message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(e, false));	
				message.setSubject("Downloadable Tracks");
				BodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setText("Hello,\n\nYou have received tracks in SoundCloud that you can download!\n\nSincerely,\nDennis Qiu");
				Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);
				String[] fileTypes = { "txt", "html" };
				for (String f : fileTypes) {
					String fileName = Selenium.Files.SoundCloud(f, user, false);
					File file = new File(fileName);
					if (file.exists()) {
						messageBodyPart = new MimeBodyPart();
						DataSource source = new FileDataSource(fileName);
						messageBodyPart.setDataHandler(new DataHandler(source));
						messageBodyPart.setFileName(file.getName());
						multipart.addBodyPart(messageBodyPart);
					}
				}
				message.setContent(multipart);
				Transport.send(message);
				emails.clear();
				JOptionPane.showMessageDialog(null, "You have successfully sent your downloadable tracks to your recipients!", "Email Sent!", JOptionPane.INFORMATION_MESSAGE);
			} catch (MessagingException m) {
				m.printStackTrace();
			}
		} else if (confirm == JOptionPane.NO_OPTION) {
			password = "";
			sendMail(confirm, answer, user);
		} else {
			JOptionPane.showMessageDialog(null, "Please choose an option", "Choose Option", JOptionPane.ERROR_MESSAGE);
			sendMail(confirm, answer, user);
		}
	}
	
	/**
	 * Checks if String is an email or not.
	 * @param email to check if email syntax is correct or not
	 * @return true if email is indeed an email, otherwise false.
	 */
	private static boolean checkEmail(String email) {
		Pattern emailRegex = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = emailRegex.matcher(email);
		return matcher.find();
	}
}
