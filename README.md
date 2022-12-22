# Awesome Driver

## Usage

### Add dependency

```xml
<repositories>
    <repository>
        <id>sonatype</id>
        <name>Sona Type</name>
        <url>https://s01.oss.sonatype.org/content/repositories/releases</url>
    </repository>
</repositories>
...
<dependency>
    <groupId>io.github.tranngockhoa</groupId>
    <artifactId>driver</artifactId>
    <version>0.0.2</version>
</dependency>
```

or

```groovy
repositories {
    mavenCentral()
    maven {
        url 'https://s01.oss.sonatype.org/content/repositories/releases'
    }
}

...
implementation 'io.github.tranngockhoa:driver:0.0.2'
```

### Java code

```java

AwesomeDriver awesomeDriver = new AwesomeDriver(true);
awesomeDriver.get("https://bot.sannysoft.com/");
```



