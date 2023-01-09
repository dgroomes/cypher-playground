-- Initialize the relational schema.

-- States have a natural primary key, which is the state abbreviation.
create table states (state_code char(2) primary key, state_name text not null);

-- Note: I don't think I need a primary key on this table because no other table references it. In fact, it is itself
-- just a reference table.
create table state_adjacencies (state_code char(2) references states(state_code), adjacent_state_code char(2) references states(state_code), unique (state_code, adjacent_state_code));

-- Cities do not have a natural primary key because a given city name, like Springfield, can exist in many states.
-- We'll use a surrogate key.
create table cities (id serial primary key, city_name text not null, state_code char(2) references states(state_code), unique (city_name, state_code));

-- ZIP codes are their own natural primary key.
create table zip_codes (zip_code char(5) primary key, city_id int references cities(id), population int);
