# cypher-playground

NOT YET FULLY IMPLEMENTED

📚 Learning and exploring the Cypher query language for graph databases.

> Cypher is a declarative graph query language that allows for expressive and efficient data querying in a property
> graph.
> 
> -- <cite> https://en.wikipedia.org/wiki/Cypher_(query_language) </cite>


## Overview

I'm interested in learning graph-based query languages. While I love SQL, the ability to express a pattern-matching
query over a graph of data and get a serialized "object graph" response is something I often pine for when I'm otherwise
stuck with a SQL query full of joins. I've been eyeing graph databases for a long time (but also cautiously eyeing them
because you don't want to get tangled up with a technology that gets abandoned). I've had some brief but good experience
using Cypher queries and now I want to learn more in-depth. Graph databases have gone through the hype cycle and
hopefully we are nearing the "plateau of productivity", but we're not there yet. There are competing technologies, none
of which have cemented a lead. Still, there is a lot of activity in the space.

GQL (Graph Query Language) is a standards-body proposed graph query language heavily inspired by Cypher but it is not
yet a real thing. Cypher™️ *proper* is actually a Neo4J-specific language. Neo4J graciously supported an open
specification called *openCypher* which is basically Cypher but it it meant to be implemented by different vendors and
open source projects. openCypher is what I am exploring in this playground repository.


### Apache AGE

For this project, I have to choose a database that supports openCypher. [Apache AGE](https://age.incubator.apache.org/)
is a Postgres extension that brings graph capabilities to the very mature and wildly popular Postgres database. AGE is an
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
3. Start a psql session.
   * ```shell
     docker exec --interactive --tty cypher-playground-postgres-1 psql --username postgres
     ```
4. Create a sample graph.
   * ```sql
     SELECT create_graph('my_graph');
     ```
   * Note: this is following the [*Quick Start* instructions in the Apache AGE README](https://github.com/apache/age#quick-start).
5. Create a vertex.
   * ```sql
     SELECT *
     FROM cypher('my_graph', $$
         CREATE (n)
     $$) as (v agtype);     
     ```
   * Yes, it is verbose. This is because the Cypher query is embedded in a SQL query.
6. Query the graph
   * ```sql
     SELECT * FROM cypher('my_graph', $$
     MATCH (v)
     RETURN v
     $$) as (v agtype);
     ```
   * It returns a serialized graph object. Interestingly, it is embedded in a regular table-style result set because we
     are operating in a traditional psql and relational context. Altogether, the query and result  looks like the
     following.
   * ```text
     postgres=# SELECT * FROM cypher('my_graph', $$
     postgres$# MATCH (v)
     postgres$# RETURN v
     postgres$# $$) as (v agtype);
                                    v
     ----------------------------------------------------------------
      {"id": 281474976710657, "label": "", "properties": {}}::vertex
     (1 row)
     ```
7. When you're done, stop the database.
   * ```shell
     docker-compose down
     ```


## Notes

The [AGE manual](https://age.apache.org/age-manual) is great. Here some quotes.

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
* [ ] Consider bringing in more interesting and bigger example data. Consider the ZIP code data of my other projects.
  * [My other project `dgroomes/mongodb-playground`](https://github.com/dgroomes/mongodb-playground) has ZIP data. I'll
    bring it to this project here and import it maybe as CSV? 


## Reference

* [openCypher GitHub repository](https://github.com/opencypher/openCypher)
  * openCypher is a specification of the Cypher query language
* [Wikipedia: *Graph Query Language*](https://en.wikipedia.org/wiki/Graph_Query_Language)
  * GQL (Graph Query Language) is a proposed standard. It borrows heavily from Cypher. 
* [HackerNews comment about Apache AGE, SQL/PGQ, and GQL](https://news.ycombinator.com/item?id=27549469)
  * Thanks to this person for leaving the comment. It's a great concise summary.
* [Apache AGE docs](https://age.apache.org/age-manual/master/index.html)
