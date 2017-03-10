package edu.mtholyoke.cs341bd.bookz;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author jfoley.
 */
public class GutenbergBook {
  public String id;
  public String title;
  public String longTitle;
  public String creator;
  public String uploaded;
  public List<String> maybeWikipedias = new ArrayList<>();
  public List<String> libraryOfCongressSubjectHeading = new ArrayList<>();
  public List<String> libraryOfCongressSubjectCode = new ArrayList<>();
  public int downloads;

  public int getBookNumber() {
    return Integer.parseInt(
        Objects.requireNonNull(
            Util.getAfterIfStartsWith("etext", id)));
  }

  public String getGutenbergURL() {
    return "http://www.gutenberg.org/ebooks/"+getBookNumber();
  }
}
