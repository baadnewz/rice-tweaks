#!/bin/sh
# Run this shell script with 1 argument (path of a file) and it will output all variables and values that begin with tweaks
# i.e. find_tweaks.sh some.xml
# the command above will output something like this
# tweaks_notification_background_color -788529152
# tweaks_color 5556


# The file you are searching, change this to whatever you want
FILE=$1

# Get the grep result from the file note I h
GREPRESULT=`grep -Eo "\"tweaks_.*_color*\"[[:space:]]value=\"-*[[:digit:]]*" $FILE | sed 's/ //g'`
 
# Loop through the results
for RESULT in $GREPRESULT
do
    TEMP1=${RESULT//[\"]/}
    RESULT=${TEMP1/"value="/" "}
    echo "$RESULT"
done