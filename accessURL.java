import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Class used to run the program
 * 
 * @author arjun
 *
 */
public class accessURL {

	/**
	 * Returns all occurrences of a tag in an HTML document in the form of a
	 * ListIterator<Element>
	 * 
	 * @param html
	 * @param tag
	 * @return - iteratorlist - list of all occurrences of tag passed
	 */
	public static ListIterator<Element> getTagOcurrences(Document html, String tag) {

		Elements taglist = html.getElementsByTag(tag);
		ListIterator<Element> iteratorlist = taglist.listIterator();

		return iteratorlist;
	}

	/**
	 * Method used to display all elements of the ListIterator<Element> object
	 * passed to it containing all the rows of the information table
	 * 
	 * @param list
	 */
	public static void displaymainlist(ListIterator<Element> list) {
		int i = 1;
		while (list.hasNext()) {

			Element temp = list.next();
			if (temp.text().isBlank()) {

				System.out.println(i + ". This section contains non-text content and might have links.");

			} else {

				System.out.print(i + ". ");
				System.out.println(temp.text());
			}

			i++;
		}

	}

	/**
	 * Method that gets the text from all the elements of a ListIterator<Element>
	 * and stores it in an ArrayList<String> that is returned
	 * 
	 * @param list
	 * @return text - ArrayList<String>
	 */
	public static ArrayList<String> formhreflist(ListIterator<Element> list) {

		String[] abc;
		ArrayList<String> text = new ArrayList<String>();
		while (list.hasNext()) {

			String temp = list.next().toString();
			Scanner sc = new Scanner(temp);
			while (sc.hasNext()) {
				temp = sc.next();
				if (temp.contains("href")) {
					abc = temp.split(">");
					text.add(abc[0]);
				}

			}

		}
		return text;

	}


//	public static void findhref(String key, ArrayList<String> hreflist) {
//
//		for (int i = 0; i < hreflist.size(); i++) {
//			if (hreflist.get(i).toLowerCase().contains(key)) {
//
//				String temp = hreflist.get(i);
//				temp = temp.replace('"', '\0');
//				temp = temp.replace("href=", "");
//				temp = "https://en.wikipedia.org  ".trim() + temp.trim();
//				System.out.println(temp);
//			}
//		}
//
//	}

	public static Document parsetoprecisedoc(Document og, String tag, int occurence) {

		occurence = occurence - 1;
		Elements newlist = og.body().getElementsByTag(tag);
		ListIterator<Element> newlistiterator = newlist.listIterator(occurence);
		Element xyz = newlistiterator.next();
		return Jsoup.parse("<html>" + xyz + "</html>");
	}

	public static ArrayList<String> displayhreflist(ArrayList<String> hreflist) {

		int i = 0;
		while (i < hreflist.size()) {
			String temp = hreflist.get(i);
			temp = temp.replace("\"", "");
			temp = temp.replace("href=", "");
			if (temp.contains("cite_note")) {
				hreflist.remove(i);
				continue;
			}
			if (temp.contains("</a")) {
				temp = temp.replaceAll("\\<.*?\\>", "").trim();
				temp = temp.replace(">", "").trim();
			}

			if (temp.contains("/wiki"))
				temp = "https://en.wikipedia.org  ".trim() + temp.trim();
			else
				temp = temp.trim();

			hreflist.remove(i);
			hreflist.add(i, temp);
			System.out.println(temp);
			i++;
		}
		return hreflist;
	}

	public static boolean openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static boolean openWebpage(URL url) {
		try {
			return openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void openlinks(String decision, ArrayList<String> hreflist) throws Exception {

		if (decision.trim().toLowerCase().equals("yes")) {
			int i = 0;
			while (i < hreflist.size()) {
				openWebpage(new URL(hreflist.get(i)));
				i++;
			}
		} else
			return;

	}

	public static void generaltester() throws Exception {

		Document html = null;
		Scanner sc = new Scanner(System.in);
		String temp;
		int x = 999;
		try {
			System.out.println("Enter search query:");
			temp = sc.nextLine().toString().trim();
			System.out.println("Getting information...");
			html = Jsoup.connect("https://en.wikipedia.org/wiki/" + temp).get();
		} catch (Exception e) {

			e.printStackTrace();
		}

		Document introtable = parsetoprecisedoc(html, "table", 1);

		ListIterator<Element> fulltable = getTagOcurrences(introtable, "tr");
		displaymainlist(fulltable);

		while (x != 0) {
			System.out.println("Type the number of the category you would like to explore further: (Type 0 to exit) ");
			x = sc.nextInt();
			if (x == 0)
				break;
			Document row = parsetoprecisedoc(introtable, "tr", x);

			ListIterator<Element> linkiterator = getTagOcurrences(row, "a");
			ArrayList<String> hreflist = formhreflist(linkiterator);
			hreflist = displayhreflist(hreflist);

			if (!hreflist.isEmpty()) {
				System.out.println("Would you like to open the links?(yes/no)");
				String decision = sc.next();
				openlinks(decision, hreflist);
			} else {
				System.out.println("There are no links in this section.");
				continue;
			}

		}
	}

	public static void main(String[] args) {

		try {
			generaltester();
			System.out.println("The program has now ended.");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
