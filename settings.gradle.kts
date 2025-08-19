pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        //maven { url = java.net.URI("https://devrepo.kakao.com/nexus/content/groups/public/") }
        // 카카오 지도 SDK를 다운로드하기 위한 저장소 주소
        //maven("https://devrepo.kakao.com/nexus/content/groups/public/")
        maven { url = uri("https://devrepo.kakao.com/nexus/content/groups/public/") }
    }
}

rootProject.name = "pet_project_frontend"
include(":app")
 