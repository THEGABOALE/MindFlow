create table nivel_educacional (
    id serial primary key,
    name varchar(100) not null,
    code varchar(50) unique not null,
    description text,
    created_at timestamp default current_timestamp
);

create table misiones (
    id serial primary key,
    level_id integer references nivel_educacional(id) not null,
    title varchar(100) not null,
    description text,
    topic varchar(100),
    created_at timestamp default current_timestamp
);

create table questions (
    id serial primary key,
    mission_id integer references misiones(id) not null,
    question_text text not null,
    option_a text not null,
    option_b text not null,
    option_c text not null,
    correct_option varchar(1) not null,
    created_at timestamp default current_timestamp                                                                                                                                                                                              
);