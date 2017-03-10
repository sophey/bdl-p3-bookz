# Programming Assignment 2 - Book Metadata System

- Instructor: John Foley
- CS341BD - Building a Digital Library
- Spring 2017, Mount Holyoke College

## What's in the repository now:
*Bookz*, a starting point for a library that displays the metadata from 37,526 english books from Project Gutenberg in a web-app.

## Motivation & Learning Goals

The core of this assignment is to grow more familiar with Jetty and to move into the digital library domain. We will build a paging system that will be useful for looking at random books, search results, and books arranged by title.

## Required Changes

### Add a very-simple 'search' functionality

Take input from a text box and find books whose fields match the words given. How you do this is up to you, but do something simple. You can use a GET request for a search engine.

### Build a paging system

Search results, books that start with 'A', and basically anything else we want to add to this system needs paging. We can't dump the 37000 books that have 'the' somewhere in their description to a single page. It would crash our server over time and weigh down users' browsers.

### Build a system for flagging bad entries

We want a way for users to tell us when our metadata is wrong. This is a very open-ended requirement, it could be as simple as a submission that goes (eventually) to be appended to a file. It would be cool if you could go to "/review" and see what's been submitted as errors, though.

