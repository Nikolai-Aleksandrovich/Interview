# Redis

redis是一个非常快的非关系数据库，可以存储Key和五种不同的值（Value）之间的映射，可以将内存中的键值对数据持久化到硬盘，也可以用复制特性扩展读性能

* redis不适用表，不会预定义的强制要求用户对redis存储的不同数据进行关联

特点：因为在内存中存储，一台服务器没办法处理所有要求，所以需要主从复制特性。

##### 使用redis的理由：

* 代码更易维护，运行和相应速度更快，总体效率比关系型数据库好
* 可以避免写入不必要的临时数据，也避免对临时数据进行扫描和删除的麻烦。
  * 对于长期的频繁写入的日志报告，获取固定时间范围内的聚合数据，先将各个行插入一个总表中，通过扫描行收集聚合数据，再根据扫描结果更新聚合表中已有的行
    * 原因：插入行的执行速度比更新行要快得多，更新会引起随机读和随机写

**当服务器被关闭时**，**服务器的数据在哪**？

* 时间点存储：转存操作可以在“指定时间段内有指定数量的写操作执行”这一条件满足后，执行转存。或者调用存储命令直接执行
* 只追加：用户根据数据的重要程度，将所有修改了数据库的命令都写入一个applend-only文件中，将该文件写入设置为“从不同步”“每秒同步一次”等

例子：**存储长期的报告数据**，将这些报告数据用作固定时间范围内的聚合数据（aggregates）

收集数据的常用方法：先将各行插入一个报告表中，再通过扫描这些行收集聚合数据，再用收集到的聚合数据更新聚合表中已有的行

为什么这样做？因为插入的速度比更新的速度快很多，插入行会在硬盘末尾直接写入，而更新会引起一次Random read，也可能引起一次Random write

在redis中，用户可以直接使用原子Incr命令计算聚合数据，而且因为redis将数据存在内存中，而且发给redis的命令不需要经过查询优化器与查询分析器，所以对Redis的数据执行随机写的速度很快

## 数据结构与对象：

![image-20210630175542598](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630175542598.png)

![image-20210630175552663](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630175552663.png)

### String的底层数据结构为简单动态字符串（SDS）

是**可以修改**的字符串，内部结构实现上类似于Java的ArrayList，采用**预分配冗余空间**的方式来减少内存的频繁分配.

![image-20210721180136958](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210721180136958.png)

内部为当前字符串实际分配的空间capacity一般要高于实际字符串长度len。当字符串长度小于1M时，扩容都是加倍现有的空间，如果超过1M，扩容时一次只会多扩1M的空间。需要注意的是字符串最大长度为512M。

当redis需要的不是一个字符串字面量，而是一个可以被修改的字符串值，就需要SDS表示字符串值，在redis数据库中，包含        字符串值的键值在底层都是SDS实现的

![image-20210630181454552](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630181454552.png)

![image-20210630181509408](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630181509408.png)

客户端执行命令：

```
redis> RPUSH fruits "apple" "banana" "cherry"
(integer) 3
```

那么将创建一个新的键值对在数据库：

* 键值对的键是一个字符串对象。底层实现是一个保存了字符串的“fruit”的SDS
* 键值对的值是一个列表对象，包含三个字符串对象，分别由三个SDS实现，“apple” "banana" "cherry"

#### SDS的定义

每个sds.h/sdshdr表示一个SDS值，保存空字符串的1字节不算入len属性，这样方便调用一些c字符串函数

![image-20210630164012135](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630164012135.png)

```
struct sdshdr{
	//记录buf数组使用字节长度，就是字符串长度
	int len;
	//记录还剩余多少未使用长度
	int free;
	//字节数组，保存字符串
	char buf[];
}
```

![image-20210630181625357](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630181625357.png)

### 列表的底层是快速链表

单键多值：它的底层实际是个双向链表，对两端的操作性能很高，通过索引下标的操作中间的节点性能会较差。

ziplist：首先在列表元素较少的情况下会使用一块连续的内存存储，这个结构是ziplist，也即是压缩列表。它将所有的元素紧挨着一起存储，分配的是一块连续的内存。

![image-20210721180638037](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210721180638037.png)

quicklist：当数据量比较多的时候才会改成quicklist。Redis将链表和ziplist结合起来组成了quicklist。也就是将多个ziplist使用双向指针串起来使用。这样既满足了快速的插入删除性能，又不会出现太大的空间冗余。

一个列表结构可以有序的存储多个字符串

![image-20210630181707007](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630181707007.png)

![image-20210630181714297](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630181714297.png)

### redis的集合（无序）

Redis的Set是string类型的无序集合。它底层其实是一个value为null的hash表，所以添加，删除，查找的**复杂度都是****O(1)**。

Set数据结构是dict字典，字典是用哈希表实现的。

Java中HashSet的内部实现使用的是HashMap，只不过所有的value都指向同一个对象。Redis的set结构也是一样，它的内部也使用hash结构，所有的value都指向同一个内部值。

列表和集合都可以存储多个字符串，但是列表可以存储多个相同的字符串，集合则是使用散列表保证自己存储的每个字符串各不相同

![image-20210630181932170](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630181932170.png)

### redis的散列

Redis hash是一个string类型的field和value的映射表，很适合存储对象

Hash类型对应的数据结构是两种：ziplist（压缩列表），hashtable（哈希表）。当field-value长度较短且个数较少时，使用ziplist，否则使用hashtable。

redis的散列可以存储多个键值对的映射，散列表的值可以是字符串也可以是数字值

![image-20210630182243811](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630182243811.png)

![image-20210722170320049](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210722170320049.png)

**通过 key(用户ID) + field(属性标签) 就可以操作对应属性数据了，既不需要重复存储数据，也不会带来序列化和并发修改控制的问题**

### redis有序集合

有序集合和散列一样，存储键值对，有序集合的键被称为成员（member），每个成员各不相同，有序集合的值被称为分值（score），必须为浮点数，可以根据成员访问元素，也可以根据分值和分值的顺序访问元素结构

SortedSet(zset)是Redis提供的一个非常特别的数据结构，一方面它等价于Java的数据结构Map<String, Double>，可以给每一个元素value赋予一个权重score，另一方面它又类似于TreeSet，内部的元素会按照权重score进行排序，可以得到每个元素的名次，还可以通过score的范围来获取元素的列表。

zset底层使用了两个数据结构

（1）hash，hash的作用就是关联元素value和权重score，保障元素value的唯一性，可以通过元素value找到相应的score值。

（2）跳跃表，跳跃表的目的在于给元素value排序，根据score的范围获取元素列表。

不同之处是有序集合的每个成员都关联了一个**评分（****score****）**,这个评分（score）被用来按照从最低分到最高分的方式排序集合中的成员。集合的成员是唯一的，但是评分可以是重复了 。

![image-20210630201210505](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630201210505.png)

### 跳跃表

![image-20210722171236693](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210722171236693.png)

## Redis配置文件

* Units单位:配置度量单位大小,只支持字节,
* INCLUDEs:可以把公用的配置文件提取出来

### 网络配置

* bind : 默认情况bind=127.0.0.1只能接受本机的访问请求,生产环境写服务器的地址,如果开始protected-mode,那么没有设置bind ip且没有密码的情况下,Redis只接受本机响应

  不写的情况下，无限制接受任何ip地址的访问

* **protected-mode**将本机访问保护设置为no

* port:端口号6379

* tcp-backlog : backlog其实是一个连接队列，backlog队列总和=未完成三次握手队列 + 已经完成三次握手队列。高并发需要一个高backlog避免慢客户端连接问题 , 需要增加somaxconn和tcp-backlog

* timeout:一个空闲客户端何时关闭,0永不关闭

* tcp-keepalive:对访问客户端的一种心跳检测，每个n秒检测一次。

### GENERAL通用

* daemonize:是否为后台进程，设置为yes

* pidfile:存放pid文件的位置，每个实例会产生一个不同的pid文件

* loglevel:指定日志记录级别，Redis总共支持四个级别：debug、verbose、notice、warning，默认为**notice**

  四个级别根据使用阶段来选择，生产环境选择notice 或者warning

* logfile:日志文件名称

* databases16:设定库的数量 默认16，默认数据库为0，可以使用SELECT <dbid>命令在连接上指定数据库id

### SECURITY

* 设置密码:访问密码的查看、设置和取消

  在命令中设置密码，只是临时的。重启redis服务器，密码就还原了。

  永久设置，需要再配置文件中进行设置。

### **LIMITS**

* maxclients

  * 设置redis同时可以与多少个客户端进行连接。
  *  默认情况下为10000个客户端。
  * 如果达到了此限制，redis则会拒绝新的连接请求，并且向这些连接请求方发出“max number of clients reached”以作回应

* **maxmemory**

  * Ø 建议**必须设置**，否则，将内存占满，造成服务器宕机

    Ø 设置redis可以使用的内存量。一旦到达内存使用上限，redis将会试图移除内部数据，移除规则可以通过maxmemory-policy来指定。

    Ø 如果redis无法根据移除规则来移除内存中的数据，或者设置了“不允许移除”，那么redis则会针对那些需要申请内存的指令返回错误信息，比如SET、LPUSH等。

    Ø 但是对于无内存申请的指令，仍然会正常响应，比如GET等。如果你的redis是主redis（说明你的redis有从redis），那么在设置内存使用上限时，需要在系统中留出一些内存空间给同步队列缓存，只有在你设置的是“不移除”的情况下，才不用考虑这个因素。

* **maxmemory-policy**

  * Ø volatile-lru：使用LRU算法移除key，只对设置了过期时间的键；（最近最少使用）

    Ø allkeys-lru：在所有集合key中，使用LRU算法移除key

    Ø volatile-random：在过期集合中移除随机的key，只对设置了过期时间的键

    Ø allkeys-random：在所有集合key中，移除随机的key

    Ø volatile-ttl：移除那些TTL值最小的key，即那些最近要过期的key

    Ø noeviction：不进行移除。针对写操作，只是返回错误信息

## **.**  **Redis**新数据类型

### Bitmaps

现代计算机用二进制（位） 作为信息的基础单位， 1个字节等于8位， 例如“abc”字符串是由3个字节组成， 但实际在计算机存储时将其用二进制表示， “abc”分别对应的ASCII码分别是97、 98、 99， 对应的二进制分别是01100001、 01100010和01100011，如下图![image-20210722193707977](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210722193707977.png)

合理地使用操作位能够有效地提高内存使用率和开发效率。

   Redis提供了Bitmaps这个“数据类型”可以实现对位的操作：

（1）  Bitmaps本身不是一种数据类型， 实际上它就是字符串（key-value） ， 但是它可以对字符串的位进行操作。

（2）  Bitmaps单独提供了一套命令， 所以在Redis中使用Bitmaps和使用字符串的方法不太相同。 可以把Bitmaps想象成一个以位为单位的数组， 数组的每个单元只能存储0和1， 数组的下标在Bitmaps中叫做偏移量。

![image-20210722195022427](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210722195022427.png)

### HyperLogLog

求集合中不重复元素个数的问题称为基数问题。

解决基数问题有很多种方案：

（1）数据存储在MySQL表中，使用distinct count计算不重复个数

（2）使用Redis提供的hash、set、bitmaps等数据结构来处理

以上的方案结果精确，但随着数据不断增加，导致占用空间越来越大，对于非常大的数据集是不切实际的。

能否能够降低一定的精度来平衡存储空间？Redis推出了HyperLogLog

Redis HyperLogLog 是用来做基数统计的算法，HyperLogLog 的优点是，在输入元素的数量或者体积非常非常大时，计算基数所需的空间总是固定的、并且是很小的。

在 Redis 里面，每个 HyperLogLog 键只需要花费 12 KB 内存，就可以计算接近 2^64 个不同元素的基数。这和计算基数时，元素越多耗费内存就越多的集合形成鲜明对比。

但是，因为 HyperLogLog 只会根据输入元素来计算基数，而不会储存输入元素本身，所以 HyperLogLog 不能像集合那样，返回输入的各个元素。

 

什么是基数?

比如数据集 {1, 3, 5, 7, 5, 7, 8}， 那么这个数据集的基数集为 {1, 3, 5 ,7, 8}, 基数(不重复元素)为5。 基数估计就是在误差可接受的范围内，快速计算基数。

## Redis事务

Redis事务是一个单独的隔离操作：事务中的所有命令都会序列化、按顺序地执行。事务在执行的过程中，不会被其他客户端发送来的命令请求所打断。

Redis事务的主要作用就是串联多个命令防止别的命令插队。

### Multi,discard,Exec

![image-20210722203317336](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210722203317336.png)

### 事务的错误处理

组队中某个命令出现了报告错误，执行时整个的所有队列都会被取消。触发discard

如果执行阶段某个命令报出了错误，则只有报错的命令不会被执行，而其他的命令都会执行，不会回滚

Redis就是利用这种check-and-set乐观锁机制实现事务的。

### **WATCH** **key** **[key ...]**

在执行multi之前，先执行watch key1 [key2],可以监视一个(或多个) key ，如果在事务**执行之前这个(****或这些) key** **被其他命令所改动，那么事务将被打断。**

### unwatch

取消 WATCH 命令对所有 key 的监视。

如果在执行 WATCH 命令之后，EXEC 命令或DISCARD 命令先被执行了的话，那么就不需要再执行UNWATCH 了。

### **Redis****事务三特性**

* **单独的隔离操作**:事务中的所有命令都会序列化、按顺序地执行。事务在执行的过程中，不会被其他客户端发送来的命令请求所打断。

* **没有隔离级别的概念**: 队列中的命令没有提交之前都不会实际被执行，因为事务提交前任何指令都不会被实际执行

*  不保证原子性 

   事务中如果有一条命令执行失败，其后的命令仍然会被执行，没有回滚 

## 秒杀中的情况

### 超卖

利用乐观锁,设置版本号即可

### 已经秒杀完,但还是有库存

原因:乐观锁导致请求失败,先点的没买到,后点的买到了

### 连接超时

利用连接池解决:节省每次连接redis服务带来的消耗，把连接好的实例反复利用,通过参数管理连接的行为

* ### 链接池参数
  * MaxTotal：控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；如果赋值为-1，则表示不限制；如果pool已经分配了MaxTotal个jedis实例，则此时pool的状态为exhausted。
  *  maxIdle：控制一个pool最多有多少个状态为idle(空闲)的jedis实例；
  *  MaxWaitMillis：表示当borrow一个jedis实例时，最大的等待毫秒数，如果超过等待时间，则直接抛JedisConnectionException；
  * testOnBorrow：获得一个jedis实例的时候是否检查连接可用性（ping()）；如果为true，则得到的jedis实例均是可用的；

### **  **LUA****脚本在****Redis****中的优势**

将复杂的或者多步的redis操作，写为一个脚本，一次提交给redis执行，减少反复连接redis的次数。提升性能。

LUA脚本是类似redis事务，有一定的原子性，不会被其他命令插队，可以完成一些redis事务性的操作。

但是注意redis的lua脚本功能，只有在Redis 2.6以上的版本才可以使用。

利用lua脚本淘汰用户，解决超卖问题。

redis 2.6版本以后，通过lua脚本解决**争抢问题**，实际上是**redis** **利用其单线程的特性，用任务队列的方式解决多任务并发问题**。

![image-20210722205815761](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210722205815761.png)

## Redis持久化

提供了2个不同的持久化方法

* RDB（Redis DataBase）
* AOF（Append Of File）

### 对于RDB

在指定的时间间隔内将内存中的数据集快照写入磁盘， 也就是行话讲的Snapshot快照，它恢复时是将快照文件直接读到内存里

**备份是如何执行的**

Redis会单独创建（fork）一个子进程来进行持久化，会先将数据写入到 一个临时文件中，待持久化过程都结束了，再用这个临时文件替换上次持久化好的文件。 整个过程中，主进程是不进行任何IO操作的，这就确保了极高的性能 如果需要进行大规模数据的恢复，且对于数据恢复的完整性不是非常敏感，那RDB方式要比AOF方式更加的高效。**RDB****的缺点是最后一次持久化后的数据可能丢失**。

### fork

l Fork的作用是复制一个与当前进程一样的进程。新进程的所有数据（变量、环境变量、程序计数器等） 数值都和原进程一致，但是是一个全新的进程，并作为原进程的子进程

l 在Linux程序中，fork()会产生一个和父进程完全相同的子进程，但子进程在此后多会exec系统调用，出于效率考虑，Linux中引入了“**写时复制技术**”

l **一般情况父进程和子进程会共用同一段物理内存**，只有进程空间的各段的内容要发生变化时，才会将父进程的内容复制一份给子进程。

**RDB****持久化流程**

![image-20210722212919505](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210722212919505.png)

 **dump.rdb****文件**

在redis.conf中配置文件名称，默认为dump.rdb

   **命令****save VS bgsave**

save ：save时只管保存，其它不管，全部阻塞。手动保存。不建议。 

**bgsave****：****Redis****会在后台异步进行快照操作，** **快照同时还可以响应客户端请求。**

可以通过lastsave 命令获取最后一次成功执行快照的时间

 **flushall****命令**

执行flushall命令，也会产生dump.rdb文件，但里面是空的，无意义

### **SNAPSHOTTING**

​     **Save**

格式：save 秒钟 写操作次数

  **stop-writes-on-bgsave-error**

当Redis无法写入磁盘的话，直接关掉Redis的写操作。推荐yes.

   **rdbcompression** **压缩文件**

对于存储到磁盘中的快照，可以设置是否进行压缩存储。如果是的话，redis会采用LZF算法进行压缩。

如果你不想消耗CPU来进行压缩的话，可以设置为关闭此功能。推荐yes.

  **rdbchecksum** **检查完整性**

在存储快照后，还可以让redis使用CRC64算法来进行数据校验，

但是这样做会增加大约10%的性能消耗，如果希望获取到最大的性能提升，可以关闭此功能

推荐yes.

​     **rdb****的备份**

先通过config get dir 查询rdb文件的目录 

将*.rdb的文件拷贝到别的地方

rdb的恢复

u 关闭Redis

u 先把备份的文件拷贝到工作目录下 cp dump2.rdb dump.rdb

u 启动Redis, 备份数据会直接加载

**优势**

l 适合大规模的数据恢复

l 对数据完整性和一致性要求不高更适合使用

l 节省磁盘空间

l 恢复速度快

**劣势**

l Fork的时候，内存中的数据被克隆了一份，大致2倍的膨胀性需要考虑

l 虽然Redis在fork时使用了**写时拷贝技术**,但是如果数据庞大时还是比较消耗性能。

l 在备份周期在一定间隔时间做一次备份，所以如果Redis意外down掉的话，就会丢失最后一次快照后的所有修改。

![image-20210722214035474](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210722214035474.png)

##   **Redis****持久化之****AOF**

**AOF（Append Only File）**

以**日志**的形式来记录每个写操作（增量保存），将Redis执行过的所有写指令记录下来(**读操作不记录**)， **只许追加文件但不可以改写文件**，redis启动之初会读取该文件重新构建数据，换言之，redis 重启的话就根据日志文件的内容将写指令从前到后执行一次以完成数据的恢复工作

**AOF持久化流程**

（1）客户端的请求写命令会被append追加到AOF缓冲区内；

（2）AOF缓冲区根据AOF持久化策略[always,everysec,no]将操作sync同步到磁盘的AOF文件中；

（3）AOF文件大小超过重写策略或手动重写时，会对AOF文件rewrite重写，压缩AOF文件容量；

（4）Redis服务重启时，会重新load加载AOF文件中的写操作达到数据恢复的目的；

**AOF****默认不开启**

可以在redis.conf中配置文件名称，默认为 appendonly.aof

AOF文件的保存路径，同RDB的路径一致。

**AOF****和****RDB****同时开启，****redis****听谁的？**

AOF和RDB同时开启，系统默认取AOF的数据（数据不会存在丢失）

****  **AOF****启动****/****修复****/****恢复**

l AOF的备份机制和性能虽然和RDB不同, 但是备份和恢复的操作同RDB一样，都是拷贝备份文件，需要恢复时再拷贝到Redis工作目录下，启动系统即加载。

l 正常恢复

n 修改默认的appendonly no，改为yes

n 将有数据的aof文件复制一份保存到对应目录(查看目录：config get dir)

n 恢复：重启redis然后重新加载

 

l 异常恢复

n 修改默认的appendonly no，改为yes

n 如遇到**AOF****文件损坏**，通过/usr/local/bin/**redis-check-aof--fix appendonly.aof**进行恢复

n 备份被写坏的AOF文件

n 恢复：重启redis，然后重新加载

 **AOF****同步频率设置**

appendfsync always

始终同步，每次Redis的写入都会立刻记入日志；性能较差但数据完整性比较好

appendfsync everysec

每秒同步，每秒记入日志一次，如果宕机，本秒的数据可能丢失。

appendfsync no

redis不主动进行同步，把同步时机交给操作系统。

* **Rewrite****压缩**

1是什么：

AOF采用文件追加方式，文件会越来越大为避免出现此种情况，新增了重写机制, 当AOF文件的大小超过所设定的阈值时，Redis就会启动AOF文件的内容压缩， 只保留可以恢复数据的最小指令集.可以使用命令bgrewriteaof

2重写原理，如何实现重写

AOF文件持续增长而过大时，会fork出一条新进程来将文件重写(也是先写临时文件最后再rename)，redis4.0版本后的重写，是指上就是把rdb 的快照，以二级制的形式附在新的aof头部，作为已有的历史数据，替换掉原来的流水账操作。

no-appendfsync-on-rewrite：

如果 no-appendfsync-on-rewrite=yes ,不写入aof文件只写入缓存，用户请求不会阻塞，但是在这段时间如果宕机会丢失这段时间的缓存数据。（降低数据安全性，提高性能）

   如果 no-appendfsync-on-rewrite=no, 还是会把数据往磁盘里刷，但是遇到重写操作，可能会发生阻塞。（数据安全，但是性能降低）

触发机制，何时重写

Redis会记录上次重写时的AOF大小，默认配置是当AOF文件大小是上次rewrite后大小的一倍且文件大于64M时触发

重写虽然可以节约大量磁盘空间，减少恢复时间。但是每次重写还是有一定的负担的，因此设定Redis要满足一定条件才会进行重写。 

auto-aof-rewrite-percentage：设置重写的基准值，文件达到100%时开始重写（文件是原来重写后文件的2倍时触发）

auto-aof-rewrite-min-size：设置重写的基准值，最小文件64MB。达到这个值开始重写。

例如：文件达到70MB开始重写，降到50MB，下次什么时候开始重写？100MB

系统载入时或者上次重写完毕时，Redis会记录此时AOF大小，设为base_size,

如果Redis的AOF当前大小>= base_size +base_size*100% (默认)且当前大小>=64mb(默认)的情况下，Redis会对AOF进行重写。 

3、重写流程

（1）bgrewriteaof触发重写，判断是否当前有bgsave或bgrewriteaof在运行，如果有，则等待该命令结束后再继续执行。

（2）主进程fork出子进程执行重写操作，保证主进程不会阻塞。

（3）子进程遍历redis内存中数据到临时文件，客户端的写请求同时写入aof_buf缓冲区和aof_rewrite_buf重写缓冲区保证原AOF文件完整以及新AOF文件生成期间的新的数据修改动作不会丢失。

（4）1).子进程写完新的AOF文件后，向主进程发信号，父进程更新统计信息。2).主进程把aof_rewrite_buf中的数据写入到新的AOF文件。

（5）使用新的AOF文件覆盖旧的AOF文件，完成AOF重写。

![image-20210722220709916](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210722220709916.png)

**优势**

![image-20210722221101124](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210722221101124.png)

n 备份机制更稳健，丢失数据概率更低。

n 可读的日志文本，通过操作AOF稳健，可以处理误操作。

****  **劣势**

n 比起RDB占用更多的磁盘空间。

n 恢复备份速度要慢。

n 每次读写都同步的话，有一定的性能压力。

n 存在个别Bug，造成恢复不能。

![image-20210722221138827](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210722221138827.png)

**1.1.1.**  **用哪个好**

官方推荐两个都启用。

如果对数据不敏感，可以选单独用RDB。

不建议单独用 AOF，因为可能会出现Bug。

如果只是做纯内存缓存，可以都不用。

l RDB持久化方式能够在指定的时间间隔能对你的数据进行快照存储

l AOF持久化方式记录每次对服务器写的操作,当服务器重启的时候会重新执行这些命令来恢复原始的数据,AOF命令以redis协议追加保存每次写的操作到文件末尾. 

l Redis还能对AOF文件进行后台重写,使得AOF文件的体积不至于过大

l 只做缓存：如果你只希望你的数据在服务器运行的时候存在,你也可以不使用任何持久化方式.

l 同时开启两种持久化方式

l 在这种情况下,当redis重启的时候会优先载入AOF文件来恢复原始的数据, 因为在通常情况下AOF文件保存的数据集要比RDB文件保存的数据集要完整.

l RDB的数据不实时，同时使用两者时服务器重启也只会找AOF文件。那要不要只使用AOF呢？ 

l 建议不要，因为RDB更适合用于备份数据库(AOF在不断变化不好备份)， 快速重启，而且不会有AOF可能潜在的bug，留着作为一个万一的手段。

l 性能建议

如果使用AOF，好处是在最恶劣情况下也只会丢失不超过两秒数据，启动脚本较简单只load自己的AOF文件就可以了。

代价,一是带来了持续的IO，二是AOF rewrite的最后将rewrite过程中产生的新数据写到新文件造成的阻塞几乎是不可避免的。

只要硬盘许可，应该尽量减少AOF rewrite的频率，AOF重写的基础大小默认值64M太小了，可以设到5G以上。

默认超过原大小100%大小时重写可以改到适当的数值

##  **Redis_****主从复制**

主机数据更新后根据配置和策略， 自动同步到备机的master/slaver机制，**Master****以写为主，****Slave****以读为主**

作用:

l 读写分离，性能扩展

l 容灾快速恢复

### 如何配置主从复制

拷贝多个redis.conf文件include(写绝对路径)

开启daemonize yes

Pid文件名字pidfile

指定端口port

Log文件名字

dump.rdb名字dbfilename

Appendonly 关掉或者换名字

**1.1.1.**  **新建redis6379.conf，填写以下内容**

include /myredis/redis.conf

pidfile /var/run/redis_6379.pid

port 6379

dbfilename dump6379.rdb

​                               

**1.1.2.**  **新建redis6380.conf，填写以下内容**

 include /myredis/redis.conf

pidfile /var/run/redis_6379.pid

port 6379

dbfilename dump6379.rdb

**1.1.3.**  **新建redis6381.conf，填写以下内容**

 include /myredis/redis.conf

pidfile /var/run/redis_6379.pid

port 6379

dbfilename dump6379.rdb

slave-priority 10

设置从机的优先级，值越小，优先级越高，用于选举主机时使用。默认100

**1.1.1.**  **启动三台redis服务器**

**1.1.1.**  **查看三台主机运行情况**

**1.1.1.**  **配从(库****)****不配主****(****库****)** 

slaveof <ip><port>

成为某个实例的从服务器

1、在6380和6381上执行: slaveof 127.0.0.1 6379

2、在主机上写，在从机上可以读取数据

在从机上写数据报错

​                               

3、主机挂掉，重启就行，一切如初

4、从机重启需重设：slaveof 127.0.0.1 6379

可以将配置增加到文件中。永久生效。

## **1.1.**  **常用****3****招**

**1.1.1.**  **一主二仆**

切入点问题？slave1、slave2是从头开始复制还是从切入点开始复制?比如从k4进来，那之前的k1,k2,k3是否也可以复制？

从机是否可以写？set可否？ 

主机shutdown后情况如何？从机是上位还是原地待命？

主机又回来了后，主机新增记录，从机还能否顺利复制？ 

其中一台从机down后情况如何？依照原有它能跟上大部队吗？

### 迭代传递

上一个Slave可以是下一个slave的Master，Slave同样可以接收其他 slaves的连接和同步请求，那么该slave作为了链条中下一个的master, 可以有效减轻master的写压力,去中心化降低风险。

用 slaveof <ip><port>

中途变更转向:会清除之前的数据，重新建立拷贝最新的

风险是一旦某个slave宕机，后面的slave都没法备份

主机挂了，从机还是从机，无法写数据了

### 反客为主

当一个master宕机后，后面的slave可以立刻升为master，其后面的slave不用做任何修改。

用 slaveof no one  将从机变为主机。

## **1.1.**  **复制原理**

l Slave启动成功连接到master后会发送一个sync命令

l Master接到命令启动后台的存盘进程，同时收集所有接收到的用于修改数据集命令， 在后台进程执行完毕之后，master将传送整个数据文件到slave,以完成一次完全同步

l 全量复制：而slave服务在接收到数据库文件数据后，将其存盘并加载到内存中。

l 增量复制：Master继续将新的所有收集到的修改命令依次传给slave,完成同步

l 但是只要是重新连接master,一次完全同步（全量复制)将被自动执行

**1.1.**  **哨兵模式****(sentinel)**

**反客为主的自动版**，能够后台监控主机是否故障，如果故障了根据投票数自动将从库转换为主库

**1.1.1.1.**      **复制延时**

由于所有的写操作都是先在Master上操作，然后同步更新到Slave上，所以从Master同步到Slave机器有一定的延迟，当系统很繁忙的时候，延迟问题会更加严重，Slave机器数量的增加也会使这个问题更加严重。

**故障恢复**

优先级在redis.conf中默认：slave-priority 100，值越小优先级越高

偏移量是指获得原主机数据最全的

每个redis实例启动后都会随机生成一个40位的runid

## **Redis****集群**

容量不够，redis如何进行扩容？

并发写操作， redis如何分摊？

另外，主从模式，薪火相传模式，主机宕机，导致ip地址发生变化，应用程序中配置需要修改对应的主机地址、端口等信息。

之前通过代理主机来解决，但是redis3.0中提供了解决方案。就是无中心化集群配置。

### 集群

Redis 集群实现了对Redis的水平扩容，即启动N个redis节点，将整个数据库分布存储在这N个节点中，每个节点存储总数据的1/N。

Redis 集群通过分区（partition）来提供一定程度的可用性（availability）： 即使集群中有一部分节点失效或者无法进行通讯， 集群也可以继续处理命令请求。