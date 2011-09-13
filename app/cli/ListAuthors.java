package cli;

import java.util.List;

import dao.DAO;

import models.Author;

public class ListAuthors {
	public static void main(String[] args) {
		List<Author> list = DAO.findAllAuthors();

		System.out.printf("Found %d authors...\n", list.size());

		for (Author a : list)
			System.out.println(a);
	}
}