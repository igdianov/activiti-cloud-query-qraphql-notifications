pipeline {
	
    agent {
	    kubernetes {
	        // Change the name of jenkins-maven label to be able to use yaml configuration snippet
	        label "maven-jenkins"
	        // Inherit from Jx Maven pod template
	        inheritFrom "maven"
	        // Add scheduling configuration to Jenkins builder pod template
	        yamlFile "maven-gke-gsutil.yaml"        
	    } 
    }
    
    environment {
      ORG               = "introproventures"
      APP_NAME          = "activiti-cloud-query-graphql-notifications"
      CHARTMUSEUM_CREDS = credentials("jenkins-x-chartmuseum")
      
      CHART_REPOSITORY  = "http://jenkins-x-chartmuseum:8080" 

      CHARTMUSEUM_GS_BUCKET = "$ORG-chartmuseum"
      GITHUB_CHARTS_REPO    = "https://github.com/igdianov/helm-charts.git"
      
    }
    stages {
      stage("CI Build and push snapshot") {
        when {
          branch "PR-*"
        }
        environment {
          PREVIEW_VERSION = "0.0.0-SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER"
          PREVIEW_NAMESPACE = "$APP_NAME-$BRANCH_NAME".toLowerCase()
          HELM_RELEASE = "$PREVIEW_NAMESPACE".toLowerCase()
        }
        steps {
          container("maven") {
          
            sh "make preview-version"

            sh "make install"
            
            //sh "make skaffold/preview"
            
            sh "make helm/preview"

            //sh "make preview"
          }
        }
      }
      stage("Build Release") {
        when {
          branch "master"
        }
        steps {
          container("maven") {
            // ensure we're not on a detached head
            sh "make checkout"

            // so we can retrieve the version in later steps
            sh "make next-version"
            
            // Let's test first
            sh "make install"

            // Let's build and lint Helm chart
            sh "make helm/build"

            // Let's make tag in Git
            sh "make tag"
            
            // Let's deploy to Nexus
            sh "make deploy"
            
            // Let's build and push Docker image
			sh "make skaffold/release"
            
            // Let's release chart into Chartmuseum
            sh "make helm/release"
            
            // Let's release chart into Github repository
            sh "make helm/github"
            
          }
          container("cloud-sdk") {
            // Let's update index.yaml in Chartmuseum storage bucket
            sh "curl --fail -L ${CHART_REPOSITORY}/index.yaml | gsutil cp - gs://${CHARTMUSEUM_GS_BUCKET}/index.yaml"
          }
          
        }
      }
      stage("Update Versions") {
        when {
          branch "master"
        }
        steps {
          container("maven") {
            // Let's push changes and open PRs to downstream repositories
            sh "make updatebot/push-version"

            // Let's update any open PRs
            sh "make updatebot/update"

            // Let's publish release notes in Github using commits between previous and last tags
            sh "make changelog"
          }
        }
      }
    }
    post {
        always {
            cleanWs()
        }
    }
}
