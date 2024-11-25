
-- read the raw gtfs data from unpacked csv-files in directory `./gtfs_data`
-- (without transformation or validation for now)

CREATE TABLE __agency         AS SELECT * FROM read_csv('gtfs_data/agency.txt');
CREATE TABLE __attributions   AS SELECT * FROM read_csv('gtfs_data/attributions.txt');
CREATE TABLE __calendar       AS SELECT * FROM read_csv('gtfs_data/calendar.txt');
CREATE TABLE __calendar_dates AS SELECT * FROM read_csv('gtfs_data/calendar_dates.txt');
CREATE TABLE __feed_info      AS SELECT * FROM read_csv('gtfs_data/feed_info.txt');
CREATE TABLE __frequencies    AS SELECT * FROM read_csv('gtfs_data/frequencies.txt');
CREATE TABLE __routes         AS SELECT * FROM read_csv('gtfs_data/routes.txt');
CREATE TABLE __shapes         AS SELECT * FROM read_csv('gtfs_data/shapes.txt');
CREATE TABLE __stop_times     AS SELECT * FROM read_csv('gtfs_data/stop_times.txt');
CREATE TABLE __stops          AS SELECT * FROM read_csv('gtfs_data/stops.txt');
CREATE TABLE __transfers      AS SELECT * FROM read_csv('gtfs_data/transfers.txt');
CREATE TABLE __trips          AS SELECT * FROM read_csv('gtfs_data/trips.txt');















