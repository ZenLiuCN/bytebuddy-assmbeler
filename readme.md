# Assembler
[![](https://jitpack.io/v/ZenLiuCN/bytebuddy-assmbeler.svg)](https://jitpack.io/#ZenLiuCN/bytebuddy-assmbeler)

Utilities for use ByteBuddy to generate classes.

## Component
### Maker
Utilities to make Description for Elements.
+ TypeMaker : make `TypeDescription.Latent`
+ MethodMaker : make `MethodDescription.Latent`
+ MethodTokenMaker : make `MethodDescription.Token`
+ FieldMaker : make `FieldDescription.Latent`
+ FieldTokenMaker : make `FieldDescription.Token`
+ TypeVariableMaker : make `TypeVariableToken`
+ ParameterTokenMaker : make `ParameterDescription.Token`
+ generic(*) : a wrapper for `TypeDescription.Generic.Builder`
+ annotation(*) : a wrapper for `AnnotationDescription.Builder`
### Assembler
Utilities to build ByteCode with ASM like mode.
+ Manual: Simplest builder with only ByteCode operates.
+ Compute: Complex builder with stack compute and type trace.
### ByteBuddyPlugin and Extension
+ ByteBuddyPlugin: the implement of Byte-Buddy's `Plugin`
+ Extension: SPI for use by `ByteBuddyPlugin`

use with maven for compile time enhancement:
```xml
  <plugin>
        <groupId>net.bytebuddy</groupId>
        <artifactId>byte-buddy-maven-plugin</artifactId>
        <version>${byte-buddy-maven-plugin.version}</version>
        <executions>
            <execution>
                <id>post-compile</id>
                <phase>compile</phase>
                <goals>
                    <goal>transform</goal>
                </goals>
            </execution>
            <execution>
                <id>post-test-compile</id>
                <phase>test-compile</phase>
                <goals>
                    <goal>transform-test</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <!-- this will autoload ByteBuddyPlugin and all Extensions -->
            <classPathDiscovery>true</classPathDiscovery>
        </configuration>
    </plugin>
```

## License
GPL-2.0-only WITH Classpath-exception-2.0 (SPDX identifier)
