-- Create a new database to keep track of NFL football teams during the season. There should be at least three tables:

CREATE TYPE conference_type AS ENUM ('afc', 'nfc');
CREATE TYPE division_type AS ENUM ('east', 'north', 'south', 'west');

-- Team: TeamId, Location, Nickname, Conference, Division; e.g., 1, Kansas City, Chiefs, AFC, West
CREATE TABLE team
    (
        team_id SERIAL
            PRIMARY KEY NOT NULL,
        location varchar(50) NOT NULL,
        nickname varchar(50) NOT NULL,
        conference conference_type NOT NULL,
        division division_type NOT NULL
    );
-- Game: GameId, TeamId1, TeamId2, Score1, Score2, Date
CREATE TABLE game
    (
        game_id SERIAL
            PRIMARY KEY NOT NULL,
        team_id1 int,
        team_id2 int,
        score1 int
            CHECK ( score1 >= 0 ),
        score2 int
            CHECK ( score2 >= 0 ),
        date date
            CHECK ( date >= '2024-01-01'),
        FOREIGN KEY (team_id1) REFERENCES team (team_id) ON DELETE NO ACTION,
        FOREIGN KEY (team_id2) REFERENCES team (team_id) ON DELETE NO ACTION
    );

CREATE TYPE position_type AS ENUM (
    'quarterback',
    'running_back',
    'wide_receiver',
    'tight_end',
    'defensive_end',
    'linebacker',
    'cornerback',
    'safety'
    );


-- Player: PlayerId, TeamId, Name, Position
CREATE TABLE player
    (
        player_id SERIAL
            PRIMARY KEY NOT NULL,
        team_id int,
        name varchar(50) NOT NULL,
        position position_type NOT NULL,
        FOREIGN KEY (team_id) REFERENCES team (team_id) ON DELETE NO ACTION
    );