import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.2.2.RELEASE"
	id("io.spring.dependency-management") version "1.0.8.RELEASE"
	id("org.sonarqube") version "2.8"
	id("jacoco")
	id("java")
	kotlin("jvm") version "1.3.61"
	kotlin("plugin.spring") version "1.3.61"
}

group = "com.karthik.users"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	// aws bom
	implementation(platform("software.amazon.awssdk:bom:2.5.29"))
	// aws dynamo db
	implementation("software.amazon.awssdk:dynamodb")

	// arrow-kt core
	implementation("io.arrow-kt:arrow-core:0.10.4")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.3")
	testImplementation("io.kotlintest:kotlintest-extensions-spring:3.3.3")
}

sonarqube {
	properties {
		property("sonar.host.url", "https://sonarcloud.io")
		property("sonar.organization", "chejerlakarthik-github")
		property("sonar.projectName", "user-service")
		property("sonar.projectKey", "chejerlakarthik_user-service")
		property("sonar.projectVersion", "1.0")
		property("sonar.junit.reportPaths", "user-service/build/test-results/test")
		property("sonar.jacoco.reportPaths", "build/jacoco/test.exec")
	}
}

jacoco {
	toolVersion = "0.8.5"
	reportsDir = file("$buildDir/jacocoReports")
}

tasks.jacocoTestReport {
	reports {
		xml.isEnabled = true
		csv.isEnabled = false
		html.destination = file("${buildDir}/jacocoHtml")
	}
}

tasks.test {
	finalizedBy(tasks.jacocoTestReport)
}

tasks.check {
	dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.jacocoTestCoverageVerification {
	violationRules {
		rule {
			limit {
				counter = "LINE"
				value = "COVEREDRATIO"
				minimum = "0.1".toBigDecimal()
			}
		}

//		rule {
//			enabled = false
//			element = "CLASS"
//			includes = listOf("org.gradle.*")
//
//			limit {
//				counter = "LINE"
//				value = "TOTALCOUNT"
//				maximum = "0.3".toBigDecimal()
//			}
//		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}
