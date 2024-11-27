-- creates a table with a row for every route id.
-- the second column contains a list with all stops for the given route

CREATE TABLE routes_with_stops AS
    SELECT
        routes.route_id,
        list(stop_times.stop_id) as stops
    FROM
        __trips as trips,
        __routes as routes,
        __stop_times as stop_times
    WHERE
        trips.route_id = routes.route_id AND
        trips.trip_id = stop_times.trip_id
    GROUP BY
        routes.route_id
    ;