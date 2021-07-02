# MongoDB学习手册

### 连接数据库

### 切换数据库

使用db展现当前数据库

```
db
```

使用use ***切换数据库，就算没有数据库，在切换时MongoDB也会创建

```
use examples
```

##### 在数据库中插入

首先切换到新的数据库中

```
use gettingStarted
```

其次，插入文档，在gettingStarted中创建一个新的集合，名字是people

```
db.people.insert({
  name: { first: "Alan", last: "Turing" },
  birth: new Date('Jun 23, 1912'),
  death: new Date('Jun 07, 1954'),
  contribs: [ "Turing machine", "Turing test", "Turingery" ],
  views : NumberLong(1250000)
})
```

会输出结果

```
WriteResult({ "nInserted" : 1 })
```

##### 查看数据

```
db.people.find({ "name.last": "Turing" })
```

运行后会查看输出

```
{
  "_id" : ObjectId("5c9bca3c5345268c98ac7abc"),
  "name" : {
    "first" : "Alan",
    "last" : "Turing"
  },
  "birth" : ISODate("1912-06-23T04:00:00Z"),
  "death" : ISODate("1954-06-07T04:00:00Z"),
  "contribs" : [
    "Turing machine",
    "Turing test",
    "Turingery"
  ],
  "views" : NumberLong(1250000)
}
```

## 迁移或者导入数据到集群上

可以将现存的MongoDB服务器上的数据、JSON、CSV文件使用迁移工具转到Atlas上

### 选择一款迁移工具

* 对于在MongoDB集群上的重复数据集（具有高可用性，常见性）导入策略：
  * 使用Live Migrate
  * 必须给Live Migrate提供主机名字
  * 之前的MongoDB服务器的版本要先升级到2.6之后
  * 要有之前服务器的oplog的访问权
* 对于一个分片集群的多个分片
  * 如果应用可以承受短时间的停机，就可以使用Live Migrate
  * 之前的服务器必须有MongoDB3.6的版本
  * 如果可以承受停机，而且分片集群并没有相对应的MongoDB，那可以用MongoDump和MongoRestore
* 对一个单片的分片集群
  * 使用Live Migrate
* 对一个标准的MongoDB结点
  * 把标准节点转化为单节点的复制集，然后使用Live Migrate

### 数据的实时迁移

可以使用Atlas的用户接口迁移数据，JSON，csv

##### 实时迁移副本集（Replica Set）到Atlas

Atlas 可以将源副本集实时迁移到 Atlas 集群，使集群与远程源保持同步，直到将应用程序切换到 Atlas 集群。 到达以下过程中的转换步骤后，应该停止写入源集群，方法是停止应用程序实例，将它们指向 Atlas 集群，然后重新启动它们。

![The ellipsis button appears beneath the cluster name.](https://docs.atlas.mongodb.com/images/live-migrate-blurred-region.png)

Group of MongoDB servers that maintain the same data set. Replica sets provide redundancy, high availability, and are the basis for all production deployments.

#### Load File with `mongoimport`

You can use [`mongoimport`](https://docs.mongodb.com/database-tools/mongoimport/#mongodb-binary-bin.mongoimport) or a `CSV` file into MongoDB Atlas cluster.

方法：

* 在目标Atlas 集群上设置数据库用户（读写）

  1. In the **Security** section of the left navigation, click **Database Access**. The **Database Users** tab displays.
  2. Click **Add New Database User**.
  3. Add an **Atlas admin** user.

* 打开连接对话

  From the **Clusters** view, click **Connect** for the Atlas cluster into which you want to migrate data.

* 更新IP权限列表

  - The public IP address of the server on which [`mongoimport`](https://docs.mongodb.com/database-tools/mongoimport/#mongodb-binary-bin.mongoimport) will run, or
  - If set up for VPC peering, either the peer's VPC CIDR block (or a subnet) or the peer VPC's Security Group, if you chose AWS as your cloud provider.

* 复制目标肌群的URI/host信息

  使用连接字符串URI，连接到你的Atlas集群

  1. Click **Connect Your Application**.

  2. Copy the connection string found in step 1.

  3. Replace **PASSWORD** with the password for the root user, and **DATABASE** with the name of the database to which you wish to connect.

     

     This connection string is specified to [`mongoimport`](https://docs.mongodb.com/database-tools/mongoimport/#mongodb-binary-bin.mongoimport) in the `--uri` option.

     When using `--host`, if the Atlas cluster is a replica set you must also retrieve the replica set name. For example:

     ```
     myAtlasRS/atlas-host1:27017,atlas-host2:27017,atlas-host3:27017
     ```

* **Run mongoimport**

  The following example imports data from the file `/somedir/myFileToImport.json` into collection `myData` in the `testdb` database. The operation includes the `--drop` option to drop the collection first if the collection exists.

  Using `--uri`:

  ```
  mongoimport --uri "mongodb://root:<PASSWORD>@atlas-host1:27017,atlas-host2:27017,atlas-host3:27017/<DATABASE>?ssl=true&replicaSet=myAtlasRS&authSource=admin" --collection myData --drop --file /somedir/myFileToImport.json
  ```

  

  Using `--host`:

  ```
  mongoimport --host myAtlasRS/atlas-host1:27017,atlas-host2:27017,atlas-host3:27017 --ssl -u myAtlasAdminUser -p 'myAtlasPassword' --authenticationDatabase admin  --db testdb --collection myData --drop --file /somedir/myFileToImport.json
  ```

  

  Add/edit the [`mongoimport`](https://docs.mongodb.com/database-tools/mongoimport/#mongodb-binary-bin.mongoimport) command line options as appropriate for your deployment. See [`mongoimport`](https://docs.mongodb.com/database-tools/mongoimport/#mongodb-binary-bin.mongoimport) for more [`mongoimport`](https://docs.mongodb.com/database-tools/mongoimport/#mongodb-binary-bin.mongoimport) options.

### 创建搜索索引

