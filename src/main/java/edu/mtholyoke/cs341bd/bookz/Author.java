package edu.mtholyoke.cs341bd.bookz;

import org.apache.commons.lang3.text.WordUtils;

/**
 * Created by Sophey on 3/8/17.
 */
public class Author implements Comparable<Author> {

  String firstName;
  String lastName;
  int birth;
  int death;

  public Author(String firstName, String lastName, int birth, int death) {
    this.firstName = WordUtils.capitalizeFully(firstName).trim();
    this.lastName = WordUtils.capitalizeFully(lastName).trim();
    this.birth = birth;
    this.death = death;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public int getBirth() {
    return birth;
  }

  public int getDeath() {
    return death;
  }

  @Override
  public String toString() {
    if (firstName.length() == 0)
      return lastName;
    return lastName + ", " + firstName;
  }

  public String getPlusString() {
    if (firstName.length() == 0)
      return lastName.replaceAll("\\s", "+");
    return (lastName + "&" + firstName).replaceAll("\\s", "+");
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return toString().equals(o.toString());
  }

  @Override
  public int compareTo(Author other) {
    return toString().compareTo(other.toString());
  }

}
