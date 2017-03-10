package edu.mtholyoke.cs341bd.bookz;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

public class Model {
  Map<String, GutenbergBook> library;
  Map<Character, List<GutenbergBook>> booksStartingWith;
  Map<Character, List<GutenbergBook>> booksAuthorStartingWith;
  Map<Author, List<GutenbergBook>> booksByAuthor;

  Map<GutenbergBook, String> flagged; // id to problem

  List<GutenbergBook> booksWithSearch;
  List<Author> authors;

  public final int NUM_PER_PAGE = 20;

  public Model() throws IOException {
    // start with an empty hash-map; tell it it's going to be big in advance:
    library = new HashMap<>(40000);
    // do the hard work:
    DataImport.loadJSONBooks(library);
    // store books starting with different characters in map
    storeBooks();

    // set flagged to empty
    flagged = new HashMap<>();
  }

  public void storeBooks() {
    booksStartingWith = new HashMap<>();
    booksAuthorStartingWith = new HashMap<>();
    booksByAuthor = new HashMap<>();
    for (GutenbergBook book : library.values()) {
      char firstTitle = Character.toUpperCase(book.title.charAt(0));
      if (!booksStartingWith.containsKey(firstTitle)) {
        booksStartingWith.put(firstTitle, new ArrayList<>(10000));
      }
      booksStartingWith.get(firstTitle).add(book);

      // if creator field is empty, extract from title
      String author = book.creator;
      if (author == null) {
        int byIndex = book.title.indexOf("by");
        if (byIndex < 0) {
          continue;
        }
        author = book.title.substring(book.title.indexOf("by") + 3);
      }
      int commaIndex = author.indexOf(", ");
      String lastName;
      String firstName = "";
      if (commaIndex < 0) {
        lastName = author;
      } else {
        lastName = author.substring(0, commaIndex);
        firstName = author.substring(commaIndex + 2);
      }
      commaIndex = firstName.indexOf(", ");
      int birth = 0, death = 0;
      if (commaIndex >= 0) {
        String yStr = firstName.substring(commaIndex + 2);
        firstName = firstName.substring(0, commaIndex);
        String[] years = yStr.split("-");
        if (StringUtils.isNumeric(years[0]))
          birth = Integer.parseInt(years[0]);
        if (years.length > 1 && StringUtils.isNumeric(years[1]))
          death = Integer.parseInt(years[1]);
      }

      Author authorFormatted = new Author(firstName, lastName, birth, death);
      char firstAuthor = Character.toUpperCase(lastName.charAt(0));
      if (!booksAuthorStartingWith.containsKey(firstAuthor)) {
        booksAuthorStartingWith.put(firstAuthor, new ArrayList<>(10000));
      }
      booksAuthorStartingWith.get(firstAuthor).add(book);

      if (!booksByAuthor.containsKey(authorFormatted)) {
        booksByAuthor.put(authorFormatted, new ArrayList<>(100));
      }
      booksByAuthor.get(authorFormatted).add(book);
    }

    authors = new ArrayList<>(booksByAuthor.keySet());
    Collections.sort(authors);
  }

  public GutenbergBook getBook(String id) {
    return library.get(id);
  }

  public List<GutenbergBook> getBooksStartingWith(char firstChar) {
    return booksStartingWith.get(firstChar);
  }

  public List<GutenbergBook> getBooksAuthorStartingWith(char firstChar) {
    return booksAuthorStartingWith.get(firstChar);
  }

  public List<GutenbergBook> getBooksByAuthor(Author author) {
    return booksByAuthor.get(author);
  }

  public List<Author> getAuthors() {
    return authors;
  }

  /**
   * Gets the number of pages in a list of books starting with character.
   *
   * @param c
   * @return
   */
  public int getNumPagesStartingWithChar(char c) {
    return getNumPages(getBooksStartingWith(c));
  }

  /**
   * General get number of pages given a list of books.
   *
   * @param books
   * @return
   */
  public <T> int getNumPages(List<T> books) {
    if (books == null)
      return 0;
    return (int) Math.ceil((double) books.size() / NUM_PER_PAGE);
  }

  /**
   * Gets the books starting with a character on a certain page.
   *
   * @param firstChar
   * @param page
   * @return
   */
  public List<GutenbergBook> getBooksStartingWith(char firstChar, int page) {
    return getPage(getBooksStartingWith(firstChar), page);
  }

  /**
   * More general way to get the page. Takes in a list of books and a page
   * number and generates the list of books on that page.
   *
   * @param books list of books to pull from
   * @param page  page to pull
   * @return page
   */
  public <T> List<T> getPage(List<T> books, int page) {
    int startIndex = (page - 1) * NUM_PER_PAGE;
    if (books == null)
      return null;
    if (startIndex >= books.size()) {
      startIndex = books.size() - books.size() % NUM_PER_PAGE;
    }
    int endIndex = startIndex + NUM_PER_PAGE;
    if (endIndex >= books.size()) {
      endIndex = books.size();
    }

    return books.subList(startIndex, endIndex);
  }

  public List<GutenbergBook> getRandomBooks(int count) {
    return ReservoirSampler.take(count, library.values());
  }


  public Map<GutenbergBook, String> getFlagged() {
    return flagged;
  }

  public void addFlagged(String id, String problem) {
    flagged.put(getBook(id), problem);
  }

  /**
   * Gets the books that have the parameter in its title.
   *
   * @param book
   * @param page
   * @return
   */
  public List<GutenbergBook> searchBooks(String book, int page) {
    storeBooksForSearch(book);
    return getPage(searchBooks(), page);
  }

  public List<GutenbergBook> searchBooks() {
    return booksWithSearch;
  }

  public void storeBooksForSearch(String book) {
    book = book.toLowerCase();


    booksWithSearch = new ArrayList<>();
    for (GutenbergBook newBook : library.values()) {
      String lowerCaseBook = newBook.title.toLowerCase();
      if (lowerCaseBook.contains(book)) {
        booksWithSearch.add(newBook);
      }
    }
  }

  public int getNumPagesForSearch() {
    return getNumPages(searchBooks());
  }

}
