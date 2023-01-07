-- Load the Apache AGE extension.
--
-- These steps are described by the Apache AGE README: https://github.com/apache/age

create extension age;
load 'age';
alter role postgres set search_path = public, ag_catalog;
