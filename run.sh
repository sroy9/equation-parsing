#mvn -q dependency:copy-dependencies
#mvn -q compile

CP="./target/classes/:./target/dependency/*:./config/" # use this 
OPTIONS="-cp $CP"
#PACKAGE_PREFIX="edu.illinois.cs.cogcomp"

MAIN="relation.RelationDriver"
#MAIN="mentiondetect.MentionDetector"
time nice java $OPTIONS $MAIN $*
