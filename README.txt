README

Target 1

Goals that can apply to a "books by author name" view:
(2) A. A minimal solution looks just like the title pages in the starter code.
Extract the author for now by taking the creator field up to the first comma: Dickens, Charles, 1812-1870 -> Dickens
(2) B. A satsifactory solution has the P2 paging system integrated in the results.

Goals that need to be an "authors by author name" view:
(2) E. A better solution keeps authors with the same last name (but different first names) separate, as possible (the identity of the Author class is based on first and last name).
(4) F. A great system extracts authors from the title (e.g. after the word by) if not available in the creator field.
(4) G. A great system has two-levels of pages, because there is now an Author class.
At the first level, links to each author are available, with a count of books by each.
At the second level, books by that author are available.

Target 2

(2) I. A minimal system tracks likes globally. Every click increments a counter, which is stored per-book and visible to us.
(2) J. A satisfactory system tracks likes per-user, but has a user field in every book's form in order to like.
(8) K. A good system will allow users to log in in one place, tracking their username in cookies. Attempting to "like" something without being logged in will actually send the user to the login form.
(4) L. A great system can show which users have liked a book.
(4) M. A great system will also allow a user to visit a "/user/likes" page which shows them what they (or other users) have liked.

(16) Writing Target 

Complete (writing_target.txt)

