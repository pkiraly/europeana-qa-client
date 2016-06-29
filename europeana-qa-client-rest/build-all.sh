CWD=`pwd`
cd ~/git/metadataqa-api/
mvn clean install
cd ~/git/europeana-qa-api/
mvn clean install
cd $CWD
mvn clean install -DskipTests

