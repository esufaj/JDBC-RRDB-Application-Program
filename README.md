# JDBC-RRDB-Application-Program

The Raccoon-Rhapsody Database: 

Create files and drop files are provided for convenience at the bottom.


An application program that will use JDBC to send automatic updates to the RR database in order to update game/quest entries

The Raccoon Rhapsody(RRDB), the multi-player online game by Questeme, has become wildly popular. The company wants to streamline maintaining the RR-DB database that backs the game.


The Task:

One task that game administrators have to do is to create new quests on a regular basis for the players. This involves making a new entry into the Quest table for the new quest for a future day with a given theme and region. And then assigning loot — adding to the Loot table — for the new quest. 

I have been automate this task with an application program, let us call it CreateQuest. It was created in Java using JDBC; so, CreateQuest.java. The app connects with Questeme's PostgreSQL database server at db with the RR-DB database to make the necessary updates on request.


Specification:

The app will be called from the command line on one of Questeme's machines on its local area network e.g.,

% java CreateQuest '2021-01-15' Camelot 'Merlin Prophesy' 20000 username 0.618

The app then adds that quest to the database, if possible, and populates the quest with loot.


Parameters:

CreateQuest <day> <realm> <theme> <amount> [<user>] [seed]

    day: the day for the new quest
    realm: which realm the new quest is in
    theme: the theme for the new quest
    amount: the floor for the sum of the assigned loot by value (sql)
    user (optional): which user and database the app is connecting with and to, respectively. This should default to your user name (which is also your database's name).
    seed (optional): a real (float) number between -1 and 1 that is seeded before the use of random(). (If no seed is provided, then no seeding is to be done beforehand.)


Operation:

Given no failure mode occurs, the app should proceed to do the following.

    A tuple is added to the Quest table with the specified day, realm, and theme (in database user).
    Tuples are added to the Loot table that “assign” loot to the new quest, following the loot assignment rules below
    

Loot Assignment Rules:

Loot is to be assigned to the new quest randomly, but with the following two constraints.

    distinct. Two pieces of loot of the same type (treasure) are not assigned. (That is, we are sampling the Treasure table without replacement.)
    exceeds. The sum of the assigned loots's value — the corresponding sql in the Treasure table — equals or exceeds that requested (parameter amount).
    
    
Design Consideratons and Mandates:    

The app uses a driver to connect to the PSQL db. The driver we are using is postgresql-42.2.14.jar. 

Authentication:

The way this would be best set up, say, at Questeme, would be via SSL and certificates, to provide a “drop-through” authentication. This although would be difficult to do for this project, thus instead, we shall use a .pg_pass file in the home directory on the local machine. In that file, you will have a line like

db:*:username:username:not_my_password

where username (the user and then database) is replaced by your Postgres user name (and database name, which is the same). And replacing the last field with your Postgres password, of course.

We can use a Java package pgpass courtesy of technology16 at GitHub - technology16/pgpass: Simple Java .pgpass file loader under the Apache License 2.0. A copy of this is compiled and available for use in …/PG/. Thus, you can

import pgpass.*;

and call

String passwd = PgPass.get("db", "*", user, user);

for your program to fetch the password from '~/.pgpass' of the person invoking the program.


Seeding:

The program chooses treasure entries at random to populate loot for the new quest. Postgres provides a function called random() that returns a random float between 0 and 1. Programs that take random actions, however, are hard to debug! Providing the same seed, however, a second invocation of the program means that it will get the same sequence of “random” choices as the first invocation.

To seed the random function in Postgres, one executes

select setseed(<seed>);

with <seed> above replaced with a float between -1 and 1; e.g., setseed(0.618).


