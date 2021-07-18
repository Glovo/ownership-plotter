#!/usr/bin/env bash

currentDirectory="$(pwd)"

# Create a temporary directory to store main branch files
temporaryRepositoryPath="$(mktemp --directory)"
# This will remove the temporary directory when script is done executing
trap '{ rm -rf -- "${temporaryRepositoryPath}"; }' EXIT

# Clone project to the temporary directory
git clone git@github.com:Glovo/ownership-plotter.git "${temporaryRepositoryPath}"
# Build the project in the temporary directory
mvn -f "${temporaryRepositoryPath}/pom.xml" clean package -DskipTests

# Extract project properties from pom.xml file
function extractMavenProperty() {
  propertyName="${1}"
  mvn -q -f "${temporaryRepositoryPath}/pom.xml" \
         -Dexec.executable=echo \
         -Dexec.args="\${${propertyName}}" \
         --non-recursive exec:exec 2>/dev/null
}

projectGroupId="$(extractMavenProperty 'project.groupId')"
projectArtifactId="$(extractMavenProperty 'project.artifactId')"
projectVersion="$(extractMavenProperty 'project.version')"

# Install JAR files
mvn deploy:deploy-file \
    -f "${temporaryRepositoryPath}/pom.xml" \
    -DgroupId="${projectGroupId}" \
    -DartifactId="${projectArtifactId}" \
    -Dversion="${projectVersion}" \
    -Dfile="${temporaryRepositoryPath}/target/${projectArtifactId}-${projectVersion}.jar" \
    -Dsources="${temporaryRepositoryPath}/target/${projectArtifactId}-${projectVersion}-sources.jar" \
    -Djavadoc="${temporaryRepositoryPath}/target/${projectArtifactId}-${projectVersion}-javadoc.jar" \
    -Durl="file://for_some_reason_the_first_part_of_path_is_ignored_wtf${currentDirectory}"

artifactDirectory="${projectGroupId//.//}/${projectArtifactId}"
artifactSlug="${projectGroupId}:${projectArtifactId}:${projectVersion}"

# Create a commit
git add "${artifactDirectory}"
git commit "${artifactDirectory}" -m "Install ${artifactSlug}"

echo "Installation of ${artifactSlug} complete"