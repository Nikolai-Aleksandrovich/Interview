连接

```
mongo "mongodb+srv://cluster0.verzb.mongodb.net/myFirstDatabase" --username root --password 18829506860
```

创建js文件

```js
print("Is the shell in interactive mode?  " + isInteractive() );
```

使用下列命令在shell中运行

```
let loadStatus = load("E:/SummerProject/haide/src/main/resources/static/javascript/test.js");
```

# JavaDriver笔记

## 连接并插入一个文档

```
ConnectionString connectionString = new ConnectionString("mongodb+srv://root:18829506860@cluster0.verzb.mongodb.net/SummerProject?retryWrites=true&w=majority");
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.build();
		MongoClient mongoClient = MongoClients.create(settings);
		MongoDatabase database = mongoClient.getDatabase("SummerProject");
		MongoCollection<Document> collection = database.getCollection("Class");
		Document doc = new Document("name", "MongoDB")
				.append("type", "database")
				.append("count", 1)
				.append("versions", Arrays.asList("v3.2", "v3.0", "v2.6"))
				.append("info", new Document("x", 203).append("y", 102));
		collection.insertOne(doc);
```

## 连接并插入一个文档列表

```
ConnectionString connectionString = new ConnectionString("mongodb+srv://root:18829506860@cluster0.verzb.mongodb.net/SummerProject?retryWrites=true&w=majority");
MongoClientSettings settings = MongoClientSettings.builder()
      .applyConnectionString(connectionString)
      .build();
MongoClient mongoClient = MongoClients.create(settings);
MongoDatabase database = mongoClient.getDatabase("SummerProject");
MongoCollection<Document> collection = database.getCollection("Class");
List<Document> documents = new ArrayList<Document>();
for (int i = 0; i < 100; i++) {
   documents.add(new Document("i", i));
}
collection.insertMany(documents);
```

## 计算数量：

```
System.out.println(collection.countDocuments());
```

## 找到集合中第一个文档

```
Document myDoc = collection.find().first();
System.out.println(myDoc.toJson());
```

## 找到集合中所有文档

```
MongoCursor<Document> cursor = collection.find().iterator();
try {
    while (cursor.hasNext()) {
        System.out.println(cursor.next().toJson());
    }
} finally {
    cursor.close();
}
```

下边这样的用法是不合法的，因为循环如果很早终止的话，可能会泄露游标

```
for (Document cur : collection.find()) {
    System.out.println(cur.toJson());
}
```

## 找到符合条件的某个文档：

```
myDoc = collection.find(eq("i", 71)).first();
System.out.println(myDoc.toJson());
```

## 找到所有符合条件的文档

```
collection.find(gt("i", 50))
        .forEach(doc -> System.out.println(doc.toJson()));
```

## 使用and操作符能更方便

```
collection.find(and(gt("i", 50), lte("i", 100)))
        .forEach(doc -> System.out.println(doc.toJson()));
```

## 更新一个单独文档

```
collection.updateOne(eq("i", 10), set("i", 110));
```

## 更新多个文档

```
UpdateResult updateResult = collection.updateMany(lt("i", 100), inc("i", 100));
System.out.println(updateResult.getModifiedCount());
```

## 删除某个文档

```
collection.deleteOne(eq("i", 110));
```

## 删除多个文档

```
DeleteResult deleteResult = collection.deleteMany(gte("i", 100));
System.out.println(deleteResult.getDeletedCount());
```

## Create Indexes

To create an index on a field or fields, pass an index specification document to the [`createIndex()`](https://mongodb.github.io/mongo-java-driver/4.2/apidocs/mongodb-driver-sync/com/mongodb/client/MongoCollection.html#createIndex(org.bson.conversions.Bson)) method. An index key specification document contains the fields to index and the index type for each field:

```java
 new Document(<field1>, <type1>).append(<field2>, <type2>) ...
```

- For an ascending index type, specify `1` for `<type>`.
- For a descending index type, specify `-1` for `<type>`.

The following example creates an ascending index on the `i` field:

```java
 collection.createIndex(new Document("i", 1));
```

# POJO

一个MongoDB的pojo实例

```
public final class Person {
    private ObjectId id;
    private String name;
    private int age;
    private Address address;

    public Person() {
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(final ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(final int age) {
        this.age = age;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }
    
    // Rest of implementation
}

public final class Address {
    private String street;
    private String city;
    private String zip;

    public Address() {
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(final String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(final String zip) {
        this.zip = zip;
    }
    
    // Rest of implementation
}
```

