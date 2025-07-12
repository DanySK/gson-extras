const publishCmd = `
curl -sL https://github.com/shyiko/jabba/raw/master/install.sh | JABBA_COMMAND="install zulu@1.6.119" bash
ruby -e 'puts "org.gradle.java.installations.paths=#{Dir["#{Dir.home}/.jabba/jdk/*"].join(",")}"' >> gradle.properties
git tag -a -f \${nextRelease.version} \${nextRelease.version} -F CHANGELOG.md
git push --force origin \${nextRelease.version}
./gradlew publishAllPublicationsToProjectLocalRepository zipMavenCentralPortalPublication releaseMavenCentralPortalPublication || exit 1
./gradlew publishAllToGithubRepository || true
`
import config from 'semantic-release-preconfigured-conventional-commits' with { type: "json" };
config.plugins.push(
    [
        "@semantic-release/exec",
        {
            "publishCmd": publishCmd,
        }
    ],
    "@semantic-release/github",
    "@semantic-release/git",
)
export default config
