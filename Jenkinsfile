pipeline {
	
    agent {
	    kubernetes {
	        // Change the name of jenkins-maven label to be able to use yaml configuration snippet
	        label "maven-jenkins"
	        // Inherit from Jx Maven pod template
	        inheritFrom "maven"
	        // Add scheduling configuration to Jenkins builder pod template
	        yaml """
spec:
  nodeSelector:
    cloud.google.com/gke-preemptible: true

  # It is necessary to add toleration to GKE preemtible pool taint to the pod in order to run it on that node pool
  tolerations:
  - key: gke-preemptible
    operator: Equal
    value: true
    effect: NoSchedule
    
# Create sidecar container with gsutil to publish chartmuseum index.yaml to Google bucket storage 
  volumes:
  - name: gsutil-volume
    secret:
      secretName: gsutil-secret
      items:
      - key: .boto
        path: .boto
  containers:
  - name: cloud-sdk
    image: google/cloud-sdk:alpine
    command:
    - /bin/sh
    - -c
    args:
    - gcloud config set pass_credentials_to_gsutil false && cat
    workingDir: /home/jenkins
    securityContext:
      privileged: false
    tty: true
    resources:
      requests:
        cpu: 128m
        memory: 256Mi
      limits:
    volumeMounts:
      - mountPath: /home/jenkins
        name: workspace-volume
      - name: gsutil-volume
        mountPath: /root/.boto
        subPath: .boto
"""        
	} 
    }
    
    environment {
      ORG               = "introproventures"
      APP_NAME          = "activiti-cloud-query-graphql-notifications"
      CHARTMUSEUM_CREDS = credentials("jenkins-x-chartmuseum")
      
      CHART_REPOSITORY  = "http://jenkins-x-chartmuseum:8080" 

      CHARTMUSEUM_GS_BUCKET = "introproventures"
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
          
            sh "make preview"

            // Let's release chart into Github repository
            sh "make -C charts/${APP_NAME} github"
          }
          
          container("cloud-sdk") {
          
            // Let's update index.yaml in Chartmuseum storage bucket
            sh "curl --fail -L ${CHART_REPOSITORY}/index.yaml | gsutil cp - gs://${CHARTMUSEUM_GS_BUCKET}/index.yaml"

            input "Pause"
            
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
            sh "make version"
            
            // Let's test first
            sh "make install"

            // Let's make tag in Git
            sh "make tag"
            
            // Let's deploy to Nexus
            sh "make deploy"
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
/*
        failure {

		input """Pipeline failed. 
We will keep the build pod around to help you diagnose any failures. 

Select Proceed or Abort to terminate the build pod"""
        }
*/	

    }
}
