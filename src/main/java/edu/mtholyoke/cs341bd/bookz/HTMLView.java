package edu.mtholyoke.cs341bd.bookz;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class HTMLView {

  private String metaURL;

  public HTMLView(String baseURL) {
    this.metaURL = "<base href=\"" + baseURL + "\">";
  }

  /**
   * HTML top boilerplate; put in a function so that I can use it for all the
   * pages I come up with.
   *
   * @param html  where to write to; get this from the HTTP response.
   * @param title the title of the page, since that goes in the header.
   */
  void printPageStart(PrintWriter html, String title) {
    html.println("<!DOCTYPE html>"); // HTML5
    html.println("<html>");
    html.println("  <head>");
    html.println("    <title>" + title + "</title>");
    html.println("    " + metaURL);
    html.println("    <link type=\"text/css\" rel=\"stylesheet\" href=\"" +
        getStaticURL("bookz.css") + "\">");
    html.println("  </head>");
    html.println("  <body>");
    html.println("  <a href='/front'><h1 class=\"logo\">" + title +
        "</h1></a>");
  }

  public String getStaticURL(String resource) {
    return "static/" + resource;
  }

  /**
   * HTML bottom boilerplate; close all the tags we open in
   * printPageStart.
   *
   * @param html where to write to; get this from the HTTP response.
   */
  void printPageEnd(PrintWriter html) {
    html.println("  </body>");
    html.println("</html>");
  }

  void showFrontPage(Model model, HttpServletResponse resp) throws IOException {
    try (PrintWriter html = resp.getWriter()) {
      printPageStart(html, "Bookz");


      html.println("<div class=\"form\">");
      html.println("	<form action=\"search\" method=\"SEARCH\">");
      html.println("		<label>Search: "
          + "<input type=\"text\" name=\"searchBook\" />"
          + "</label>");
      html.println("	<form>");
      html.println("</div>");

      html.println("<h3>Browse books</h3>");

      html.println("<h5>By Title</h5>");
      for (char letter = 'A'; letter <= 'Z'; letter++) {
        html.println("<a href='/title/" + letter + "/1'>" + letter + "</a> ");
      }
      for (char letter = '0'; letter <= '9'; letter++) {
        html.println("<a href='/title/" + letter + "/1'>" + letter + "</a> ");
      }
      html.println("<h5>By Author (Last Name)</h5>");
      for (char letter = 'A'; letter <= 'Z'; letter++) {
        html.println("<a href='/author/" + letter + "/1'>" + letter + "</a> ");
      }
      html.println("<br><a href='/authors/1'>List of Authors</a>");

      // get 5 random books:
      html.println("<h3>Check out these random books</h3>");
      List<GutenbergBook> randomBooks = model.getRandomBooks(5);
      for (GutenbergBook randomBook : randomBooks) {
        printBookHTML(html, randomBook);
      }
      html.println("<a href='/flagged'>Review Flags</a>");
      printPageEnd(html);
    }
  }

  public void showBookPage(GutenbergBook book, HttpServletResponse resp)
      throws IOException {
    try (PrintWriter html = resp.getWriter()) {
      printPageStart(html, "Bookz");
      printBookHTML(html, book);
      printPageEnd(html);
    }
  }

  /**
   * Displays the flagged books and the problems.
   *
   * @param problems
   * @param resp
   * @throws IOException
   */
  public void showReviewPage(Map<GutenbergBook,
      String> problems, HttpServletResponse resp)
      throws IOException {
    try (PrintWriter html = resp.getWriter()) {
      printPageStart(html, "Flagz");
      for (GutenbergBook book : problems.keySet()) {
        printFlagHTML(html, book, problems.get(book));
      }
      printPageEnd(html);
    }
  }

  /**
   * Prints the flagged book with the problem.
   *
   * @param html
   * @param book
   * @param problem
   */
  private void printFlagHTML(PrintWriter html, GutenbergBook book, String
      problem) {
    html.println("<div class='book'>");
    html.println("<a class='none' href='/book/" + book.id + "'>");
    html.println("<div class='title'>" + book.title + "</div>");
    html.println("<a>" + problem + "</a>");
    html.println("</a>");
    html.println("</div>");
  }

  private void printBookHTML(PrintWriter html, GutenbergBook book) {
    html.println("<div class='book'>");
    html.println("<a class='none' href='/book/" + book.id + "'>");
    html.println
        ("<a class='flag' href='/flag/" + book.getBookNumber() + "'>Flag</a>");
    html.println("<div class='title'>" + book.title + "</div>");
    if (book.creator != null) {
      html.println("<div class='creator'>" + book.creator + "</div>");
    }
    html.println("<a href='" + book.getGutenbergURL() + "'>On Project " +
        "Gutenberg</a>");
    html.println("</a>");
    html.println("</div>");
  }

  /**
   * Prints the page numbers at the bottom of the page.
   *
   * @param html
   * @param current
   * @param end
   * @param query
   */
  private void printPages(PrintWriter html, int current, int end, String
      query) {
    for (int i = Math.max(current - 3, 1); i <= Math.min(end, current + 3);
         i++) {
      if (i == current) {
        html.println("<a>Current </a>");
      } else {
        html.println("<a href='" + query + "/" + i + "'>" + i + " </a>" +
            " ");
      }
    }
  }

  public void showBookCollection(List<GutenbergBook> theBooks, int
      currentPage, int numPages, String query, HttpServletResponse resp)
      throws IOException {
    try (PrintWriter html = resp.getWriter()) {
      printPageStart(html, "Bookz");
      if (theBooks != null) {
        for (int i = 0; i < Math.min(20, theBooks.size()); i++) {
          printBookHTML(html, theBooks.get(i));
        }
      }

      printPages(html, currentPage, numPages, query);
      printPageEnd(html);
    }
  }

  public void showAuthors(List<Author> authors, int currentPage, int
      numPages, Model model, HttpServletResponse resp) throws IOException {
    try (PrintWriter html = resp.getWriter()) {
      printPageStart(html, "Bookz");
      if (authors != null) {
        for (Author author : authors) {
          html.println
              ("<a href='/authorPage/" + author.getPlusString() + "/1'>" +
              author.toString() + "</a> (" + model.getBooksByAuthor(author)
                  .size() + ")<br>");
        }
      }

      printPages(html, currentPage, numPages, "/authors");
      printPageEnd(html);
    }
  }


  public void showSearchResults(List<GutenbergBook> theBooks, int
      currentPage, int numPages, String query, String title,
                                HttpServletResponse resp)
      throws IOException {
    try (PrintWriter html = resp.getWriter()) {
      printPageStart(html, title);
      if (theBooks != null) {
        for (int i = 0; i < Math.min(20, theBooks.size()); i++) {
          printBookHTML(html, theBooks.get(i));
        }
      }

      printPages(html, currentPage, numPages, "/search?searchBook=" + query);
      printPageEnd(html);
    }
  }

  public void showFlagPage(GutenbergBook book, HttpServletResponse
      resp) throws IOException {
    try (PrintWriter html = resp.getWriter()) {
      printPageStart(html, "Bookz");
      if (book != null) {
        html.println("<form action='submitFlag/" + book.getBookNumber() + "' " +
            "method='POST'>");
        html.println("Problem: <br>");
        html.println("<input type='hidden' name='id' value='etext" + book
            .getBookNumber() + "'/>");
        html.println("<input type=\"text\" name=\"problem\">");
        html.println("<input type=\"submit\" value=\"Submit\">");
        html.println("</form>");
      }
      printPageEnd(html);
    }
  }
}
