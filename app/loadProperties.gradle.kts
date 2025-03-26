import java.util.Properties

val propertiesFile = rootProject.file("secret.properties")
if (propertiesFile.exists()) {
    val properties = Properties()
    properties.load(propertiesFile.inputStream())

    if (properties.containsKey("KEY_STORE_PATH")) {
        project.extensions.extraProperties.set("RELEASE_STORE_FILE", properties.getProperty("KEY_STORE_PATH"))
        project.extensions.extraProperties.set("RELEASE_STORE_PASSWORD", properties.getProperty("KEY_STORE_PASSWORD"))
        project.extensions.extraProperties.set("RELEASE_KEY_ALIAS", properties.getProperty("KEY_ALIAS"))
        project.extensions.extraProperties.set("RELEASE_KEY_PASSWORD", properties.getProperty("KEY_PASSWORD"))
    }
}