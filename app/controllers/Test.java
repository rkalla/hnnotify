package controllers;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import parser.HNParser;

public class Test {
	public static void main(String[] args) throws IOException {
		System.out.println("comhead username: " + HNParser.parseCommentUsername(2985549));
		System.out.println("subtext username: " + HNParser.parseCommentUsername(2985276));
	}

	private static void loadSubtext(File file) throws IOException {
		Document doc = Jsoup.parse(file, "UTF-8");
		// Document doc = Jsoup.parse(new URL(
		// "http://news.ycombinator.com/newcomments"), 30000);

		Elements block = doc.select("td.subtext");

		// SCENARIO #1
		if (block != null) {
			// Make sure we have the subelements we need.
			if (block.size() > 0) {
				// Grab first 'subtext' and get its links.
				Elements links = block.get(0).select("a[href]");

				// First link is the username.
				if (links.size() > 0)
					System.out.println("Username: " + links.get(0).text());
			}
		}
	}

	private static void loadComhead(File file) throws IOException {
		Document doc = Jsoup.parse(file, "UTF-8");
		// Document doc = Jsoup.parse(new URL(
		// "http://news.ycombinator.com/newcomments"), 30000);

		Elements elements = doc.select("span.comhead");
		Elements comments = doc.select("span.comment");
		Elements more = doc.select("td.title");

		System.out.println("Found " + elements.size() + " elements.");

		for (int i = 0; i < elements.size(); i++) {
			Element e = elements.get(i);
			System.out.println(e.html());

			printComhead(e);
			System.out.println("\tComment: " + comments.get(i).html());
		}

		System.out.println("MORE: " + more.get(0).child(0).attr("href"));
	}

	private static void printComhead(Element comhead) {
		// Get all the valid a-href links from the comhead section.
		Elements links = comhead.select("a[href]");

		for (int i = 0, size = links.size(); i < size; i++) {
			Element link = links.get(i);
			System.out.println("\t" + link.attr("href") + " - '" + link.text()
					+ "'");
		}
	}
}