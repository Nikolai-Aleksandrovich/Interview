# 名词：

消息：类似一条记录或一行表，由字节数组组成

键：消息的可选元数据，是一个字节数组，用于为消息选择分区（对键生成一致性散列，在对分区数量取模，决定这个消息该去哪个分区）

主题：消息通过主题进行分类，类似数据库的表，或者文件系统的文件夹，主题分为若干个分区

分区：一个分区是一个提交日志，消息追加写入分区，无法在主题范围保持消息的顺序，但可以保证消息在分区内的顺序

批次：为了提高效率，消息分批次写入kafka

偏移量：一个不断递增的整数值，创建消息时，kafka会把偏移量写入，消费者读取的时候，偏移量也会保存，断连后依然可以继续

broker：一个独立的kafka服务器是broker，接收来自生产者的消息，为消息设置偏移量，然后放到磁盘保存，为消费者提供服务，对读取的分区做出响应，返回消息

集群：broker是集群的组成部分，每个集群都有一个broker充当集群控制器的角色，负责管理工作，将分区分配给broker和监控broker，一个分区从属于一个broker，那么他就是分区的首领，如果一个分区属于多个broker，那么就是发生分区复制，提供了消息冗余。

# Kafka生产者

### 生产者的发送方式：

* 创建一个ProducerRecord对象，要指定目标主题和要发送的内容，键和分区是可选的。
* 发送ProducerRecord对象时，要把键和值都转化为字节数组才可以发送。
* 数据传给分区器，如果已经被指定了分区，那么直接返回给指定的分区，如果没有，那么会根据ProducerRecord对象的键计算对应的分区
* 接下来，这条消息被记录在一个批次中，这个批次的所有消息都会发送到一个主题的相同分区下，有一个独立的线程负责发送这些记录批次到相应的broker
* 服务器收到消息会返回一个响应，若成功写入，则返回RecodeMetaData对象，包含了主题和分区信息，以及偏移量。如果写入失败，就会返回错误，生产者收到错误后会尝试重试，直到超过重试次数，返回错误信息。

### 生产者如何创建

* bootstrap.servers

  指定broker的地址清单，至少要有两台，可以从这两台查询其他broker的地址，也可以防止其中一台宕机

* key.serializer

  broker希望接受和发送的都是字节数组，生产者使用key.serializer这个类序列化要发送的键对象，如果使用常见的数据类型，就可以使用kafka提供的序列化器，自定义序列化器就要实现Serializer接口。

* value.serializer

  如果键和值类型一样，那么就可以使用一样的序列化器，不然就要重新配置

  **创建一个新的生产者**：

  ```
  
  ```

  * 新建一个新的Properties对象
  * 因为键和值是字符串类型，所以使用内置的StringSerializer
  * 创建一个新的生产者对象，然后把Properties对象传给他

发送消息由三种模式：

* 发送并忘记：不关心是否到达
* 同步发送：使用send发送消息，返回一个Future对象，调用get（）可以得到消息发送是否成功
* 异步发送：调用send方法，并指定一个回调函数，服务器返回响应并且调用它

### 发送消息到kafka

```

```

* 创建：send方法需要将ProducerRecord对象作为参数，所以要先创建一个。他有多个构造函数，键和值的对象类型必须与序列化器和生产者对象匹配
* 发送：使用send发送ProducerRecord对象，消息先是被放进缓冲区，然后使用单独的线程发送到服务器，并返回一个Future对象
* 纠错：生产者不仅发送会产生异常，本身也会：序列化异常，缓冲区满，发送线程被中断

### 同步发送消息

```

```

* send方法先返回一个Future对象，调用Future的get方法，等待kafka服务器响应，如果返回错误，就会抛出异常，如果没有错误，则会得到一个RecordMetadata对象，来获取消息偏移量
* 如果发送消息前或者发送中出现了错误，那么也会抛出异常

kafkaProducer会出现两类错误：

* 可重试错误：可以通过重发消息解决：连接错误，无主错误
* 不可重试：消息过大

### 异步发送消息

不需要等待响应，不需要占用阻塞一个线程

```

```

* 为了使用回调，需要一个实现了Callback接口的类，这个接口只有一个onCompletion方法
* 如果kafka返回一个错误，onCompletion就会抛出一个非空异常
* 在发送消息时传进去一个回调对象

## 生产者的配置

* acks：指定要有多少个分区副本接收到消息，生产者才认为消息写入是成功的（0，1...all）

* buffer.memory：设置生产者内存缓冲区大小，该区缓冲要发给服务器的消息

* compression.type：默认消息不会被压缩，但是可以指定为snappy，gzip，lz4

* retires：决定了生产者可以重发消息的次数，如果达到这个错误，那么生产者就会放弃，并返回错误

  ​	retire为非0整数，同时设置每个连接可传送批次大小更大，则会影响顺序保证，如果第一批次的消息写入失败，第二批次消息写入成功，如果第一次重试成功，那么顺序就反过来了。

* batch.size：决定一个批次可以使用的内存大小，但是批次并不是满了才发，任何大小的批次都有可能被发送，所以大了没用

* linger.ms：指定发送批次之前，等待更多消息加入批次的时间，增加延迟，但会增加吞吐量那么什么时候发送批次呢？

  * 在批次满了的时候，或者到达时间的时候，就会发送

* client.id：识别机器来源

* max.in.flight.requests.per.connection:指定接收到服务器确认之前可以发送多少消息

* timeout.ms\request.timeout.ms\metadata.fetch.timeout.ms：

  * request timeout.ms指定生产者发送数据等待服务器响应的时间
  * metadata指定生产者获取元数据时的等待响应时间
  * timeout.ms指定broker等待同步副本返回消息确认的时间，指定时间没有收到同步副本的确认，broker就会返回一个错误

* max.request.size:生产者能发送的请求大小

* max.block.size:调用send方法，或者使用partitionsFor方法获取元数据的阻塞时间，什么时候会阻塞呢？

  * 当发送缓冲区已满，就会阻塞

* receive.buffer.byte/send.buffer.byte：TCPsocket接受和发送缓冲区的大小

## 序列化器

创建一个生产者对象必须要指定序列化器，但如何自定义序列化器？（不建议）

```

```

### 使用Acro序列化

Avro数据通过与语言无关的schema定义，通过JSON来描述，数据被序列化为二进制文件或者JSON文件，在读写文件时需要用到schema，如果写消息的应用使用了新的schema，负责读的端口不需要更新schema也可以正常使用

使用方法：

* 首先把写入数据所使用的schema写入注册表
* 在记录里引用注册表中的schema的标识符
* 负责读取的程序从注册表中拉去正确的schema，序列化器和反序列化器分别负责注册和拉取

```

```

## 分区：

kafka消息包含四部分：主题，消息，键，分区。键有两个用途：作为消息的附加信息，决定消息前往哪个分区

创建一个消息记录：

```

```

创建一个键为空的消息记录

```

```

* 如果键为空，那么记录将被随机的发送到主题内各个可用分区上，分区器使用轮询器来做（round robin）
* 如果键不为空，而且采用了默认的分区器，那么会对键进行散列，将值对所有的分区数量求余（保证每次相同键映射到相同分区），一旦新增了分区，映射将会改变

### 自定义分区器

* 只接受字符串作为键，如果不是字符串，那么酒抛出异常
* 客户姓名应该用configure传，而不是直接硬编码

```
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.InvalidRecordException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

import java.util.List;
import java.util.Map;

/**
 * @author Yuyuan Huang
 * @create 2021-07-14 17:41
 */
public class MyPartition implements Partitioner {
    public void configure(Map<String,?>configs){


    }

    @Override
    //接受主题，键，键Byte，值，值Byte，集群
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int numPartitions = partitions.size();



        if ((keyBytes!=null)||(!(key instanceof String)))//如果键Bytes不为null或者键不是字符串
        {
            throw new InvalidRecordException("key 必须为客户姓名");
        }
        if (((String)(key)).equals("banana")){
            return numPartitions;
            //因为这个分区总被分到最后一个分区
        }
        return (Math.abs(Utils.murmur2(keyBytes))%(numPartitions-1));

    }

    @Override
    public void close() {

    }
}
```

# 消费者-从kafka读数据

再均衡：分区的所有权发生变化，从一个消费者转到另一个消费者，再均衡发生的过程中，群组会发生一段时间的不可用，而且上一个消费者读取的进度不会继承到下一个消费者的读取上

分配分区：每次再均衡都会发生，当消费者新加入群组，向群组协调器发送JoinGroup请求，第一个加入的为群主，群主从协调器获得成员列表，并为每个成员分配分区，每个成员只知道自己的分区，

心跳：消费者向群组协调器broker发送心跳，维持自身的读取权以及自身与群组的所属关系，如果较长时间无心跳，就会触发一次再均衡

心跳线程：kafka添加了一个独立的心跳线程，在轮询消息的空挡发送心跳，发送心跳的频率（用于检测崩溃的消费者）和消息轮询（由处理消息的时间决定）的频率就是相互独立的。在新版本中，可以指定消费者在离开群组之前有多长时间不触发轮询，这样可以避免活锁（某个时候程序没有崩溃，而是因为其他原因无法运行），而session.timeout.ms用来控制多长时间检查消费者发生崩溃，以及停止发送心跳的时间

kafka消费者从属消费者群组，一个群组的消费者订阅一个主题，每个消费者接受一部分分区的消息

## 创建Kafka消费者

读取消息前，先创建一个KafkaConsumer对象，注入属性，属性由bootstrap.servers\key.deserialize\value.deserializer构成，还有group.id不是必须的

* bootstrap.servers指定Kafka集群的连接字符串
* 反序列化器将字节数组转化为Java对象
* group.id指定KafkaConsumer属于哪一个群组

## 订阅主题

subscribe接受一个主题列表作为参数

## 轮询

consumer通过一个轮询向服务器请求数据，一旦订阅，轮询就会进行群组协调，分区均衡，发送心跳，获取数据，只要使用API就可以处理从分区返回的数据

轮询在第一次调用poll方法时，会负责查找群组协调器，之后加入群组，接受分配的分区，如果发生了再均衡，这个在均衡的过程是在轮询其间进行的，心跳也是在轮询期间进行，所以要尽量保证轮询其间的处理工作尽快完成

线程安全：一个消费者占用一个线程，把消费者的逻辑封装在自己的对象里，然后用多个线程让每个消费者运行在自己的线程。

### 详细配置

* fetch.min.bytes

  指定消费者从服务器获取的最小字节数，如果broker收到消费者请求，但是数据量小于改大小，就会等有足够的数据才会发送

* fetch.max.wait.ms

  指定broker的等待时间，如果很久没有获得足够的数据，就在这个值内返回目前的数据

* max.partition.fetch.bytes

  指定服务器从每个分区返回给消费者的最大字节数，默认1MB，导致poll方法从每个分区返回的记录不超过这个字节，这个值必须比broker一次能接受的消息大，不然消费者无法读取所有的信息，可以调小这个值避免会话过期，导致消费者重分配

* session.timeout.ms

  消费之死亡之前可以和服务器断开连接的时间，3s默认，如果消费者没有在这个时间内发送心跳，那么就会触发再均衡，把这个分区分配给别的消费者，这个值和heartbeat.interval.ms一起用，心跳值指可以消费者发送自己心跳的频率，而这个值指多久没有心跳就会重分配

* enable.auto.commit

  消费者是否自动提交偏移量，默认true，为了避免数据丢失和重复数据，可以设置为false，使用auto.commit.interval.ms控制发送频率

* partition.assignment.strategy

  PartitionAssignor根据指定的消费者和主题，决定分区分配策略

  * range策略

    把主题中连续的分区分配给消费者

  * roundRoubin

    将所有主题逐个分配给消费者，如果所有消费者订阅相同的主题，那就给消费者分配i相同数量的分区

* clientid

  标志从客户端发过来的消息

* max.poll.records

  控制单次call方法返回的记录数量，可控制在轮询中处理的信息量

* receive.buffer.bytes和send.buffer.bytes

  TCP缓冲区也可以设置大小

## 提交和偏移量

偏移量：消息在分区的位置，消费者使用kafka来追踪消息在分区的位置

提交：更新分区的当前位置

消费者如何提交偏移量：

消费者发送消息到consumer_offset的特殊主题发送消息，包含每个分区的偏移量，如果消费者一直处于运行状态，那偏移量无用，但是如果消费者宕机或者新的消费者加入群组，那么会再均衡，新的消费者可以从这个偏移量继续。

### 自动提交

把enable.auto.commit设置为true，那么每5s，消费者就会把poll接收到的最大偏移量提交，提交时间由auto.commit.interval.ms决定，提交通过轮询做，每次进行轮询都会检查是否提交偏移量了，如果是，那么就会提交从上一次轮询返回的偏移量，如果否，那就提交当前的偏移量

每次调用轮询方法，都会提交上一次调用返回的偏移量，并不知道那些消息被处理了，所以这次调用要保持当前调用返回的消息被处理完毕，这才处理异常和提前退出轮询应该注意

问题在于，如果设置5s的间隔，3s后发生了再均衡，那么就会重复读取3s内到达的数据。

### 提交当前偏移量（同步）（borker回应前，应用程序会一直阻塞）

可以在必要的时间提交偏移量，而不是固定时间间隔

首先设置enable.auto.commit为false，使用commitSync()提交偏移量，可以提交由poll返回的最新偏移量

### 异步提交

当任务提交过程中，或者遇到某些问题之前，commitSync()会一直重试，但是commitAsync()不会，因为可能受到已过期的偏移量信息

### 同步和异步组合提交

一般的提交失败不会出现问题，但是如果这是关闭消费者或者再均衡前的最后一次提交，那就要确保成功，在消费者关闭前一般会组合使用同步和异步提交

### 提交特定的偏移量

如果想频繁的提交偏移量，或者在批次处理的中途提交，可以在调用commitSync()和commitAsync()的时候传进去要提交的分区和偏移量的map，但是一个消费者可能读取两个分区，所以要跟踪所有分区的偏移量

### 再均衡监听器（失去分区所有权时要做）

消费者退出和分区再均衡，会产生一次清理清理工作：消费者会在失去一个分区所有权之前，提交一个已处理的偏移量，如果消费者有缓冲区处理偶发事情，那么失去所有权时，需要处理缓冲区累积的记录。

首先创建一个继承了 ConsumerRebalanceListener，重写其中两个方法， onPartitionsRevoked和onPartitionsAssigned，每一次再均衡前和再均衡后都会触发

如果发生再均衡，则在即将失去分区所有权的时候，提交最近处理过的偏移量，而不是批次中在处理的最后一个偏移量，因为分区在处理的过程中可能会被撤回，并且要提交所有消息的偏移量，而不是即将失去分区所有权的偏移量

调用subscribe方法传进去一个ConsumerRebalanceLisener,这个实例有两个需要实现的方法：

```
 class HandleRebalance implements ConsumerRebalanceListener{

            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> collection) {
                System.out.println("lost partitions in rebalance. Committing current offsets"+currentOffsets);
                consumer.commitSync(currentOffsets);
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> collection) {

            }
        }
```

* 该方法会在在均衡开始之前和消费者停止读取信息之后调用，在这里提交偏移量

  ```
  @Override
              public void onPartitionsRevoked(Collection<TopicPartition> collection) {
                  System.out.println("lost partitions in rebalance. Committing current offsets"+currentOffsets);
                  consumer.commitSync(currentOffsets);
              }
  ```

* 该方法会在分配分区之后和消费者读取信息之前调用

  ```
   @Override
              public void onPartitionsAssigned(Collection<TopicPartition> collection) {
  
              }
  ```

### 从特定偏移量开始处理记录

可以使用poll方法从特定偏移量处开始处理消息，从分区首部或者分区尾部读取：seekToBegining(Collection<TopicPartition> tp)  seekToEnd(Collection<TopicPartition> tp)

使用ConsumerRebalanceListener和seek方法：

### 如何退出轮询

要退出循环，就要用另一个线程调用consumer.wakeup()方法，这是唯一一个消费者可以从其他线程安全调用的方法，抛出WakeupException异常，在退出线程前调用close方法很重要，他会提交任何未提交的信息，并告知自己要离开群组，并且触发再均衡

运行在单独的线程，当需要终结时，调用wakeup方法，不必要处理WakeupException

```
Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                System.out.println("exit...");
                consumer.wakeup();
                try{
                    mainThread.join();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });
```

## 集群成员关系

当borker长时间无响应，就会在zookeeper上断开连接，对应的临时节点就会移除，监听列表的kafka组件会被告知这个broker已经移除

当关闭broker时，节点会消失，但是他的id还可能存在在其他数据结构中

### 控制器

**控制器的竞争和故障：**除了borker的日常工作，它还包含broker的首领选举，第一个控制器会创建一个控制器，之后到来的broker都会尝试创建首领控制器，但是会出现一个错误阻止创建，并导致创建一个zookeeper watch对象，以方便接收首领的变更通知

当控制器出现故障，这个临时节点会消失，其他watch对象得到通知，会尝试让自己变成新的控制器，并且向所有broker发送controller epoch，比之前更大数值。

**首领的选举：**当控制器发现一个broker离开群组，并且他原本包含集群的首领（分区），那么就会在该（分区列表副本的下一个副本中 ）选出首领，并将信息发给所有包含新首领或现有跟随者的broker发送谁是新首领，谁是分区跟随者的信息，新首领处理生产者消费者的请求，跟随者负责复制

