package dao;

import java.util.List;

import models.Comment;
import models.Comment.State;

public class SetDeliveredStateComments {
	public static void main(String[] args) {
		List<Comment> list = DAO.findAllComments();
		
		System.out.printf("Found %d comments...\n", list.size());
		
		for(Comment c : list) {
			c.state = State.DELIVERED;
			DAO.updateComment(c);
		}
	}
}