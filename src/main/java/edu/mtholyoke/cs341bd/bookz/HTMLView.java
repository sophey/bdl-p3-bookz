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
   * @param url
   */
  void printPageStart(PrintWriter html, String title, String url) {
    html.println("<!DOCTYPE html>"); // HTML5
    html.println("<html>");
    html.println("  <head>");
    html.println("    <title>" + title + "</title>");
    html.println("    " + metaURL);
    html.println("    <link type=\"text/css\" rel=\"stylesheet\" href=\"" +
        getStaticURL("bookz.css") + "\">");
    html.println("  </head>");
    html.println("  <body>");
    html.println("  <a href='" + url + "'><h1 class=\"logo\">" + title +
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

  void showFrontPage(Model model, HttpServletResponse resp, boolean loggedIn)
      throws IOException {
    try (PrintWriter html = resp.getWriter()) {
      printPageStart(html, "Bookz", "/front");
      if (loggedIn)
        html.println("<div id='right'><a href='/signOut'>Sign Out</a></div>");
      else
        html.println("<div id='right'><a href='/login'>Log In</a></div>");

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
      html.println("<br><a href='/user/likes/1'>Books Users Like</a>");

      // get 5 random books:
      html.println("<h3>Check out these random books</h3>");
      List<GutenbergBook> randomBooks = model.getRandomBooks(5);
      for (GutenbergBook randomBook : randomBooks) {
        printBookHTML(html, randomBook, model);
      }

      // get 5 recently liked books:
      html.println("<h3>Check out these recently liked books</h3>");
      List<GutenbergBook> likedBooks = model.getRecentlyLiked();
      for (GutenbergBook likedBook : likedBooks) {
        printBookHTML(html, likedBook, model);
      }
      html.println("<a href='/flagged'>Review Flags</a>");
      printPageEnd(html);
    }
  }

  public void showBookPage(Model model, GutenbergBook book,
                           HttpServletResponse resp)
      throws IOException {
    try (PrintWriter html = resp.getWriter()) {
      printPageStart(html, "Bookz", "/front");
      printBookHTML(html, book, model);
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
      printPageStart(html, "Flagz", "/front");
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

  private void printBookHTML(PrintWriter html, GutenbergBook book, Model
      model) {
    html.println("<div class='book'>");
    html.println("<a class='none' href='/book/" + book.id + "'>");
    html.println
        ("<div id='right'><a href='/flag/" + book.getBookNumber() + "'>Flag" +
            "</a><br>");
    html.println
        ("<a href='/like/" + book.getBookNumber() + "'>" + model
            .getLiked(book.id).size() + " Like(s)</a></div>");
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

  public void showBookCollection(List<GutenbergBook> theBooks, Model model, int
      currentPage, int numPages, String query, HttpServletResponse resp)
      throws IOException {
    try (PrintWriter html = resp.getWriter()) {
      printPageStart(html, "Bookz", "/front");
      if (theBooks != null) {
        for (int i = 0; i < Math.min(20, theBooks.size()); i++) {
          printBookHTML(html, theBooks.get(i), model);
        }
      }

      printPages(html, currentPage, numPages, query);
      printPageEnd(html);
    }
  }

  public void showAuthors(List<Author> authors, int currentPage, int
      numPages, Model model, HttpServletResponse resp) throws IOException {
    try (PrintWriter html = resp.getWriter()) {
      printPageStart(html, "Bookz", "/front");
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


  public void showSearchResults(List<GutenbergBook> theBooks, Model model, int
      currentPage, int numPages, String query, String title,
                                HttpServletResponse resp)
      throws IOException {
    try (PrintWriter html = resp.getWriter()) {
      printPageStart(html, title, "/front");
      if (theBooks != null) {
        for (int i = 0; i < Math.min(20, theBooks.size()); i++) {
          printBookHTML(html, theBooks.get(i), model);
        }
      }

      printPages(html, currentPage, numPages, "/search?searchBook=" + query);
      printPageEnd(html);
    }
  }

  /**
   * Shows the page to submit a flag.
   *
   * @param book
   * @param resp
   * @throws IOException
   */
  public void showFlagPage(GutenbergBook book, HttpServletResponse
      resp) throws IOException {
    try (PrintWriter html = resp.getWriter()) {
      printPageStart(html, "Bookz", "/front");
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

  /**
   * Shows the page to submit a like.
   *
   * @param book
   * @param resp
   * @param model
   * @throws IOException
   */
  public void showLikePage(GutenbergBook book, HttpServletResponse
      resp, Model model, String user) throws IOException {
    try (PrintWriter html = resp.getWriter()) {
      printPageStart(html, "Bookz", "/front");
      if (book != null) {
        html.println("<form action='submitLike/" + book.getBookNumber() + "' " +
            "method='POST'>");
        html.println("<input type='hidden' name='id' value='etext" + book
            .getBookNumber() + "'/>");
        html.println("<input type='hidden' name='user' value='" + user + "'/>");
        html.println("<input type=\"submit\" value=\"Like!\">");
        html.println("</form>");

        html.println("<br>Likers:");
        for (String u : model.getLiked(book.id)) {
          html.println("<br><a href='/userLikes/" + u + "/1'>" + u +
              "</a>");
        }
      }
      printPageEnd(html);
    }
  }

  /**
   * Login page
   *
   * @param resp
   * @throws IOException
   */
  public void showLoginPage(HttpServletResponse resp) throws IOException {
    try (PrintWriter html = resp.getWriter()) {
      printPageStart(html, "Bookz", "/front");
      html.println("<form action='submitLogin'" + "method='POST'>");
      html.println("User: ");
      html.println("<input type=\"text\" name=\"user\"><br>");
      html.println("<input type=\"submit\" value=\"Log In!\">");
      html.println("</form>");
      printPageEnd(html);
    }
  }

}
