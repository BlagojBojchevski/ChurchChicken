# ChurchChicken

How to build
To get a Git project into your build add a project-level dependency:

Option - 1
1. Navigate to File > Project Structure > Dependencies.
2. Select the Module in which you’ll use the library.
3. In the Declared Dependencies tab, click add and select Jar/Aar Dependency in the dropdown.
<img width="722" alt="Screenshot 2022-10-07 at 16 28 49" src="https://user-images.githubusercontent.com/98315564/194577792-d9f44c73-a0a2-402e-ba47-adcd69b04d44.png">

4. In the Add Jar/Aar Dependency dialog, provide a path to the library ( libs/relevantsdk-aprel-release.aar ).
5. Select the configuration that requires this dependency, or select "implementation" if it applies to all configurations, and click OK.
6. Indicate in build.gradle that the library has been added. Also, these lines need to be added.

    implementation 'com.squareup.retrofit2:converter-scalars:2.3.0'
    
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${kotlin_version}"
    
    implementation 'io.reactivex.rxjava2:rxjava:2.2.21'
    
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
    
    implementation 'com.squareup.retrofit2:adapter-rxjava2:2.9.0'

    implementation 'com.squareup.okhttp3:okhttp:4.9.2'
    
    implementation 'com.squareup.okhttp3:logging-interceptor:4.9.2'
