package util;

import java.util.List;

import dao.DAO;

import models.Comment;

public class ListComments {
	public static void main(String[] args) {
		List<Comment> list = DAO.findAllComments();
		
		System.out.printf("Found %d comments...\n", list.size());
		
		for(Comment c : list)
			System.out.println(c);
	}
}