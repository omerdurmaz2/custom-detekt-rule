plugins {
    //id("java-library")
    id("org.jetbrains.kotlin.jvm") // Versiyon numarasını kaldırın
}

dependencies {
    implementation("io.gitlab.arturbosch.detekt:detekt-api:1.23.8") // Kullanılan sürüm doğru mu?
    implementation(kotlin("stdlib"))
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8 // Veya projenize uygun daha yüksek bir sürüm
    targetCompatibility = JavaVersion.VERSION_1_8 // Veya projenize uygun daha yüksek bir sürüm
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
