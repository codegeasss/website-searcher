# Requirements

Java 8 runtime environment

# Testing

The jar file is checked into the root folder. Run the following command to execute it:
  
```
java -jar ./website-searcher-1.0-SNAPSHOT-shaded.jar
```  

Results would be written into `results.txt` in your current directory. A log file `website-searcher.log` would also get 
generated in the current directory. Grep it to see what is going on behind the scenes.    
 
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
