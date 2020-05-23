def branchName= env.BRANCH_NAME
def buildNum = env.BUILD_NUMBER
def project_token = 'UHUENBBBtreBBDyhhfkd888553099jjYYUU6PPPn'
properties([
    gitLabConnection('gitlab.example.com'),
    pipelineTriggers([
        [
            $class: 'GitLabPushTrigger',
            branchFilterType: 'All',
            triggerOnPush: true,
            triggerOnMergeRequest: true,
            triggerOpenMergeRequestOnPush: "never",
            triggerOnNoteRequest: true,
            noteRegex: "Jenkins please retry a build",
            skipWorkInProgressMergeRequest: true,
            secretToken: project_token,
            ciSkip: false,
            setBuildDescription: true,
            addNoteOnMergeRequest: true,
            addCiMessage: true,
            addVoteOnMergeRequest: true,
            acceptMergeRequestOnSuccess: true,
            branchFilterType: "NameBasedFilter",
            includeBranchesSpec: "${branchName}",
            excludeBranchesSpec: "",
        ]
    ])
])


node(){
  try{
    print buildNum
    print branchName
      
    /* STAGE 1 - Print Git & Jenkins informations : /!\ CHANGE THIS URL WITH YOU OWN REPO /!\ */
    stage('Git check'){
      git branch: branchName, credentialsId: "git-credentials", url: "https://github.com/kenybapin/Simple_Pipeline.git"
    }
    
    /* commitID */
    def commitIdLong = sh returnStdout: true, script: 'git rev-parse HEAD'
    def commitId = commitIdLong.take(7)
    
    /* AppVersion */
    def version = sh returnStdout: true, script: "awk -F'[<>]' '/<version>/{print \$3}' ./pom.xml | head -n 1"

    /* Target server */
    if (branchName == "test" ){
      server = "app-test"
    }
    if (branchName == "preprod" ){
      server = "app-preprod"
    }
    if (branchName == "prod" ){
      server = "app-prod"
    }

    print """
    ========================================================================================    
    Banch: $branchName
    Commit: $commitId
    Job: $buildNum
    Server : $server
    AppVersion: $version
    ========================================================================================
    """
    
    /* STAGE 2 - & TEST and INSTALL */
    stage('MAVEN Test & Build'){
      sh 'mvn -B clean test'
      sh 'mvn -B clean install'
    }

    /* trim variables for docker build (easy fix) */
    def vers = version.trim()
    def cid = commitId.trim()
    def branch = branchName.trim()

    def imageName='192.168.100.20:5000/app'
    
    /* STAGE 3 - & BUILD DOCKER IMAGE */
    stage('DOCKER Build & Push to registry'){
      docker.withRegistry('http://192.168.100.20:5000', 'myregistry_login') {
        def customImage = docker.build("$imageName:${vers}-${branch}-${cid}")
         customImage.push()
      }
      withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'myregistry_login',usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        sh 'curl -sk --user $USERNAME:$PASSWORD https://192.168.100.20:5000/v2/app/tags/list'
      }
      sh "docker rmi $imageName:${vers}-${branch}-${cid}"
    }
    
    
    /* STAGE 4 - DEPLOY */
     stage('Ansible deploy'){
        ansiblePlaybook (
          playbook: "ansible/install.yml",
          hostKeyChecking: false,
          inventory: "ansible/hosts",
          extras: "-u vagrant -e 'image=$imageName:${vers}-${branch}-${cid}' -e 'tag=${vers}-${branch}-${cid}' -e 'server=${server}'"
          )
     }
    
  } finally {
    cleanWs()
  }
}
