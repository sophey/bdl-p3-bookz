package edu.mtholyoke.cs341bd.bookz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author jfoley.
 */
public class DataImport {
  static ObjectMapper jsonLibrary = new ObjectMapper();
  static {
    jsonLibrary.registerModule(new JsonOrgModule());
  }

  static String getOrNull(JSONObject obj, String key) throws JSONException {
    if(!obj.has(key) || obj.isNull(key)) {
      return null;
    }
    return obj.getString(key);
  }

  static void loadJSONBooks(Map<String, GutenbergBook> library) throws IOException {
    // Load up book data from catalog.jsonl.gz
    try (BufferedReader catalogLines = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("catalog.jsonl.gz")), "UTF-8"))) {

      while(true) {
        String nextLine = catalogLines.readLine();
        if(nextLine == null) break; // done with file
        try {
          JSONObject json = jsonLibrary.readValue(nextLine, JSONObject.class);

          GutenbergBook book = new GutenbergBook();
          book.title = json.getString("friendly_title");
          book.longTitle = json.getString("title");
          book.id = json.getString("id");
          book.uploaded = json.getString("created");
          book.downloads = json.getInt("downloads");
          book.creator = getOrNull(json,"creator");

          JSONArray subjects = json.getJSONArray("subjects");
          for (int i = 0; i < subjects.length(); i++) {
            String[] subj = subjects.getString(i).split("\t");
            if(subj[0].equals("dcterms:lcc")) {
              book.libraryOfCongressSubjectCode.add(subj[1]);
            } else if(subj[0].equals("dcterms:lcsh")) {
              book.libraryOfCongressSubjectHeading.add(subj[1]);
            }
          }

          JSONArray kblinks = json.getJSONArray("kblinks");
          for (int i = 0; i < kblinks.length(); i++) {
            book.maybeWikipedias.add(kblinks.getString(i));
          }

          library.put(book.id, book);

        } catch (JSONException e) {
          // don't let a bad book stop us from loading most of them!
          e.printStackTrace(System.err);
          continue;
        }
      }
    }

    System.out.println("Loaded up "+library.size()+" books into our library!");
  }
}
