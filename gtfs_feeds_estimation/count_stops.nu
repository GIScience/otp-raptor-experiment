

# download all gtfs archives and extract each stops-file
open gtfs_urls.csv
    | each { |line| if ($"stops__($line.name)" | path exists) == false {
                    try { http get $line.url }
                  | try { save --force --progress $"($line.name).zip" }
                  | try { unzip $"($line.name).zip" -d $"stops__($line.name)" stops.txt }
          } }


wc -l stops__*/stops.txt | lines | sort -r

# wc -l stops__*/stops.txt | lines | sort | to html | save world_stops_by_size.html



