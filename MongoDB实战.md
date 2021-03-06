# 基于MongoDB的Web数据库

* 内部存储用二进制JSON存储，也就是BSON
* 无schema，很方便拓展数据结构
* 索引使用B-树（平衡树）的数据结构，每个集合可以创建64个索引
* 可复制集合：在多个机器上分布式存储数据，可以实现数据冗余操作、备灾操作。复制还可以伸缩数据库读操作。可复制集合由多台服务器组成，每个服务器由独立的物理机，调用这些节点，其中一个作为主节点，其余作为从节点。主从复制功能，主节点接受读、写操作，从服务器进行读操作
  * 可复制集还支持自动化备灾，主节点失效，会选择一个从节点作为主节点。

# 如何设计数据模型

### 思考以下问题：

* 存储方式：BSON还是行列
* RDBMS允许进行复杂的更新，可以在事务里包含多个更新，并且包含回滚操作和原子性，MongoDB不支持事务，但是支持在一个文档中的原子更新

* 对于一对多关系：使用内嵌文档列表

* 对于多对多关系：内嵌列表，但是列表中包含的是id

* 最好加入SEO，比如

  ```
  slug:"wheeelbarrow-9092"
  ```

## 核心概念

数据库文件：当创建mongoDB文件时，mongoDB会在磁盘上分配一系列数据库文件集合，存储在启动mongod时dbpath参数指定的目录文件夹下，默认在/data/db文件夹里存储数据：

* mongod.lock:存储服务器进程ID
* garden.ns（garden为数据库名），为命名空间，数据库的每个集合和索引的元数据都有自己的命名空间文件，组织形式时hashmap
* mongoDB还为集合和索引在文件里分配空间，进行预分配空间，这样保证操作在邻近的区域，而不是跨磁盘，持续写入会新建新的预分配空间，每次大小为之前的2倍，直到2GB结束

### 集合是文档的容器

#### 盖子集合：

有固定大小，专为日志写入而定制的集合，新写入会自动删除最先插入的集合

#### TTL集合：

检查集合中文档时间，如果与当前时间的差值超过某个值，则删除

### 文档

#### 序列化：所有文档发给MongoDB之前都需要先序列化为BSON格式，之后再从BSON反序列化

注意：不能序列化任意哈希数据结构，为什么？

要做到无错序列化，key的名字必须有效，并且每个值要可以转化为BSON类型，有效的key名字由255B长的的字符串组成，可以由任意合法的ASCII字符构成，但是不能包括$.和非结尾的null，转化为等价的字符串会带来key的长度很占空间的问题

#### 字符要进行UTF-8编码

ＢＳＯＮ数字：

可以存储ｄｏｕｂｌｅ，ｉｎｔ，ｌｏｎｇ，但是ＢＳＯＮ缺乏小数支持，没办法存储小数点

#### 文档的限制

*　ＢＳＯＮ文档的大小限制在16ＭＢ，原因在于：
  *　超过16ＭＢ的文档是设计的大的无意义
  *　发送文档给用户，需要把整个文档读出来，这很耗性能
  *　避免出现深嵌套，过深的嵌套导致查询和访问很麻烦

## ＪａｖａＳｃｒｉｐｔ查询运算符（js不能走索引）

当无法使用查询运算符进行查询的条件，可以使用ｊａｖａＳｃｒｉｐｔ，但应该至少结合一个其他查询运算符，这样会消减结果集，减少必须加载到ｊｓ上下文的文档数目，比如：

```
db.reviews.find({
	'user_id':ObjectId("456465465465"),
	'$where':"(this.rating*.92)>3"
})
```

有两个优点：首先使用了标准查询索引，其次使用了where查询，性能好

但是可能会出现JS注入攻击，用户可能会在发送的值中包含js代码

### 正则表达式不能走索引



### 更新操作中，分为目标更新和替换更新

目标更新是原子性的，可以保持一致性，因为查询和修改都在一个操作完成

替换更新可能导致竟态条件，T1进行查询，T2进行目标更新，T3进行替换更新，T1查到3，T2改为4，T3可能修改为



# 简述InnoDB双写

# 缓冲区

为啥会有两次写？必要了解partial page write 问题 : 
       InnoDB 的Page Size一般是16KB，其数据校验也是针对这16KB来计算的，将数据写入到磁盘是以Page为单位进行操作的。而计算机硬件和操作系统，写文件是以4KB作为单位的,那么每写一个innodb的page到磁盘上，在os级别上需要写4个块.通过以下命令可以查看文件系统的块大小.

在极端情况下（比如断电）往往并不能保证这一操作的原子性，16K的数据，写入4K 时，发生了系统断电/os crash ，只有一部分写是成功的，这种情况下就是 partial page write 问题。有人会想到系统恢复后MySQL可以根据redolog 进行恢复，而mysql在恢复的过程中是检查page的checksum，checksum就是pgae的最后事务号，发生partial page write 问题时，page已经损坏，找不到该page中的事务号，就无法恢复。