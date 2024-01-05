import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
  id("io.spring.dependency-management") version "1.0.15.RELEASE"

}

group = "com.vas.maz"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}



val vertxVersion = "4.5.0"
val junitJupiterVersion = "5.9.1"
val thymeleafVersion = "3.0.12.RELEASE"
val log4jVersion = "2.14.1"

val mainVerticleName = "com.vas.maz.vertx_starter.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClass.set(launcherClassName)
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))

  // Core Vert.x dependencies
  implementation ("io.vertx:vertx-core:$vertxVersion")
  implementation ("io.vertx:vertx-web:$vertxVersion")




  // Authentication and Authorization
  implementation ("io.vertx:vertx-auth-common:$vertxVersion")
  implementation ("io.vertx:vertx-auth-htdigest:$vertxVersion")
  implementation ("io.vertx:vertx-auth-jwt:$vertxVersion")
  implementation ("io.vertx:vertx-auth-oauth2:4.5.0")

  // Database Clients
  implementation ("io.vertx:vertx-jdbc-client:$vertxVersion")

  // Logging
  implementation("org.apache.logging.log4j:log4j-api")
  implementation("org.apache.logging.log4j:log4j-core")
  implementation ("org.apache.logging.log4j:log4j-slf4j-impl")

  // Other dependencies
  implementation ("mysql:mysql-connector-java:8.0.28")
  implementation ("com.google.protobuf:protobuf-java:3.19.6")
  implementation ("com.fasterxml.jackson.core:jackson-databind:2.13.4.1")
  implementation ("org.mindrot:jbcrypt:0.4")
  implementation ("org.slf4j:slf4j-api:1.7.30")
  implementation("io.vertx:vertx-web-templ-thymeleaf:4.5.0")
  implementation ("org.thymeleaf:thymeleaf:$thymeleafVersion")


  // Testing
  testImplementation ("io.vertx:vertx-junit5:$vertxVersion")
  testImplementation ("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}
