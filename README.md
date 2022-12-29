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
    <version>0.1.0</version>
</dependency>
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.7.2</version>
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
implementation 'io.github.tranngockhoa:driver:0.1.0'
implementation 'org.seleniumhq.selenium:selenium-java:4.7.2'
```

### Java code

```java

AwesomeDriver awesomeDriver = new AwesomeDriver(true);
awesomeDriver.get("https://bot.sannysoft.com/");
```



