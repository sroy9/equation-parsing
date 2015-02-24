#mvn -q dependency:copy-dependencies
#mvn -q compile

CP="./target/classes/:./target/dependency/*:./config/" # use this 
OPTIONS="-cp $CP"
PACKAGE_PREFIX="relevance"

MAIN="$PACKAGE_PREFIX.RelDriver"
time nice java $OPTIONS $MAIN $*
