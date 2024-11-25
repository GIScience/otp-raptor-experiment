
-- read the raw gtfs data from unpacked csv-files
-- (without transformation or validation for now)

CREATE TABLE __agency         AS SELECT * FROM read_csv('agency.txt');
CREATE TABLE __attributions   AS SELECT * FROM read_csv('attributions.txt');
CREATE TABLE __calendar       AS SELECT * FROM read_csv('calendar.txt');
CREATE TABLE __calendar_dates AS SELECT * FROM read_csv('calendar_dates.txt');
CREATE TABLE __feed_info      AS SELECT * FROM read_csv('feed_info.txt');
CREATE TABLE __frequencies    AS SELECT * FROM read_csv('frequencies.txt');
CREATE TABLE __routes         AS SELECT * FROM read_csv('routes.txt');
CREATE TABLE __shapes         AS SELECT * FROM read_csv('shapes.txt');
CREATE TABLE __stop_times     AS SELECT * FROM read_csv('stop_times.txt');
CREATE TABLE __stops          AS SELECT * FROM read_csv('stops.txt');
CREATE TABLE __transfers      AS SELECT * FROM read_csv('transfers.txt');
CREATE TABLE __trips          AS SELECT * FROM read_csv('trips.txt');















