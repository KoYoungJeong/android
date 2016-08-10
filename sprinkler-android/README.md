## How to Build

### 1 build
```
$> ./gradlew :sprin:clean :sprin:install
```

### 2. output

sprinkler-android/build/

> libs/ javadoc.jar, sources.jar
> outputs/aar/*.aar
> poms/pom-default.xml -- pom.xml 으로 이름 변경

### 3. 사내 넥서스로 배포

```
http://nx.jandi.io:8081/nexus/#view-repositories
```

위의 총 4개 파일을 사내 넥서스로 업로드함

