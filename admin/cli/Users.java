package cli;

import java.util.List;

import models.User;
import dao.DAO;

public class Users {
	public static void main(String[] args) {
		if(args.length == 0) {
			System.out.println("Usage:");
			System.out.println("\t-list Show all users.");
			System.out.println("\t-findByEmail Find a user by email.");
			System.out.println("\t-findByUsername find a user by username.");
			System.out.println("\t-remove Delete a user from the DB.");
		}
		
		if ("-list".equalsIgnoreCase(args[0])) {
			List<User> users = DAO.findAllUsers();

			for (User u : users)
				System.out.println(u);

			System.out.println("------------------");
			System.out.println("Total Users: " + users.size());
		} else if ("-findByEmail".equalsIgnoreCase(args[0])) {
			String email = args[1];

			if (email == null || email.isEmpty()) {
				System.out.println("Email arg was empty.");
				System.exit(1);
			}

			User u = DAO.findUserByEmail(email);
			System.out.println("User: " + u);
		} else if ("-findByUsername".equalsIgnoreCase(args[0])) {
			String name = args[1];

			if (name == null || name.isEmpty()) {
				System.out.println("Username arg was empty.");
				System.exit(1);
			}

			User u = DAO.findUserByEmail(name);
			System.out.println("User: " + u);
		} else if ("-remove".equalsIgnoreCase(args[0])) {
			String name = args[1];

			if (name == null || name.isEmpty()) {
				System.out.println("Username arg was empty.");
				System.exit(1);
			}

			User u = DAO.findUserByUsername(name);
			System.out.println("Found user: " + u);

			DAO.deleteUser(u);
			System.out.println("\tUser successfully deleted.");
		}
	}
}