package com.example.ide.templates

import java.io.File

object FabricTemplateGenerator {

    fun createFabricModProject(
        baseDir: File,
        modName: String,
        modId: String,
        packageName: String,
        mcVersion: String = "1.20.4",
        useKotlin: Boolean = false
    ) {
        val root = File(baseDir, modId.lowercase())
        root.mkdirs()

        // 1. gradle.properties
        File(root, "gradle.properties").writeText(
            """
            # Fabric Properties
            minecraft_version=$mcVersion
            yarn_mappings=1.20.4+build.3:v2
            loader_version=0.15.6
            fabric_version=0.92.0+1.20.4

            # Mod Properties
            mod_version=1.0.0
            maven_group=$packageName
            archives_base_name=$modId
            """.trimIndent()
        )

        // 2. settings.gradle
        File(root, "settings.gradle").writeText(
            """
            pluginManagement {
                repositories {
                    maven {
                        name = 'Fabric'
                        url = 'https://maven.fabricmc.net/'
                    }
                    mavenCentral()
                    gradlePluginPortal()
                }
            }

            rootProject.name = '$modId'
            """.trimIndent()
        )

        // 3. build.gradle
        File(root, "build.gradle").writeText(
            """
            plugins {
                id 'fabric-loom' version '1.5-SNAPSHOT'
                id 'maven-publish'
                ${if (useKotlin) "id 'org.jetbrains.kotlin.jvm' version '1.9.22'" else ""}
            }

            version = project.mod_version
            group = project.maven_group

            repositories {
                mavenCentral()
            }

            dependencies {
                minecraft "com.mojang:minecraft:${'$'}{project.minecraft_version}"
                mappings "net.fabricmc:yarn:${'$'}{project.yarn_mappings}:v2"
                modImplementation "net.fabricmc:fabric-loader:${'$'}{project.loader_version}"

                // Fabric API
                modImplementation "net.fabricmc.fabric-api:fabric-api:${'$'}{project.fabric_version}"
            }

            processResources {
                inputs.property "version", project.version

                filesMatching("fabric.mod.json") {
                    expand "version": project.version
                }
            }

            tasks.withType(JavaCompile).configureEach {
                it.options.release = 17
            }

            java {
                withSourcesJar()
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            """.trimIndent()
        )

        // 4. Fabric Resources Structure
        val resourcesDir = File(root, "src/main/resources")
        resourcesDir.mkdirs()

        // fabric.mod.json
        val mainClassPath = "$packageName.${modId.replaceFirstChar { it.uppercase() }}Mod"
        File(resourcesDir, "fabric.mod.json").writeText(
            """
            {
              "schemaVersion": 1,
              "id": "$modId",
              "version": "${'$'}{version}",
              "name": "$modName",
              "description": "Created with CodeIDE Mobile Modder",
              "authors": [
                "Developer"
              ],
              "contact": {},
              "license": "MIT",
              "icon": "assets/$modId/icon.png",
              "environment": "*",
              "entrypoints": {
                "main": [
                  "$mainClassPath"
                ]
              },
              "mixins": [
                "$modId.mixins.json"
              ],
              "depends": {
                "fabricloader": ">=0.15.0",
                "minecraft": "~1.20.4",
                "java": ">=17",
                "fabric-api": "*"
              }
            }
            """.trimIndent()
        )

        // modid.mixins.json
        File(resourcesDir, "$modId.mixins.json").writeText(
            """
            {
              "required": true,
              "package": "$packageName.mixin",
              "compatibilityLevel": "JAVA_17",
              "mixins": [
                "ExampleMixin"
              ],
              "injectors": {
                "defaultRequire": 1
              }
            }
            """.trimIndent()
        )

        // Lang file
        val langDir = File(resourcesDir, "assets/$modId/lang")
        langDir.mkdirs()
        File(langDir, "en_us.json").writeText(
            """
            {
              "item.$modId.ruby": "Ruby Item",
              "block.$modId.ruby_block": "Ruby Block"
            }
            """.trimIndent()
        )

        // 5. Source Code Directories
        val packagePath = packageName.replace('.', '/')
        val javaSourceDir = File(root, "src/main/java/$packagePath")
        javaSourceDir.mkdirs()

        val className = "${modId.replaceFirstChar { it.uppercase() }}Mod"

        if (!useKotlin) {
            // Main Java Class
            File(javaSourceDir, "$className.java").writeText(
                """
                package $packageName;

                import net.fabricmc.api.ModInitializer;
                import org.slf4j.Logger;
                import org.slf4j.LoggerFactory;

                public class $className implements ModInitializer {
                    public static final String MOD_ID = "$modId";
                    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

                    @Override
                    public void onInitialize() {
                        LOGGER.info("Hello Fabric world from $modName initialized by CodeIDE!");
                        // Register your items and blocks here
                    }
                }
                """.trimIndent()
            )

            // Mixin Java Class
            val mixinDir = File(javaSourceDir, "mixin")
            mixinDir.mkdirs()
            File(mixinDir, "ExampleMixin.java").writeText(
                """
                package $packageName.mixin;

                import net.minecraft.client.gui.screen.TitleScreen;
                import org.spongepowered.asm.mixin.Mixin;
                import org.spongepowered.asm.mixin.injection.At;
                import org.spongepowered.asm.mixin.injection.Inject;
                import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

                @Mixin(TitleScreen.class)
                public class ExampleMixin {
                    @Inject(at = @At("HEAD"), method = "init")
                    private void init(CallbackInfo info) {
                        System.out.println("This message is printed by an example mod mixin generated by CodeIDE!");
                    }
                }
                """.trimIndent()
            )
        } else {
            // Kotlin Main Class
            val ktSourceDir = File(root, "src/main/kotlin/$packagePath")
            ktSourceDir.mkdirs()
            File(ktSourceDir, "$className.kt").writeText(
                """
                package $packageName

                import net.fabricmc.api.ModInitializer
                import org.slf4j.LoggerFactory

                class $className : ModInitializer {
                    companion object {
                        const val MOD_ID = "$modId"
                        val LOGGER = LoggerFactory.getLogger(MOD_ID)
                    }

                    override fun onInitialize() {
                        LOGGER.info("Hello Fabric world from Kotlin $modName!")
                    }
                }
                """.trimIndent()
            )
        }

        // 6. GitHub Actions Workflow (.github/workflows/build.yml) for Cloud Build
        val githubDir = File(root, ".github/workflows")
        githubDir.mkdirs()
        File(githubDir, "build.yml").writeText(
            """
            name: CodeIDE Cloud Build Mod

            on:
              push:
                branches: [ "main", "master" ]
              workflow_dispatch:

            jobs:
              build:
                runs-on: ubuntu-latest

                steps:
                  - name: Checkout Code
                    uses: actions/checkout@v4

                  - name: Set up JDK 17
                    uses: actions/setup-java@v4
                    with:
                      java-version: '17'
                      distribution: 'temurin'

                  - name: Setup Gradle
                    uses: gradle/actions/setup-gradle@v3

                  - name: Grant Execute Permission for Gradlew
                    run: chmod +x gradlew || true

                  - name: Build Fabric Mod JAR
                    run: ./gradlew build --no-daemon || gradle build

                  - name: Upload Mod Artifact
                    uses: actions/upload-artifact@v4
                    with:
                      name: $modId-mod-build
                      path: build/libs/*.jar
            """.trimIndent()
        )
    }
}
