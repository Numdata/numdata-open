node () {

	stage ('numdata-open - Checkout') {
		checkout scm
// 		checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '', url: 'https://github.com/Numdata/numdata-open.git']]])
	}

	stage ('numdata-open - Build') {
		wrap([$class: 'Xvfb']) {
			nodejs(nodeJSInstallationName: 'NodeJS 10', configId: 'numdata-npm-config') {
				sh 'cd js; npm install'

				configFileProvider([configFile(fileId: 'numdata-maven-toolchains', variable: 'TOOLCHAINS')]) {
					withMaven(maven: 'Maven', mavenSettingsConfig: 'numdata-maven-settings') {
						if(isUnix()) {
							sh "mvn clean install -t $TOOLCHAINS"
						} else {
							bat "mvn clean install -t $TOOLCHAINS"
						}
					}

					publishCoverage adapters: [jacocoAdapter('coverage-report/target/site/jacoco/jacoco.xml')]
				}
			}
		}

		archiveArtifacts allowEmptyArchive: false, artifacts: '**/target/*.jar', caseSensitive: true, defaultExcludes: true, fingerprint: false, onlyIfSuccessful: true

		junit '**/target/surefire-reports/TEST-*.xml'
	}
}
