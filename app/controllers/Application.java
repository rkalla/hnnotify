package controllers;

import static common.Constants.FORM_DEFAULT_EMAIL;
import static common.Constants.FORM_DEFAULT_USERNAME;
import models.Author;
import models.User;
import dao.DAO;
import parser.HNParser;
import play.data.validation.Email;
import play.data.validation.Required;
import play.mvc.Controller;

public class Application extends Controller {
	public static void index() {
		renderArgs.put("email", FORM_DEFAULT_EMAIL);
		renderArgs.put("username", FORM_DEFAULT_USERNAME);

		render();
	}

	public static void signup(String email, String username) {
		// Validate the two required fields.
		if (email == null || email.isEmpty()
				|| FORM_DEFAULT_EMAIL.equals(email)
				|| validation.email(email).error != null)
			validation.addError("email", "Please enter a valid email address.");
		if (FORM_DEFAULT_USERNAME.equals(username)
				|| validation.required(username).error != null)
			validation.addError("username", "Please enter your HN username.");
		else {
			Author a = DAO.findAuthor(username);

			// If we know nothing about this user, check HN's site.
			if (a == null && !HNParser.isValidUsername(username))
				validation.addError("username", "The username '" + username
						+ "' does not appear to be a valid HN username.");
		}

		User u = DAO.findUser(username);

		// Make sure they aren't already registered.
		if (u != null)
			validation.addError("username", username
					+ " is already a registered user of HN Notify.");

		// If there were any errors, re-render the page with them.
		if (validation.hasErrors()) {
			if(email == null || email.isEmpty())
				email = FORM_DEFAULT_EMAIL;
			if(username == null || username.isEmpty())
				username = FORM_DEFAULT_USERNAME;
			
			renderArgs.put("email", email);
			renderArgs.put("username", username);

			render("@index");
		}

		// Register the user so we start looking for replies for them.
		u = new User(username, email, System.currentTimeMillis());
		DAO.saveUser(u);
		
		welcome();
	}
	
	public static void welcome() {
		render();
	}
	
	public static void privacy() {
		render();
	}
	
	public static void how() {
		render();
	}
}