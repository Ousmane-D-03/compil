VERSION=4.13.2
main="main.Main"
path="/usr/local/lib/antlr-"$VERSION"-complete.jar"
export CLASSPATH=$CLASSPATH:$path

java $main
