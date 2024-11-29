


open __gtfs_urls.csv
    | each { |line| http get $line.url
                  | save --force --progress $"($line.name).zip"
                  | unzip -o $"($line.name).zip" -d $"($line.name)" stops.txt
                  | cd $"($line.name)"
                  | open stops.txt | lines | length
      }
    | math sum

