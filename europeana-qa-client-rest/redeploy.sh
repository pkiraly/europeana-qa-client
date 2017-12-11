PROJECT=`pwd`
TOMCAT=~/apache-tomcat-8.0.36
APPL=europeana-qa

cd $TOMCAT
bin/shutdown.sh
sleep 3

rm -rf webapps/$APPL
rm -rf webapps/$APPL.war
cp $PROJECT/target/$APPL.war webapps/

bin/startup.sh

cd $PROJECT
