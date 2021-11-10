# Mule DataSonnet Extension

The DataSonnet extension adds [DataSonnet mapping language](http://datasonnet.com) support to Mule 4.x
# Mule supported versions
Mule 4.x

# Installation 

To use the DataSonnet module in your application, add the following dependency to your `pom.xml`:

```
<groupId>org.mule.connectors</groupId>
<artifactId>mule-datasonnet-connector</artifactId>
<version>2.0.0</version>
<classifier>mule-plugin</classifier>
```
#Usage
Use the following namespace and schema location:
```
<mule ... 
     xmlns:datasonnet="http://www.mulesoft.org/schema/mule/datasonnet"
     xsi:schemaLocation=" ...
        http://www.mulesoft.org/schema/mule/datasonnet http://www.mulesoft.org/schema/mule/datasonnet/current/mule-datasonnet.xsd>
```

Declare the global config:
```
<datasonnet:config name="datasonnetconfig"/>
```
The `datasonnet:config` element has the following parameters:

| Name | Type | Description | Default Value | Required |
| ---- | ---- | ----------- | ------------- | -------- |
|`name` | String | The name for this configuration. Connectors reference the configuration with this name. | | *yes*
| `library-search-paths | List of `datasonnet:library-search-path` elements | list of directories where the connector will search for named imports (i.e. all files with extension `.libsonnet`. If not set, the connector will search in the classpath (including JARs). |  | no

## Example

```
<datasonnet:config name="datasonnetconfig_lib">
    <datasonnet:library-search-paths>
        <datasonnet:library-search-path value="./src/test/resources"/>
        <datasonnet:library-search-path value="/com/foo/bar"/>
    </datasonnet:library-search-paths>
</datasonnet:config>
```

To transform a payload using DataSonnet, use the following message processor:

```
<datasonnet:transform config-ref="datasonnetconfig" mappingFile="mytransform.ds"/>
```

The `datasonnet:transform` processor has the following attributes:

| Attribute | Mandatory | Description |
| --------- | --------- | ----------- |
| `config-ref` | yes | reference to the global config |
| `datasonnetFile` | no | name of the mapping file (must be within the application classpath) |
| `datasonnetScript` | no | string containing DataSonnet mapping script. Either `mappingFile` or `mappingScript` attribute must be provided`|
| `responseMimeType` | no | the mime type of the resulting transformation. Default is `application/json`|


# Named Imports Support
By default, named imports are resolved by scanning the application classpath and resolving the paths relative to the classpath element or to the root of the jar where the library is located.
For example, consider the following appication structure:
```
MULE_HOME
|- apps
   |- myApp
      |- mule-config.xml
      |- classes
      |  |- dslibs
      |  |  |- lib2.libsonnet
      |  |- lib1.libsonnet
      |- lib
         |- dslibs.jar      
```

The `dslibs.jar` contains libraries `lib3.libsonnet` and `morelibs/lib4.libsonnet`.

The imports section of the mapping should look like:

```
local lib1 = import 'lib1.libsonnet';
local lib2 = import 'dslibs/lib2.libsonnet';
local lib3 = import 'lib3.libsonnet';
local lib4 = import 'morelibs/lib4.libsonnet';
``` 

This behavior can be overridden by setting the `library-search-paths` nested element of the global config element. The value of this element is a list of absolute or relative paths.

# Reporting Issues

We use GitHub:Issues for tracking issues with this connector. You can report new issues at this link https://github.com/datasonnet/mule-datasonnet-connector/issues.
