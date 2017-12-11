CWD=`pwd`
echo Build Metadata QA API
cd ~/git/metadata-qa-api/
git pull
mvn clean install -Ptravis

echo Build Europeana QA API
cd ~/git/europeana-qa-api/
git pull
mvn clean install -Ptravis

echo Build client
cd $CWD
mvn clean install -DskipTests

