#!/bin/bash
p2UpdateSite=$WORKSPACE/releng/org.eclipse.gendoc.update-site/target
updateSite=$WORKSPACE
nightlyDownloadSrc=$WORKSPACE/releng/toolkit/downloads/updates/nightly/latest
nightlyDownloadTarget=/home/data/httpd/download.eclipse.org/gendoc/updates/nightly/latest
timestamp=$(date +%s000)

rm -rf $updateSite/repository
rm -rf $updateSite/*.zip
mkdir -p $updateSite/repository
mv $p2UpdateSite/repository $updateSite
mv $p2UpdateSite/*.zip $updateSite

rm -rf $nightlyDownloadTarget
mkdir -p $nightlyDownloadTarget
cp -rf $nightlyDownloadSrc $nightlyDownloadTarget/..

# Replace the timestamp in the repo files 
sed -i "s/!!__TIMESTAMP__!!/${timestamp}/" $nightlyDownloadTarget/compositeContent.xml 
sed -i "s/!!__TIMESTAMP__!!/${timestamp}/" $nightlyDownloadTarget/compositeArtifacts.xml 
