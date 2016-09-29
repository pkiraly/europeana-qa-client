CWD=`pwd`
cd ~/git/metadata-qa-api/
mvn clean install
cd ~/git/europeana-qa-api/
mvn clean install
cd $CWD
mvn clean install -DskipTests

