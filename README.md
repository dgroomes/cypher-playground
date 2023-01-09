# cypher-playground

NOT YET FULLY IMPLEMENTED

📚 Learning and exploring the Cypher query language for graph databases like Apache AGE.

> Cypher is a declarative graph query language that allows for expressive and efficient data querying in a property
> graph.
> 
> -- <cite> https://en.wikipedia.org/wiki/Cypher_(query_language) </cite>


> Apache AGE:
> Graph Processing & Analytics for RDBs
> 
> -- <cite> https://age.apache.org/ </cite>


## Overview

This project explores the ergonomics of evolving an existing relational data model to a graph model. Specifically, we
explore migrating existing relational data in a Postgres database to an [Apache AGE](https://age.apache.org/) graph model
in the same Postgres database. We want to answer these questions:

1. What is the developer experience of Cypher?
   * How do Cypher queries compare to SQL queries? I expect recursive queries to be the main draw of Cypher. But what
     are the awkward parts of Cypher? 
2. What is the data migration story for Apache AGE?
   * What tools does Apache AGE offer to migrate existing relational data to a graph model? And better yet, can we skip
     a migration altogether? Meaning, can we write Cypher queries over relational data? (The answer is no; but I know
     Apache AGE is rapidly evolving and is strategically invested in relational databases, so I'm hopeful that this area
     enriches over time).

This project uses US geographies as its data domain. Specifically, we'll model ZIP codes, their containing city and
their containing state. This creates a tree-like structure. This data model is not complex enough to warrant a graph
data model so let's make it more interesting and also model "state adjacencies". For example, Minnesota neighbors
Wisconsin.

This project uses Docker to run a Postgres database pre-installed with Apache AGE.

This project defines a multi-module Gradle project that defines Java programs that load the initial domain data, migrate
the data from a relational state to a graph state, and query the data.

Here is a breakdown of the components of this project:

* `docker-compose.yml` and `postgres-init/`
   * This is the Docker-related stuff. The Docker Compose file defines the Postgres container and mounts the `postgres-init/`
     directory into the container. The `postgres-init/` directory contains the SQL scripts that initialize the database
     with the relational schema and the US state data.
* `data-loader/`
    * `data-loader/` is a Gradle module. It defines a Java program that loads the ZIP code and city data from the
      `zips.jsonl` file.
* `data-migrator/`
    * NOT YET IMPLEMENTED
    * `data-migrator/` is a Gradle module. It defines a Java program that migrates the relational data to a graph data
      model.
* `data-queryer/`
    * NOT YET IMPLEMENTED
    * `data-queryer/` is a Gradle module. It defines a Java program that queries the graph data using Cypher.


## Background

I'm interested in learning graph-based query languages. While I love SQL, the ability to express a pattern-matching
query over a graph of data and get a serialized "object graph" response is something I often pine for when I'm otherwise
stuck with a SQL query full of joins. I've been eyeing graph databases for a long time (but also cautiously eyeing them
because you don't want to get tangled up with a technology that gets abandoned). I've had some brief but good experience
using Cypher queries and now I want to learn more in-depth. Graph databases have gone through the hype cycle and
hopefully we are nearing the "plateau of productivity", but we're not there yet. There are competing technologies, none
of which have cemented a lead. Still, there is a lot of activity in the space.

GQL (Graph Query Language) is a standards-body proposed graph query language heavily inspired by Cypher but it is not
yet a real thing. Cypher™️ *proper* is actually a Neo4J-specific language. Neo4J graciously supported an open
specification called *openCypher* which is basically Cypher but it is meant to be implemented by different vendors and
open source projects. openCypher is what I am exploring in this playground repository.


### Apache AGE

For this project, I have to choose a database that supports openCypher. [Apache AGE](https://age.apache.org/) is a
Postgres extension that brings graph capabilities to the very mature and wildly popular Postgres database. AGE is an
acronym for "A Graph Extension". AGE is very promising because it is tied to Postgres (a sign of stability and maturity),
it reached a 1.0 release in 2022 and it is under the Apache umbrella (another sign of durability). The project has good
momentum. Plus it has a Java client. I'll use AGE for this playground repository.

This playground repository is effectively a playground for both Cypher and Apache AGE.


### Forward Looking: GQL

Lastly, I have my eyes on [SQL/PGQ](https://en.wikipedia.org/wiki/Graph_Query_Language#SQL/PGQ_Property_Graph_Query)
which is a proposed extension to the SQL standard which would allow for graph queries. This is by far the most
conservative leap from the SQL world to the graph world and this is what I'm most interested in. But this project is
extremely early so there's nothing to play with yet.


## Instructions

Follow these instructions to get up and running with a graph database, some sample data, and some cypher queries.

1. Pre-requisite: Docker
2. Start the Postgres database with the AGE extension.
   * ```shell
     docker-compose up --detach
     ```
   * As part of the startup procedure, the relational schema is created and the US state data gets loaded.  
3. Load the ZIP code and city data.
   * ```shell
     ./gradlew :data-loader:run
     ```
   * It will look something like the following.
   * ```text
     00:19:46 [main] INFO  dataloader.Main - Loading ZIP code data from the local file into Postgres ...
     00:20:22 [main] INFO  dataloader.Main - Loaded 25,701 cities and 29,353 ZIP codes.
     ```
4. Migrate the relational data to a graph model.
   * NOT YET IMPLEMENTED
   * ```shell
     ./gradlew :data-migrator:run
     ```
5. Query the graph data.
   * NOT YET IMPLEMENTED
   * ```shell
     ./gradlew :data-queryer:run
     ```
   * Read the Java source code to understand the Cypher queries.
6. When you're done, stop the database.
   * ```shell
     docker-compose down
     ```


## Notes

The [AGE manual](https://age.apache.org/age-manual) is great. Here are some quotes.

> Cypher uses a Postgres namespace for every individual graph. It is recommended that no DML or DDL commands are
> executed in the namespace that is reserved for the graph.

> AGE uses a custom data type called agtype, which is the only data type returned by AGE. Agtype is a superset of Json
> and a custom implementation of JsonB.

> Cypher cannot be used in an expression, the query must exists in the FROM clause of a query. However, if the cypher
> query is placed in a Subquery, it will behave as any SQL style query.


## Wish List

General clean-ups, TODOs and things I wish to implement for this project:

* [ ] Add a Java client to the project.
* [x] DONE What does it mean that AGE supports hybrid SQL-cypher querying? I know technically that's what I already have but
  it's just a `select * from`; it's a no-op. What is a more interesting example?
  * The [*AGE beyond Cypher*](https://age.apache.org/age-manual/master/advanced/advanced_overview.html) section of the
    docs gets into it. Update: ok I kind of get it.
* [x] DONE Is there any way to port an existing relational table to a graph? Even if it is a hacky way to do it, I'd like to
  prove that you don't need to throw away your existing hard-earned data and modelling to adopt the graph way.
   * No, there isn't any mechanism in AGE to do this. [The recommended workaround is to indeed roll your own migration](https://github.com/apache/age/issues/289#issuecomment-1244135270)
     but this subject is on the radar of the AGE project. And indeed we should expect this functionality because the
     above-the-fold marketing content on [the Apache AGE home page](https://age.apache.org/) says the following.

     > Through Apache AGE, PostgreSQL users will gain access to graph query modeling within the existing relational database.
   
   * The word "existing" suggests existing *data* in the way I read it. But now I see that it is making the selling
     point that you can use you existing database *technology* (e.g. Postgres, and in the future MySQL, etc.). 

* [ ] Consider building from source; might not be worth it.
* [ ] Use the Apache AGE Viewer to visualize the graph.
* [x] DONE Bring in an interesting set of example data as *relational data*. Consider the ZIP code data of my other projects.
  * [My other project `dgroomes/mongodb-playground`](https://github.com/dgroomes/mongodb-playground) has ZIP data. I'll
    bring it to this project here and import it maybe as CSV?
* [ ] Write a relational-to-graph migration program to port the data from relational SQL tables to an AGE graph. This
  program should not be generic. It should be specific to the data I'm working with.
* [ ] Write some Cypher queries over the graph data. They should engage the cyclic nature of the graph data. Also write
  some aggregation queries which should not engage the cyclic nature of the graph data. I'm talking about "find objects
  that relate to objects that look like XYZ" and then "sum up the numeric field ABC and find the top 10 results". I want
  to compare and contrast Cypher with SQL but be fair and give them challenges that they are suited to.  
* [ ] Write SQL queries over the graph data. They should engage the cyclic nature of the data.
* [ ] This isn't related to Cypher or AGE at all, but I'd like to maybe do a stored procedure so I can load the data in
  a batch. Right now the bottleneck is that I need to do a full round trip just to insert one city and get its surrogate
  key. I know Hibernate's trick is that it generates its own keys instead of letting Postgres do it. I think it locks
  onto a block range of keys or something? I suppose that's clever.


## Reference

* [openCypher GitHub repository](https://github.com/opencypher/openCypher)
  * openCypher is a specification of the Cypher query language
* [Wikipedia: *Graph Query Language*](https://en.wikipedia.org/wiki/Graph_Query_Language)
  * GQL (Graph Query Language) is a proposed standard. It borrows heavily from Cypher. 
* [HackerNews comment about Apache AGE, SQL/PGQ, and GQL](https://news.ycombinator.com/item?id=27549469)
  * Thanks to this person for leaving the comment. It's a great concise summary.
* [Apache AGE docs](https://age.apache.org/age-manual/master/index.html)
