package edu.mtholyoke.cs341bd.bookz;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley
 */
public class BookzServer extends AbstractHandler {
  Server jettyServer;
  HTMLView view;
  Model model;

  public BookzServer(String baseURL, int port) throws IOException {
    view = new HTMLView(baseURL);
    jettyServer = new Server(port);
    model = new Model();

    // We create a ContextHandler, since it will catch requests for us under
    // a specific path.
    // This is so that we can delegate to Jetty's default ResourceHandler to
    // serve static files, e.g. CSS & images.
    ContextHandler staticCtx = new ContextHandler();
    staticCtx.setContextPath("/static");
    ResourceHandler resources = new ResourceHandler();
    resources.setBaseResource(Resource.newResource("static/"));
    staticCtx.setHandler(resources);

    // This context handler just points to the "handle" method of this
    // class.
    ContextHandler defaultCtx = new ContextHandler();
    defaultCtx.setContextPath("/");
    defaultCtx.setHandler(this);

    // Tell Jetty to use these handlers in the following order:
    ContextHandlerCollection collection = new ContextHandlerCollection();
    collection.addHandler(staticCtx);
    collection.addHandler(defaultCtx);
    jettyServer.setHandler(collection);
  }

  /**
   * Once everything is set up in the constructor, actually start the server
   * here:
   *
   * @throws Exception if something goes wrong.
   */
  public void run() throws Exception {
    jettyServer.start();
    jettyServer.join(); // wait for it to finish here! We're using threads
    // behind the scenes; so this keeps the main thread around until
    // something can happen!
  }

  /**
   * The main callback from Jetty.
   *
   * @param resource what is the user asking for from the server?
   * @param jettyReq the same object as the next argument, req, just cast to a
   *                 jetty-specific class (we don't need it).
   * @param req      http request object -- has information from the user.
   * @param resp     http response object -- where we respond to the user.
   * @throws IOException      -- If the user hangs up on us while we're
   *                          writing back or
   *                          gave us a half-request.
   * @throws ServletException -- If we ask for something that's not there,
   *                          this might
   *                          happen.
   */
  @Override
  public void handle(String resource, Request jettyReq, HttpServletRequest
      req, HttpServletResponse resp)
      throws IOException, ServletException {
    System.out.println(jettyReq);

    String method = req.getMethod();
    String path = req.getPathInfo();

    System.out.println("method: " + method);
    System.out.println("path: " + path);

    if ("GET".equals(method)) {
      if ("/robots.txt".equals(path)) {
        // We're returning a fake file? Here's why: http://www.robotstxt.org/
        resp.setContentType("text/plain");
        try (PrintWriter txt = resp.getWriter()) {
          txt.println("User-Agent: *");
          txt.println("Disallow: /");
        }
        return;
      }
      if ("/search".equals(path)) {
        handleSearch(req, resp, path);
      }

      String titleCmd = Util.getAfterIfStartsWith("/title/", path);
      if (titleCmd != null) {
        char firstChar = titleCmd.charAt(0);
        int pageNum = Integer.parseInt(titleCmd.substring(2));

        if (this.model.getBooksStartingWith(firstChar) != null) {
          view.showBookCollection(this.model.getBooksStartingWith(firstChar,
              pageNum), this.model, pageNum, model
                  .getNumPagesStartingWithChar(firstChar),
              "/title/" + Character.toString(firstChar), resp);
        } else {
          redirectPageForNoSearchResults(resp);
        }
      }

      String authorCmd = Util.getAfterIfStartsWith("/author/", path);
      if (authorCmd != null) {
        char firstChar = authorCmd.charAt(0);
        int pageNum = Integer.parseInt(authorCmd.substring(2));
        List<GutenbergBook> booksAuthor = this.model
            .getBooksAuthorStartingWith(firstChar);
        if (booksAuthor != null) {
          view.showBookCollection(this.model.getPage(booksAuthor, pageNum),
              this.model, pageNum, model.getNumPages(booksAuthor),
              "/author/" + Character.toString(firstChar), resp);
        }
      }

      String authorsCmd = Util.getAfterIfStartsWith("/authors/", path);
      if (authorsCmd != null) {
        int pageNum = Integer.parseInt(authorsCmd);
        List<Author> authors = this.model.getAuthors();
        if (authors != null) {
          view.showAuthors(this.model.getPage(authors, pageNum), pageNum,
              model.getNumPages(authors), this.model, resp);
        }
      }

      String authorPageCmd = Util.getAfterIfStartsWith("/authorPage/", path);
      if (authorPageCmd != null) {
        int pageInd = authorPageCmd.indexOf("/");
        int pageNum = Integer.parseInt(authorPageCmd.substring(pageInd + 1));
        authorPageCmd = authorPageCmd.substring(0, pageInd);
        String[] authorArray = authorPageCmd.split("&");
        // puts spaces in front of upper case characters
        String lastName = authorArray[0].replaceAll("\\+", " ");
        String firstName = "";
        if (authorArray.length > 1) {
          firstName = authorArray[1].replaceAll("\\+", " ");
        }
        Author author = new Author(firstName, lastName, 0, 0);
        List<GutenbergBook> books = this.model.getBooksByAuthor(author);
        view.showBookCollection(this.model.getPage(books, pageNum), this.model,
            pageNum, model.getNumPages(books),
            "/authorPage/" + authorPageCmd, resp);
      }

      // Check for startsWith and substring
      String bookId = Util.getAfterIfStartsWith("/book/", path);
      if (bookId != null) {
        view.showBookPage(model, this.model.getBook(bookId), resp);
      }

      String flagId = Util.getAfterIfStartsWith("/flag/", path);
      if (flagId != null) {
        view.showFlagPage(this.model.getBook("etext" + flagId), resp);
      }

      String likeId = Util.getAfterIfStartsWith("/like/", path);
      if (likeId != null) {
        if (req.getCookies() != null) {
          for (Cookie cookie : req.getCookies()) {
            if (cookie.getName().equals("user")) {
              view.showLikePage(this.model.getBook("etext" + likeId), resp,
                  model, cookie.getValue());
              continue;
            }
          }
        }
        // if we've reached here, send to login page
        redirectToLogin(req, resp);
      }

      String reviewPage = Util.getAfterIfStartsWith("/flagged", path);
      if (reviewPage != null) {
        view.showReviewPage(model.getFlagged(), resp);
      }

      String likedBookId = Util.getAfterIfStartsWith("/user/likes/", path);
      if (likedBookId != null) {
        int pageNum = Integer.parseInt(likedBookId);
        List<GutenbergBook> allLiked = model.getAllLikedBooks();
        view.showBookCollection(allLiked, model, pageNum, model.getNumPages
            (allLiked), "/user/likes", resp);
      }

      String userLikeId = Util.getAfterIfStartsWith("/userLikes/", path);
      if (userLikeId != null) {
        int indSlash = userLikeId.indexOf("/");
        int pageNum = Integer.parseInt(userLikeId.substring(indSlash + 1));
        String user = userLikeId.substring(0, indSlash);
        List<GutenbergBook> booksLiked = this.model.getUserLiked(user);
        if (booksLiked != null) {
          view.showBookCollection(this.model.getPage(booksLiked, pageNum),
              this.model, pageNum, model.getNumPages(booksLiked),
              "/userLikes/" + user, resp);
        }
      }

      if ("/login".equals(path)) {
        view.showLoginPage(resp);
      }

      if ("/signOut".equals(path)) {
        redirectToLogin(req, resp);
      }

      // Front page!
      if ("/front".equals(path) || "/".equals(path)) {
        boolean isLoggedIn = false;
        if (req.getCookies() != null) {
          for (Cookie cookie : req.getCookies()) {
            if (cookie.getName().equals("user")) {
              isLoggedIn = true;
              continue;
            }
          }
        }
        view.showFrontPage(this.model, resp, isLoggedIn);
        return;
      }

    } else if ("POST".equals(method)) {
      if (path.contains("/submitFlag")) {
        handleForm(req, resp);
        return;
      } else if (path.contains("/submitLike")) {
        handleLike(req, resp);
        return;
      } else if (path.contains("/submitLogin")) {
        handleLogin(req, resp);
      }
    }
  }

  private void handleLogin(HttpServletRequest req,
                           HttpServletResponse resp)
      throws IOException {
    Map<String, String[]> parameterMap = req.getParameterMap();

    // if for some reason, we have multiple "message" fields in our form,
    // just put a space between them, see Util.join.
    // Note that message comes from the name="message" parameter in our
    // <input> elements on our form.
    String user = Util.join(parameterMap.get("user"));

    if (user != null) {
      // Good, got new message from form.
      resp.setStatus(HttpServletResponse.SC_ACCEPTED);

      resp.addCookie(new Cookie("user", user));

      submitPage(resp);

      return;
    }

    // user submitted something weird.
    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad user.");
  }

  /**
   * When a user presses the "like" button, the likes will be calculated
   *
   * @param req  -- we'll grab the form parameters from here.
   * @param resp -- where to write their "success" page.
   * @throws IOException again, real life happens.
   */
  private void handleLike(HttpServletRequest req,
                          HttpServletResponse resp)
      throws IOException {
    Map<String, String[]> parameterMap = req.getParameterMap();

    // if for some reason, we have multiple "message" fields in our form,
    // just put a space between them, see Util.join.
    // Note that message comes from the name="message" parameter in our
    // <input> elements on our form.
    String user = Util.join(parameterMap.get("user"));
    String id = Util.join(parameterMap.get("id"));

    if (user != null && id != null) {
      // Good, got new message from form.
      resp.setStatus(HttpServletResponse.SC_ACCEPTED);

      model.addLike(id, user);

      submitPage(resp);

      return;
    }

    // user submitted something weird.
    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad like.");
  }

  /**
   * When a user submits (enter key) or pressed the "Submit" button, we'll
   * get their request in here. This is called explicitly from handle, above.
   *
   * @param req  -- we'll grab the form parameters from here.
   * @param resp -- where to write their "success" page.
   * @throws IOException again, real life happens.
   */
  private void handleForm(HttpServletRequest req,
                          HttpServletResponse resp)
      throws IOException {
    Map<String, String[]> parameterMap = req.getParameterMap();

    // if for some reason, we have multiple "message" fields in our form,
    // just put a space between them, see Util.join.
    // Note that message comes from the name="message" parameter in our
    // <input> elements on our form.
    String problem = Util.join(parameterMap.get("problem"));
    String id = Util.join(parameterMap.get("id"));

    if (problem != null && id != null) {
      // Good, got new message from form.
      resp.setStatus(HttpServletResponse.SC_ACCEPTED);

      model.addFlagged(id, problem);

      submitPage(resp);

      return;
    }

    // user submitted something weird.
    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad flag.");
  }

  private void submitPage(HttpServletResponse resp) {
    // Respond!
    try (PrintWriter html = resp.getWriter()) {
      view.printPageStart(html, "Bookz: Thanks for your submission!", "/front");
      // Print actual redirect directive:
      html.println("<meta http-equiv=\"refresh\" content=\"3; url=front \">");

      // Thank you, link.
      html.println("<div class=\"body\">");
      html.println("<div class=\"thanks\">");
      html.println("<p>Thanks for your submission!</p>");
      html.println("<a href=\"front\">Back to the front page...</a> " +
          "(automatically redirect in 3 seconds).");
      html.println("</div>");
      html.println("</div>");

      view.printPageEnd(html);

    } catch (IOException ignored) {
      // Don't consider a browser that stops listening to us after
      // submitting a form to be an error.
    }
  }

  /**
   * When a user searches (enter key), we'll get their request in here.
   * This is called explicitly from handle, above.
   *
   * @param req
   * @param resp
   * @param path
   * @throws IOException
   */
  private void handleSearch(HttpServletRequest req, HttpServletResponse resp,
                            String path)
      throws IOException {
    // search and display book(s)\
    String book = req.getParameter("searchBook");


    if (book != null) {
      int pageNum = 1;
      if (book.contains("/")) {
        int ind = book.indexOf("/");
        pageNum = Integer.parseInt(book.substring(ind + 1));
        book = book.substring(0, ind);
      }

      if (this.model.searchBooks(book, pageNum).size() != 0) {

        int numBooks = this.model.searchBooks().size();
        String title = "We found " + numBooks + " results!";
        view.showSearchResults(this.model.searchBooks(book, pageNum), model,
            pageNum, this.model.getNumPagesForSearch(), book, title, resp);

      }

      // if no books redirect
      else {
        redirectPageForNoSearchResults(resp);
      }
    }
  }

  private void redirectPageForNoSearchResults(HttpServletResponse resp) {
    // Respond!
    try (PrintWriter html = resp.getWriter()) {
      view.printPageStart(html, "NO RESULTS", "/front");
      // Print actual redirect directive:
      html.println("<meta http-equiv=\"refresh\" content=\"3; url=front \">");

      // Thank you, link.
      html.println("<div class=\"body\">");
      html.println("<div class=\"thanks\">");
      html.println("<p>Your search query has no results. Please accept this " +
          "apology instead</p>");
      html.println("<a href=\"front\">Back to the front page...</a> " +
          "(automatically redirect in 3 seconds).");
      html.println("</div>");
      html.println("</div>");

      view.printPageEnd(html);

    } catch (IOException ignored) {
      // Don't consider a browser that stops listening to us after
      // submitting a form to be an error.
    }
  }

  /**
   * Redirects user to login page.
   *
   * @param resp
   */
  private void redirectToLogin(HttpServletRequest req, HttpServletResponse
      resp) {
    if (req.getCookies() != null) {
      for (Cookie cookie : req.getCookies()) {
        if (cookie.getName().equals("user")) {
          cookie.setValue("");
          cookie.setPath("/");
          cookie.setMaxAge(0);
          resp.addCookie(cookie);
        }
      }
    }

    try (PrintWriter html = resp.getWriter()) {
      view.printPageStart(html, "Please Log In", "/login");
      // Print actual redirect directive:
      html.println("<meta http-equiv=\"refresh\" content=\"2; url=login \">");

      view.printPageEnd(html);

    } catch (IOException ignored) {
      // Don't consider a browser that stops listening to us after
      // submitting a form to be an error.
    }
  }
}
