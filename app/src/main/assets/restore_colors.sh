#!/system/bin/sh

FILE=$1

restore="settings put system"

while IFS= read -r line
do
    $restore $line
    echo "$restore $line"
done < $FILE