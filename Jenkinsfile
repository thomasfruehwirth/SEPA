pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        echo 'Hello World'
        withMaven(maven: 'maven_jekins', jdk: 'JDK9') {
          sh 'mvn clean package'
        }
        
      }
    }
    stage('Test') {
      steps {
        def ws = pwd()
        def endpoint-blaze-file = ws + '/engine/target/endpoints/endpoint-blaegraph.jpar'
        def target = ws + 'engine/target'
        sh 'mv' + endpoint-blaze-file + ' ' + target + '/endpoint.jpar'
        sh 'java -server -jar '+ ws + '/engine/target/engine-0-SNAPSHOT &'
        sh 'java -server -Xmx4g -jar /home/cristianoaguzzi/blazegraph.jar &'
        timeout(10) {
            waitUntil {
               script {
                 def r = sh script: 'wget -q http://localhost:9999 -O /dev/null', returnStatus: true
                 return (r == 0);
               }
            }
        }
        withMaven(jdk: 'JDK9', maven: 'maven_jekins') {
          sh 'mvn verify  -Dmaven.javadoc.skip=true'
        }
      }
    }
  }
}