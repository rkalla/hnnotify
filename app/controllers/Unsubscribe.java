package controllers;

import static common.Constants.FORM_DEFAULT_EMAIL;
import models.User;
import play.mvc.Controller;
import dao.DAO;

public class Unsubscribe extends Controller {
	public static void index() {
		renderArgs.put("email", FORM_DEFAULT_EMAIL);

		render();
	}

	@SuppressWarnings("static-access")
	public static void unsubscribe(String email) {
		if (email == null || email.isEmpty()
				|| FORM_DEFAULT_EMAIL.equals(email)
				|| validation.email(email).error != null)
			validation.addError("email", "Please enter a valid email address.");

		User u = DAO.findUserByEmail(email);

		// Only check if don't have any errors yet.
		if (!validation.hasErrors() && u == null)
			validation.addError("email", "'" + email
					+ "' is not registered with HN Notify.");

		// If there were any errors, re-render the page with them.
		if (validation.hasErrors()) {
			if (email == null || email.isEmpty())
				email = FORM_DEFAULT_EMAIL;

			renderArgs.put("email", email);

			render("@index");
		}

		// Delete the user.
		DAO.deleteUser(u);
		farewell();
	}

	public static void farewell() {
		render();
	}
}