# 嵌入还是引用？

## 什么是范式？

数据库设计三大范式：

* 第一**范式**(确保每列保持**原子性**) 

  第一**范式**是最基本的**范式**。 如果**数据库**表中的所有字段值都是不可分解的原子值，就说明该数据库表满足了第一范式。

* 第二范式（数据的**唯一性**）(确保表中的每列都和主键相关)

  第二范式需要确保数据库表中的每一列都和主键相关，而不能只与主键的某一部分相关（主要针对联合主键而言）。也就是说在一个数据库表中，一个表中只能保存一种数据，不可以把多种数据保存在同一张数据库表中。

* 第三范式（对字段的**冗余性**）(确保每列都和主键列直接相关,而不是间接相关)

在MongoDB中，第一范式是：所有数据都是表格化的，每一个行列交汇处都有一个确切的数组，而不是关系型数据库的一个值

MongoDB的一个概念是：数据不用总是列成表格

MongoDB文档格式是使用JSON（JavaScript Object Notation）格式建模的，实际上是存在BSON（Binary JSON）中

MongoDB文档是一个键值对的字典，值可以是：

* 简单JSON类型（数字，字符串，布尔类型）
* 简单BSON类型（日期，ObjectId，UUID，正则表达式）
* 值数组
* 键值对象
* Null

### 局部性嵌入

嵌入的最大缺点是，查询的数据大于实际地需要量

数据局部性是希望嵌入一对多关系的原因，磁盘在随机寻址很费时，所以通过局部嵌入避免联合查询。

### 原子性和独立性嵌入

关系型数据库使用事务来完成原子性，MongoDB使用嵌入模式，就可以用单个操作完成原子性

**使用MongoDB，没有多文档事务，为什么设计时没有设计事务？**

底层设计时就考虑了如何简单的扩展到的多台服务器上，分布式数据库的两个问题：**分布式Join**和**分布式事务**，因为一个服务器在不可访问时会导致性能下降，那么就不支持Join和多文档事务，MongoDB可以实现自动地分片解决方案，可以获得比Join和事务更好的扩展性和性能

### 为了灵活性采用引用

一种考虑是将用户数据模型标准化到多个容器，可以提升查询的灵活性

如果应用程序需要多种方式查询数据，而且无法预计数据查询的模式，那么标准化的更好

### 为了潜在的高引用关系使用引用

当有十分高或者无法预测地引数地一对多关系时，要使用文档引用，如果要将一个文档嵌入在另外一个文档中，如果该文档过大，嵌入式会带来明显损失：

* 文档很大，使用的内存很大
* 增长的文档必然要拷贝到更大的空间中
* MongoDB的文档不能超过16MB

### 多对多的关系

多对多关系倾向于使用文档的引用，可以避免很多Join操作，可以将对象完全嵌入存储在另外一个对象中，但是在更新的时候也需要更新嵌入到该对象的文档信息，这时候应该折中，嵌入一个ID列表，而不嵌入完整的文档

## 总结

嵌入总文档的好处：

* 数据局部保存在一个文档中
* 有能力对单个文档执行原子性更新
* 但没办法处理高引用数的关系，造成性能问题

## 多态模式

MongoDB的多态模式方便父类和子类的存储，可以使用相同的集合来对所有内容结点共享的通用字段执行查询

多态模式也方便表扩展，对于关系型数据库，利用ALTER TABLE进行扩展，但在处理具有大量行数据的表，会很耗时，对于MongoDB，也可以用这种方式db.node.update，但很耗时

另一种方法是，更新应用程序来解释缺少的新字段

这样的代码即可以读取过去样式文档，也可以读取新样式文档

```
def getNodeByUrl():
	node = db.nodes.find_one({'url': url})
	node.setdefault('short_description')
	return node
```

这样可以在后台进行迁移，程序可以正常运行，同时迁移100文档：

```
def add_short_description():
	node_ids_to_migrate = db.nodes.find(
		{'short_description': {'$exists':False}}).limit(100)
	db.nodes.update(
		{'_id':{'$in':node_ids_to_migrate}},
		{'$set':{'short_description':''}},
		multi=True)
```

迁移完毕后，就可以替换程序根据URL载入节点的代码忽略默认值

```
def get_node_by_url(url):
	node = db.node.find_one({'url':url})
	return node
```

### BSON的存储效率

MongoDB的缺点是缺乏强制模式，每一个文档都要存储字段名和类型，应该减少属性名而增加值

改进方法：使用MongoDB的文档-对象映射，在应用级别的代码中，**实现很长的属性名写入只写入很短的长度**

### 多态支持半结构化的数据

可以将某个对象的公共属性作为属性名，经常更改的属性和可有可无的属性作为嵌入的子文档，如果查询过多，还可以对嵌入的文档加入索引

```
{
	_id: ObjectId(...),
	price: 4999,
	properties: {
		'Seek Time': '5ms',
		'Rotate Speed': '15k RPM'
	}
}
{
	_id: ObjectId(...),
	price: 4999,
	properties: [
		['Seek Time': '5ms'],
		['Rotate Speed': '15k RPM']
	]
}
```

增加索引：

```
db.products.ensure_index('properties')
```

## MongoDB在缺乏事务的情况下实现一致性的维护

关系型数据库标准化的一个目标：使得原子操作仅改变单行数据，当出现要更改多行数据的情况，关系型数据库使用原子多状态事务，一组更新要么都成功，或都失败，缺点是对于分布式数据库来说，这样会非常慢，**使用两段式提交协议**，可以在分布式系统中维持一致性

* 每个服务器准备执行事务，计算所有更新，并保证不出现本服务器内的一致性冲突

* 第一步完成后，每个服务器执行作为该事务的一部分的更新操作

  缺点是，在更新的过程中，将维持对一组数据的锁定，影响性能

### MongoDB混合文档实现一致性

文档模型和更新操作相结合，可以完成事务性操作。

关系型数据库用级联约束实现一句话删除命令就可以包含begin到commit

MongoDB可以实现嵌入操作，一个语句就可以删除订单

```
db.orders.remove('_id': '1123')
```

### 使用复杂更新

假如要更新，如果直接从数据库中读出文档，在内存中更新再保存回去，会出现导入订单和保存订单的静态条件，这需要自动更新数据，而不应该在应用层更新，但是在更新是会有一个风险，可能其他操作正在remove要更新的条目，必须检查返回值来检查更新操作是否成功：

```
{
	_id: '11223',
	total: 500.94,
	items: [
		{sku: '123',price: 55.11,qty: 2},
		{sku: '...',...},
	]
}
```

### 使用补偿进行更新

如果需要同时更新多个文档，多次更新，并检查是否都成功，最终确保数据库一致性

可以创建一个事务集合，来存储所有未完成的转账状态：

* 超时，则任何new状态的事务回滚
* 任何处于commited的事务都被撤销
* 人的位于rollback的事务都被撤销

事务容器包含这样的文档：

```
{
	_id: ObjectId(...),
	state: 'new',
	ts: ISODateTime(...),
	amt: 55.22,
	src: 1,
	dst: 2
}
```

账户模式更改为：

```
{_id: 1,balance: 100;txns: []}
{_id: 2,banance: 0,txns: []}
```

高层转账函数：

```
def transfer(amt,source,destination,max_txn_time):
	txn = prepare_transfer(amt,source,destination)
	commit_transfer(txn,max_txn_time)
```

在执行过程中，为转账添加一个两段式提交模型，首先准备账户，之后提交事务

```
def prepare_transfer(amt,source,destination)
	now = datetime.utcnow()
	txnid = ObjectId()
	#设置事务，时间，转账数量，状态，来去
	txn = {
		'_id': txnid,
		'state': 'new',
        'ts': datetime.utcnow(),
        'amt': amt,
        'src': source,
        'dst': destination
	}
	#存储该事务
	db.transactions.insert(txn)
	#准备账户
	#对源账户的账户模式进行减款
	result = db.accounts.update(
		{'_id': source,'balance':{'$gte':amt}},
		{'$inc':{balance-amt},
		'$push':{'txns':txn['_id']}})
	if not result['updatedExisting']:
		db.transaction.remove({'_id':txnid})
		raise InsufficientFundsError(source)
	db.accounts.update(
		{'_id':dest},
		{'$inc':{'balance':amt},
		'$push':{'txns':txn['_id']}})
```

* 原账户和目标账目存储一个挂起的事务列表，方便再账户文档中查看事务id是否处于挂起状态
* 事务再特定时间窗口完成，不然就要回滚

提交程序：

主要的目的是将事务的状态从new原子更新到commit，如果更新成功，事务退役

```
def commit_transfer(txn,max_txn_time):
	#将事务标志为已提交
	now = datetime.utcnow()
	cutoff = now - max_txn_time
	result = db.transaction.update(
		{'_id':txuid,'state':'new','ts':{'$gt':cutoff}},
		{'$set':{'state':'commit'}})
	if not result['updatedExisting']:
		raise TransactionError(txn['_id'])
	else:
		retires_transaction(txn["_id"])
```

要退役一个事务：

```
def retires_transaction(txn_id):
	db.accounts.updates(
		{'_id':txn['src'],'txns._id':txn_id},
		{'$pull':{'txns':txn_id}})
	db.accounts.updates(
		{'_id':txn['dst'],'txns._id':txn_id},
		{'$pull':{'txns':txn_id}})
	db.transaction.remove({'_id':txn_id})
	
```

在定期清理任务中处理超时的任务，以及崩溃的事务

```
def cleanup_transactions(txn,max_txn_time):
	#查找并提交部分提交的事务
	for txn in db.transaction.find({'state':'commit'},{'_id':1}):
		retire_transaction(txn['_id'])
	#将退役的事物改为回滚状态
	cutoff = new -max_txn_time
	db.transaction.update(
		{'_id':txnid,'state':'new','ts':{'$lt':cutoff}},
		{'$set':{'state':'roolback'}})
	#回滚事务
	for txn in db.transaction.find({'state':'roolback'}):
		rollback_transfer()
```

在回滚一个事务的时候，必须更新事务对象的状态，取消转账操作的影响

```
def rollback_transfer(txn):
	db.accounts.updates(
		{'_id':txn['src'],'txns._id':txn_id},
		{'$inc':{'banlance':txn['amt']},
		'$pull':{'txns':{'_id':txn['_id']}}})
	db.accounts.updates(
		{'_id':txn['src'],'txns._id':txn_id},
		{'$inc':{'banlance':-txn['amt']},
		'$pull':{'txns':{'_id':txn['_id']}}})
	db.transaction.remove({'_id':txn['_id']})
```

## 日志记录分析

模式设计：

* 选择正确的数据格式
* 只存储需要的信息

### 日志相关操作

#### 插入日志记录

主要考虑：每秒吞吐量、管理事件数据的增长

* 设置较低的write concern值

* 日志文件打包进行更新，设置db.events.insert(event,j=True)，可以再希望mongoDB再写操作的时候返回成功，而且在写操作返回之前，向磁盘的日志文件提交写操作

  也可以要求在返回之前，将数据复制到多个备用成员中：

  ```
  db.event.insert(event,w=2)
  ```

#### 批量插入

批量插入可以分散对组插入严厉的写参数的惩罚

如果不在意丢失数据，可以插入增加一个continue_on_error=True的参数，该情形下插入操作将插入尽可能多的参数，最后一次失败的时候，给出错误报告