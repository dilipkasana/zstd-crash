# zstd-crash

Run following commands :
1. mvn clean install dependency:copy-dependencies
2. java -cp target/dependency/*:target/ZSTD-Test-0.0.1-SNAPSHOT.jar com.zstd.test.Crash



Output will be like : 
java(17563,0x700003658000) malloc: *** error for object 0x7ff0ea0dc000: pointer being freed was not allocated
java(17563,0x700003658000) malloc: *** set a breakpoint in malloc_error_break to debug
Abort trap: 6







OR 

Thread hang at native compress method.
