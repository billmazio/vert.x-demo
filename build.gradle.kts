import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id( "com.github.johnrengelman.shadow") version "7.1.2"
  id ("io.spring.dependency-management") version "1.0.15.RELEASE"
}

group = "com.vas.maz"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.5.0"
val junitJupiterVersion = "5.9.1"

val mainVerticleName = "com.vas.maz.vertx_starter.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClass.set(launcherClassName)
}
dependencyManagement {
  imports {
    mavenBom("org.apache.logging.log4j:log4j-bom:2.22.0")
  }
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("org.apache.logging.log4j:log4j-api")
  implementation("org.apache.logging.log4j:log4j-core")
  implementation ("org.apache.logging.log4j:log4j-slf4j-impl")
  implementation ("org.slf4j:slf4j-api:1.7.30")
  implementation ("mysql:mysql-connector-java:8.0.28")
  implementation ("com.google.protobuf:protobuf-java:3.19.6")
  implementation("io.vertx:vertx-jdbc-client:4.5.0")





  implementation("io.vertx:vertx-core:${vertxVersion}")
  implementation("io.vertx:vertx-web:${vertxVersion}")
  implementation("io.vertx:vertx-auth-common:${vertxVersion}")
  implementation("io.vertx:vertx-auth-jwt:${vertxVersion}")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
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
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}
