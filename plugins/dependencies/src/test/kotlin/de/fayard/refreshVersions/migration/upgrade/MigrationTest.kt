package de.fayard.refreshVersions.migration.upgrade

import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language

class MigrationTest : StringSpec({

    "Ignore lines that do not contain version" {
        val lines = """
            plugins {
                kotlin("jvm")
            }

            version = "2"
            group = "de.fayard"

            repositories {
                mavenCentral()
            }

        """.trimIndent()
        lines.lines().forAll { line -> replaceVersionWithUndercore(line) shouldBe null }
    }

    "Replace version with underscore" {
        val input = """
            val a = "1.3"
            val b = "1.2.3"
            implementation("com.example:name:1.2.3")
            implementation(group : "com.example" name: "name" version :"1.2.3")
            implementation('com.example:name:1.2.3')
            implementation("com.example:name:${'$'}exampleVersion")
            implementation("com.example:name:${'$'}version")
            implementation('com.example:name:${'$'}exampleVersion')
            implementation('com.example:name:${'$'}version')
            implementation('com.example:name:1.2.3-alpha1')
            implementation('com.example:name:1.2.3-alpha-1')
            implementation('com.example:name:1.2.3.alpha.1')
            implementation('com.example:name:1.2.3-beta-1')
            implementation('com.example:name:1.2.3.beta.1')
            implementation('com.example:name:1.2.3.beta1')
            implementation('com.example:name:1.2.3-eap-1')
            implementation('com.example:name:1.2.3.eap.1')
            implementation('com.example:name:1.2.3.eap1')
        """.trimIndent().lines()
        val expected = """
            val a = "_"
            val b = "_"
            implementation("com.example:name:_")
            implementation(group : "com.example" name: "name" version :"_")
            implementation('com.example:name:_')
            implementation("com.example:name:_")
            implementation("com.example:name:_")
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
            implementation('com.example:name:_')
        """.trimIndent().lines()
        input.size shouldBeExactly expected.size
        List(input.size) { input[it] to expected[it] }
            .forAll { (input, output) ->
                replaceVersionWithUndercore(input) shouldBe output
            }
    }

})


@Language("RegExp")
val underscoreRegex =
    "(['\":])(?:\\\$\\w+ersion|(?:\\d+\\.){1,2}\\d+)(?:[.-]?(?:alpha|beta|rc|eap)[-.]?\\d+)?([\"'])".toRegex()

fun replaceVersionWithUndercore(line: String): String? = when {
    underscoreRegex.containsMatchIn(line) -> line.replace(underscoreRegex, "\$1_\$2")
    else -> null
}
