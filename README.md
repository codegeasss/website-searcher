# Requirements

Java 8 runtime environment

# Testing

The jar file is checked into the root folder. Run the following command to execute it:
  
```
java -jar ./website-searcher-1.0-SNAPSHOT-shaded.jar
```  

Results would be written into `results.txt` in your current directory. A log file `website-searcher.log` would also get 
generated in the current directory. Grep it to see what is going on behind the scenes. 

Result for a web url is in the format:

`Original line number, web url, search term, match result(true/false), error if any`

Match result defaults to false for failed URLs.

Please note that, if the `results.txt` already exits in the current directory, it would be appended with new result when 
the program is run multiple times.

Right now search term is hardcoded inside `App.java` file which contains the `main` method. If you want to change the search term or run unit tests, you 
can import the project as maven project into an IDE such as IntelliJ.     
 
# Implementation

#### Searcher

`Searcher.java` is the entry point of the application. On every search, searcher does the following in the given order:

* Initialize tasks and results queue
* Launch 20 crawler threads
* Launch a single result aggregator
* Start reading web urls from the urls file
* Create a task for each url and add it to tasks queue
* Send termination signal to tasks and results queue afters all urls are processed
* Waits for all threads to return before exiting
  
#### Crawler  

Crawler listens to incoming queue for new tasks. As soon as a task is available, crawler would fetch the contents 
from the site and does a search for the term. The result is then queued up in results queue.
Carwler terminates when a NULL is received from incoming queue, which is a signal from
the main thread to terminate. 

#### Result Aggregator 

As the name suggests, `ResultAggregator` class is responsible for aggregating all the results from crawler and writing 
to `results` file. Aggregator listens to results queue and writes to results file as soon as a result is available
This thread terminates when a NULL is received from results queue, which is a signal from the main thread to terminate.

#### BlockingQueue

Thread safe bounded queue implementation using LinkedList. In practice we could also use one of the 
Java <code>BlockingQueue</code> implementations instead.

# Notes

* Although url connection tries both http as well as https, it would fail if the url is malformed or redirected. 
Redirect is not followed. Also some URLs might fail due to missing SSL certificates.
* Text matching is a simple regex matching of the term in the entire website html. We can improve this by matching 
only the `body` of website, ignoring `meta` tags, scripts and other html elements
* Ordering of lines in `results.txt` doesn't follow the original ordering from the `urls` file due to the multi threaded 
nature of the application. But each result has the original line number. We can make ordering consistent with input by 
having result aggregator wait for all results to be available and writing them all out together sorted by line number.   
The downside of this is, it might affect the performance and also run out of memory for large inputs
* There are only few JUnit test cases. To improve the test coverage, we would have to mock out different components 
due to external dependencies. This would mean restructuring some parts of the application but is doable 
