package util;

import java.util.List;

import models.User;
import dao.DAO;

public class ListUsers {
	public static void main(String[] args) {
		List<User> list = DAO.findAllUsers();

		System.out.printf("Found %d users...\n", list.size());

		for (User u : list)
			System.out.println(u);
	}
}