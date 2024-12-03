


# check if all urls are valid
open gtfs_urls.csv
    | each { |line| try { http head $line.url } 
                | where name == 'content-length'
                | get 0
                | reject name
                | insert url  $line.url
            }

