

# http get (open de.json | get sources | last 1 | get url).0 | save ___data.zip                                                  │

#let names = [al.json sk.json]


open __gtfs_urls.csv
    | each { |line|  http get $line.url | save --force --progress one.zip }



# $names
#    | each { |name| open $name | get sources | first 1 }
#    | each { |list| $list.0 | get url }
#    | each { |url| http get $url }
#    | each { |download| $download | save one.zip }



