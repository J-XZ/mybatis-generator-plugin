
## Config

        <plugin type="space.jxz.mybatis.generator.Plugin">
			<!-- 插件功能: 禁止getters/setters方法被重写（添加final修饰符） -->
			<property name="finalGetterSetter" value="true" />
			
			<!-- 插件功能:  在Example类中添加limit/offset字段, 用于实现分页查询 -->
			<property name="limitAndOffset" value="true" />
			
			<!-- 插件功能:  add new select methods allowing you choose which columns to select -->
			<property name="manuallySelect" value="true" />			
			
			<!-- 插件功能:  add new update methods allowing you to update like set field = field + 1 or set str = upper(str) -->
			<property name="manuallyUpdate" value="true" />
			
			<!-- 插件功能:  添加insertOrUpdateByUniqueKey()和insertSeletiveOrUpdateByUniqueKeySeletive()方法, 表有且只有一个UniqueKey(包括主键)时才会生成, UniqueKey的字段数量不限. -->
			<property name="insertOrUpdate" value="true" />
			
			<!-- 插件功能:  添加数据库的字段注释和表注释到Model类 -->
			<property name="tableAndColumnComment" value="true" />
			
			<!-- 插件功能: 为所有有自增列的表添加generatedKey, 无需指定自增列名 (mysql的自增列最多只能有一个) -->
			<property name="generatedKeyForAllTable" value="true" />	
			
			<!-- 插件功能: 如果多个<table>..</table>配置中tableName匹配了重复的表, 则只生成一次该表且使用列表中最上面的配置 -->
			<property name="retainFirstTable" value="true" />					
			
			<!-- 插件功能: 字段命名规则: 下划线(特殊字符)变驼峰, 其他字符保留原本的大小写 -->
			<property name="columnNameRule2" value="true" />
			
			<!-- 插件功能: Model类命名规则: 下划线(特殊字符)变驼峰, 其他字符保留原本的大小写 -->
			<property name="domainObjectNameRule2" value="true" />
		</plugin>

mybatis-generator-plugin
========================
The default behavior of MBG really sucks, so I made a plugin to improve it slightly. Currently only supports MySQL. Not working for constructor based models.

See my blog for more details:

[http://dfxyz.space/post/580b59fe1a6cfc30d601ab4e/](http://dfxyz.space/post/580b59fe1a6cfc30d601ab4e/)

## What does this plugin do
To put it simply, this plugin:

* for the generated models, make all fields public and removes evil getters and setters
* add limit/offset related fields and methods into the Example classes to implement pagination for `selectByExample()` method
* add new methods implementing MySQL's `insert ... on duplicate key update`
* add new select methods allowing you choose which columns to select
* add new update methods allowing you to update like `set field = field + 1` or `set str = upper(str)`

## Usage
Add my repository into your pom.xml and add it as MGB's dependency:

```xml
<pluginRepositories>
    <pluginRepository>
        <id>mybatis-generator-limit-plugin-mvn-repo</id>
        <url>https://raw.github.com/dfxyz/mybatis-generator-plugin/mvn-repo/</url>
    </pluginRepository>
</pluginRepositories>
...
<build>
    <plugins>
        <plugin>
            <groupId>org.mybatis.generator</groupId>
            <artifactId>mybatis-generator-maven-plugin</artifactId>
            <version>1.3.5</version>
            <dependencies>
                ...
                <dependency>
                    <groupId>space.jxz</groupId>
                    <artifactId>mybatis-generator-plugin</artifactId>
                    <version>1.0</version>
                </dependency>
            </dependencies>
            ...
        </plugin>
        ...
    </plugins>
</build>
```

Then add it into your generatorConfig.xml:

```xml
<generatorConfiguration>
    <context id="default" targetRuntime="MyBatis3">
        <plugin type="space.jxz.mybatis.generator.Plugin"></plugin>
        ...
    </context>
</generatorConfiguration>
```

## Using the generated object

To select with limit:

```java
// select ... limit 10, 20
XExample example = new XExample();
...
example.setLimit(20);
example.setOffset(10);
List<X> results = mapper.selectByExample(example);
```

To insert a model if it not exists or update it with raw clause:

```java
// insert ... on duplicate key update c = c + 1;
X model = new Model();
model.a = 1;
model.b = 2;
model.c = 3;
mapper.insertOrUpdateManually(model, "c = c + 1"); // or insertSelectiveOrUpdateManually()
```

If the model has a id property, it will be always set correctly after insertion or updating.

To select certain column(s) only:

```java
// select a, b ...
List<X> results = mapper.selectManuallyByExample("a, b", example);
// or
X result = mapper.selectManuallyByPrimaryKey("a, b", key);
assert result.c == null;
assert result.d == null;
```

To update with raw clause:

```java
mapper.updateManuallyByExample("int_value = int_value + 1, str_value = upper(str_value)", example);
// or
mapper.updateManuallyByPrimaryKey("int_value = int_value + 1, str_value = upper(str_value)", key);
```
