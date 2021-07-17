# Redis

redis是一个非常快的非关系数据库，可以存储Key和五种不同的值（Value）之间的映射，可以将内存中的键值对数据持久化到硬盘，也可以用复制特性扩展读性能

* redis不适用表，不会预定义的强制要求用户对redis存储的不同数据进行关联

特点：因为在内存中存储，一台服务器没办法处理所有要求，所以需要主从复制特性。

##### 使用redis的理由：

* 代码更易维护，运行和相应速度更快，总体效率比关系型数据库好
* 可以避免写入不必要的临时数据，也避免对临时数据进行扫描和删除的麻烦。

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

### 简单动态字符串

当redis需要的不是一个字符串字面量，而是一个可以被修改的字符串值，就需要SDS表示字符串值，在redis数据库中，包含字符串值的键值在底层都是SDS实现的

![image-20210630181454552](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630181454552.png)

![image-20210630181509408](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630181509408.png)

![image-20210630181530663](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630181530663.png)

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

### 链表

一个列表结构可以有序的存储多个字符串

![image-20210630181707007](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630181707007.png)

![image-20210630181714297](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630181714297.png)

![image-20210630181739966](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630181739966.png)

### redis的集合（无序）

列表和集合都可以存储多个字符串，但是列表可以存储多个相同的字符串，集合则是使用散列表保证自己存储的每个字符串各不相同

![image-20210630181932170](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630181932170.png)

![image-20210630182010454](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630182010454.png)

![image-20210630182020138](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630182020138.png)

### redis的散列

redis的散列可以存储多个键值对的映射，散列表的值可以是字符串也可以是数字值

![image-20210630182243811](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630182243811.png)

![image-20210630182300587](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630182300587.png)

![image-20210630182307813](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630182307813.png)

### redis有序集合

有序集合和散列一样，存储键值对，有序集合的键被称为成员（member），每个成员各不相同，有序集合的值被称为分值（score），必须为浮点数，可以根据成员访问元素，也可以根据分值和分值的顺序访问元素结构

![image-20210630201210505](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630201210505.png)

![image-20210630201257999](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630201257999.png)

![image-20210630201328650](C:/Users/lenovo/AppData/Roaming/Typora/typora-user-images/image-20210630201328650.png)

### 字典

### 跳跃表



### 整数集合

### 压缩列表

### 对象



