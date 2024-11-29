



open __gtfs_urls.csv
    | each { |line|  http get $line.url | save --force --progress temp.zip | unzip -o temp.zip stops.txt | open stops.txt | lines | length }




