package io.micronaut.guides

import groovy.transform.CompileStatic
import io.micronaut.starter.options.BuildTool

@CompileStatic
class TestScriptGenerator {


    public static final String GITHUB_WORKFLOW_JAVA_CI = 'Java CI'
    public static final String ENV_GITHUB_WORKFLOW = 'GITHUB_WORKFLOW'

    static String emptyScript() {
        '''\
#!/usr/bin/env bash
set -e
exit 0
'''
    }

    static List<String> guidesChanged(String[] changedFiles) {
        changedFiles.findAll { path ->
            path.startsWith('guides')
        }.collect { path ->
            String guideFolder = path.substring('guides/'.length())
            guideFolder.substring(0, guideFolder.indexOf('/'))
        }.unique()
    }

    static boolean changesMicronautVersion(String[] changedFiles) {
        changedFiles.any { str -> str.contains("version.txt") }
    }

    static boolean changesDependencies(String[] changedFiles) {
        changedFiles.any { str -> str.contains("pom.xml") }
    }

    static boolean shouldSkip(String slug, String[] changedFiles) {
        if (changesMicronautVersion(changedFiles) || changesDependencies(changedFiles)) {
            return false
        }
        List<String> guidesChanged = guidesChanged()
        if (System.getenv(ENV_GITHUB_WORKFLOW) && System.getenv(ENV_GITHUB_WORKFLOW) != GITHUB_WORKFLOW_JAVA_CI)  {
            return false
        }
        if (System.getProperty(GuideProjectGenerator.SYS_PROP_MICRONAUT_GUIDE) != null) {
            if (System.getProperty(GuideProjectGenerator.SYS_PROP_MICRONAUT_GUIDE) == slug) {
                return false
            } else {
                return true
            }
        }
        return !guidesChanged.contains(slug)
    }

    static String generateScript(File guidesFolder, String metadataConfigName, boolean stopIfFailure, String[] changedFiles) {
        String bashScript = '''\
#!/usr/bin/env bash
set -e

FAILED_PROJECTS=()
EXIT_STATUS=0
'''

        List<GuideMetadata> metadatas = GuideProjectGenerator.parseGuidesMetadata(guidesFolder, metadataConfigName)
        for (GuideMetadata metadata : metadatas) {
            boolean skip = shouldSkip(metadata.slug, changedFiles)
            if (skip) {
                continue
            }
            List<GuidesOption> guidesOptionList = GuideProjectGenerator.guidesOptions(metadata)
            for (GuidesOption guidesOption : guidesOptionList) {
                String folder = GuideProjectGenerator.folderName(metadata.slug, guidesOption)
                BuildTool buildTool = folder.contains(BuildTool.MAVEN.toString()) ? BuildTool.MAVEN : BuildTool.GRADLE
                if (buildTool == BuildTool.MAVEN && metadata.skipMavenTests) {
                    continue
                }
                if (buildTool == BuildTool.GRADLE && metadata.skipGradleTests) {
                    continue
                }
                if (metadata.apps.any { it.name == GuideProjectGenerator.DEFAULT_APP_NAME } ) {
                    bashScript += scriptForFolder(folder, folder, stopIfFailure, buildTool)
                } else {
                    bashScript += """\
cd ${folder}
"""
                    for (GuideMetadata.App app: metadata.apps) {
                        bashScript += scriptForFolder(app.name, folder + '/' + app.name, stopIfFailure, buildTool)
                    }
                    bashScript += """\
cd ..
"""
                }
            }
        }

        if (!stopIfFailure) {
            bashScript += '''
if [ ${#FAILED_PROJECTS[@]} -ne 0 ]; then
  echo ""
  echo "-------------------------------------------------"
  echo "Projects with errors:"
  for p in `echo ${FAILED_PROJECTS[@]}`; do
    echo "  $p"
  done;
  echo "-------------------------------------------------"
  exit 1
else
  exit 0
fi

'''
        }
        bashScript
    }

    static String scriptForFolder(String nestedFolder, String folder, boolean stopIfFailure, BuildTool buildTool) {
        String bashScript = """\
cd ${nestedFolder}
echo "-------------------------------------------------"
echo "Executing '${folder}' tests"
${buildTool == BuildTool.MAVEN ? './mvnw -q test' : './gradlew -q test' } || EXIT_STATUS=\$?
cd ..
"""
        if (stopIfFailure) {
            bashScript += """\
if [ \$EXIT_STATUS -ne 0 ]; then
  echo "'${folder}' tests failed => exit \$EXIT_STATUS"
  exit \$EXIT_STATUS
fi
"""
        } else {
            bashScript += """\
if [ \$EXIT_STATUS -ne 0 ]; then
  FAILED_PROJECTS=("\${FAILED_PROJECTS[@]}" ${folder})
  echo "'${folder}' tests failed => exit \$EXIT_STATUS"
fi
EXIT_STATUS=0
"""
        }

        bashScript
    }
}
