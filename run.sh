#mvn -q dependency:copy-dependencies
#mvn -q compile

CP="./target/classes/:./target/dependency/*:./config/" # use this 
OPTIONS="-cp $CP"
#PACKAGE_PREFIX="edu.illinois.cs.cogcomp"

MAIN="structure.Blob"
#MAIN="$PACKAGE_PREFIX.quant.lbj.CTakesTokenReader"
time nice java $OPTIONS $MAIN $*
