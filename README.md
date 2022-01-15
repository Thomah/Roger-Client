# Roger

## Create a Windows service with WinSW

Create a file `Roger.xml` with the following content :

```xml
<service>
    <id>Roger</id>
    <name>Roger</name>
    <description>Roger</description>
    <executable>JAVA_HOME\bin\java</executable>
    <arguments>-Xmx256m -jar "ROGER_HOME\target\roger-1.0.0.jar"</arguments>
    <logmode>rotate</logmode>
</service>
```

> Replace JAVA_HOME and ROGER_HOME with appropriate values

In the same repository, download the `WinSW` executable from the [latest release](https://github.com/winsw/winsw/releases) and rename it `Roger.exe`

Open a command line prompt and run :

```cmd
.\Roger.exe install
```

On the next boot, the App will start automatically. If you want to run it now, open the Windows Services app and clic "Start"
