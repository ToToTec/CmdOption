#!/bin/sh

if [ ! -f mvn-deploy-settings.xml ]; then
        echo "Creating mvn-deploy-settings.xml"
        cat > mvn-deploy-settings.xml <<EOF
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>your-username</username>
      <password>your-password</password>
    </server>
  </servers>
</settings>
EOF

fi

echo "Please review / edit mvn-deploy-settings.xml with propper connection details. Press Enter"
read

echo "Build, sign and upload. Press Enter"
read

REPO="https://oss.sonatype.org/service/local/staging/deploy/maven2/"

mvn\
 -s ./mvn-deploy-settings.xml\
 clean source:jar javadoc:jar package gpg:sign deploy:deploy\
 -DaltDeploymentRepository=ossrh::default::${REPO}

echo "Don't forget to delete sensitive information from mvn-deploy-settings.xml"
