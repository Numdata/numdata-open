pipeline {
	agent any

	triggers {
		pollSCM('H/5 8-17 * * 1-5')
	}

	stages {
		stage ('Build') {
			steps {
				wrap([$class: 'Xvfb']) {
					nodejs(nodeJSInstallationName: 'NodeJS 10', configId: 'numdata-npm-config') {
						sh 'cd js; npm install'

						configFileProvider([configFile(fileId: 'numdata-maven-toolchains', variable: 'TOOLCHAINS')]) {
							withMaven(maven: 'Maven', mavenSettingsConfig: 'numdata-maven-settings') {
								sh "mvn clean install -t $TOOLCHAINS"
							}
						}
					}
				}
			}
		}
	}

	post {
		always {
			junit '**/target/surefire-reports/TEST-*.xml'
			publishCoverage adapters: [jacocoAdapter('coverage-report/target/site/jacoco/jacoco.xml')]
		}
		success {
			archiveArtifacts allowEmptyArchive: false, artifacts: '**/target/*.jar', caseSensitive: true, defaultExcludes: true, fingerprint: false, onlyIfSuccessful: true
		}
	}
}
