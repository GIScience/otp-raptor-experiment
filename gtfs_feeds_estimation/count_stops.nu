

# cleanup last run
rm -rf stops__*
rm -rf *.zip

#download all gtfs archives and extract each stops-file
open __gtfs_urls.csv
    | each { |line| http get $line.url
                  | save --force --progress $"($line.name).zip"
                  | unzip $"($line.name).zip" -d $"stops__($line.name)" stops.txt
      }

#sum up the total length of all stops-files
ls stops__* | get name
    | each { |directory| open $"($directory)/stops.txt" | lines | length  }
    | math sum


