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

**控制器的竞争和故障：**除了borker的日常工作，它还包含broker的首领选举，第一个broker会创建一个控制器，之后到来的broker都会尝试创建首领控制器，但是会出现一个错误阻止创建，并导致创建一个zookeeper watch对象，以方便接收首领的变更通知

当控制器出现故障，这个临时节点会消失，其他watch对象得到通知，会尝试让自己变成新的控制器，并且向所有broker发送controller epoch，比之前更大数值。

**首领的选举：**当控制器发现一个broker离开集群，并且如果他原本包含分区的首领（分区），那么这些失去首领的分区需要一个新的首领，控制器就会遍历所有分区，在该（分区列表副本的下一个副本中 ）选出首领，并将信息发给所有包含新首领或现有跟随者的broker，新首领处理生产者消费者的请求，跟随者负责复制

新加入集群：控制器发现一个broker新加入集群，就是用brokerID检查新加入的broker是否包含现有分区的副本，控制器把变更通知发给所有broker，新的broker开始从新首领复制消息

使用epoch来避免脑裂

### 复制

kafka使用主题组织数据，每个主题有若干分区，每个分区有多个副本，副本保存在broker上，每个broker保存很多分区的很多副本

副本有两种类型：

* 首领副本：每个分区有一个首领副本，负责消费者和生产者请求

* 跟随者副本：唯一任务就是从首领复制消息，保持一致，并且随时准备成为首领

什么时候首领认为跟随者与自己不同步？

当跟随着10s内没有请求任何消息，或者10s没有请求最新的消息，那就是不同步的，那么就无法成为首领

同步的副本：数据与首领保持一致，只有同步副本才可以成为首领

首选首领：创建主题时选定的首领就是首选首领（第一个指定的副本），希望在这个首选首领成为真正首领时，broker的载荷得到均衡，kafka的auto.leader.relalance.enable为true，会导致如果当前首领不是首选，而且首选同步，那么直接把首选升级为首领

### 处理请求

基于TCP，处理消费者和生产者的请求

所有请求消息都包含一个消息头：

* Request type（API key）
* Request version（因客户端版本不同而不同）
* Correlation ID（唯一表示请求消息）
* Client ID（标识发送请求的客户端）

流程：broker在每一个端口上运行**Acceptor线程**，这个线程会创建一个连接，并把它交给**Processor线程**（数量可配置）处理，从客户端获取请求消息，并把请求放在请求队列，然后把响应从响应队列读出，请求队列的请求由**IO线程**处理

请求类型：

* 生产请求：生产者写入的消息
* 获取请求：消费者和跟随者副本读取消息的请求

客户端应该自己负责发送请求到正确的broker上，该如何知道哪一个broker包含自己要写入分区的首领呢？

元数据请求：请求包含主题列表，服务器的响应包含这些主题所包含的分区，每个分区有哪些副本，谁是首领，因为broker的消息是互通的，所以可以询问任何一个broker

客户端缓存结果后使用，但也会重新发送来验证broker集群是否发生变化

### 生产请求

包含首领副本的broker收到生产者的请求，会检查：

* 用户是否有写入权限
* ack值是否合法？
* 如果ack=all，是否有足够多的同步副本保证ack完成

之后，消息被写入磁盘（写入文件缓存，并达到某个值时统一写入磁盘），并且broker在写入磁盘前，先检查ack数量是否合法，再返回响应，并写入，这个过程中，信息被写入到缓冲区中

### 获取请求

分区首领收到请求，先检查请求是否有效，使用零复制技术进行发送：直接从linux文件系统缓存发送到网络上，不需要经过缓冲区

并不是所有分区首领的消息都可以被读取，当消息被写入到分区首领，但还没有被同步，那么对这个消息的请求也是空，如果允许消费者读了，会破坏一致性。

## 物理存储

kafka基本存储单元是分区，分区无法再在多个broker上细分，也无法在一个broker的多个磁盘上细分

分区分配的要点：

* 在borker中间平均的分配分区
* 每个分区的副本分配在不同的broker上
* 每个分区的副本分配在不同机架上

### 文件格式

kafka的文件发送使用零复制技术，所以磁盘上的数据格式和生产者的数据格式一样

消息包含：键、值、偏移量、大小、检验和、消息格式、消息格式版本号、压缩算法、时间戳

对于压缩过的消息，同一个批次的消息会被压缩在一起，解压后会有整个批次的消息

kafka在每个分区维护一个索引，索引把偏移量映射到片段文件和偏移量在文件的位置，索引被分成片段，所以删除消息也可以删除对应的索引，kafka可以重新生成索引，所以不担心索引损坏

### 清理

kafka会根据设置的时间保留数据，把潮湿的旧数据删掉

每个日志片段可以被分为：

* 干净的部分：这个值之前被清理过，每个键只有一个对应的值，此值是上一次清理保留下来的
* 污浊：上一次清理之后写入

清理工作创建一个清理管理器线程，以及多个清理线程，选择污浊率较高的分区进行清理，首先读取分区的污浊部分，在内存中创建一个map，每个元素包含了消息键的散列值和消息偏移量

创建好偏移量后，开始从干净的片段读取消息，从最旧的消息开始，检查每个消息内容与map里的内容对比，检查消息的键是否存在于map中，存在那就被忽略，不存在就说明最新，把消息复制到替换片段上，

### 被删除的事件

彻底把一个键从系统删除，应用程序发送一个包含该键且值为null的消息，作为墓碑消息，清理线程发现该消息，会进行常规的清理，要保持一段时间等消费者发现值为null，清理线程再从kafka删除这个消息

### kafka可靠性保证

* 保证分区消息的顺序
* 只有消息被写入分区的所有同步副本时，才被认为是已提交的，生产者可以选择接受不同的确认类型（复制一份，全部复制，发送到网络）
* 可靠性，只要一个副本是活跃的，那么这个消息就不会消失
* 消费者只能读取已经提交的消息

### 复制

复制机制和多副本架构能保证消息的持久性和可靠性

复制过程中，跟随者副本要如何才能被认为是同步的

* 与zookeeper保持一个活跃的对话，在过去6s内发送过心跳
* 过去10s内从首领获取过消息
* 过去10内获取过最新的消息

必须全部满足，否则就是不同步的，如果当前为不同步的，那么可以通过与zookeeper重新建立连接，从首领获取最近消息，重新同步

如果一个broker在同步与不同步之间切换，可能是因为java中不恰当的垃圾回收导致的，一般垃圾回收会导致几秒钟的停顿，从而导致断开连接

非同步副本不会对性能导致影响，但是会让宕机的风险变大

## broker配置

配置参数可以在broker级别控制所有主题，也可以在主题级别，控制某个主题

### 复制系数

配置每个分区被不同的主题复制的次数，一般为3

主题级别：replication.factor

broker级别：default.replication.factor

### 不完全的首领选举

只能在broker级别配置unclean.leader.election，默认为true

什么是完全的首领选举：一个首领不可用，控制器会选择一个分区副本作为首领，这个过程中没有丢失数据，就是完全的

如果在首领不可用时，其他副本都是不同步的，该如何？

* 如果允许不同步的副本成为首领，会出现数据丢失和数据不一致的情况，如果不允许，会出现较长时间的宕机

### 最少同步副本

min.insync.replicas

至少存在n个同步副本，才会接受写数据，发送数据生产者收到NotEnoughReplicasException异常，这就是为了避免不完全选举

## 使用可靠的生产者

丢失数据的情况：

* ack为1，禁用不完全选举，生产者发送消息给规模为3的集群broker，首领收到消息，回复收到，然后崩溃。跟随者被选为首领，但数据已经丢失
* ack为all，禁用不完全选举，若生产者未处理“首领异常”问题，也会让消息丢失

## 生产者的配置：

### 发送确认

* ack = 0，只要发到网络上就算成功，丢失消息的可能：序列化失败，网卡错误，无首领等
* ack = 1，首领收到消息并把它写入分区后，发送确认，如果首领在选举，会得到首领不可用的错误，但也会丢失
* ack = all，首领会等待所有同步副本写入消息，再返回确认，与min.insync.replicas一起使用

### 配置生产者的重试参数

生产者处理的错误为两部分：

* 可自动处理的，通过单纯的重试来解决，比如获得“leader not available”
* 需要手动处理的，比如“invalid config”

重试可以保证消息至少被写入一次，但不能保证消息只写入一次，所以最好在消息中加入序列号或标识符

## 使用可靠的消费者

对消费者展示的数据已经具有了一致性，但是消费者要保证的是，不漏读消息

### 消费者的可靠性配置

* group.id，每个消费者需要唯一的groupid
* auto.offset.reset指定没有偏移量可提交或者偏移量不合法，消费者做什么，一种是earliest，导致消费者从分区头开始读取，产生大量重复处理的数据，另一种时latest，消费者从分区末尾开始读取，可以减少重复消息，但会漏掉消息
* enable.auto.commit消费者基于任务调度自动提交偏移量，如果在消费者轮询处理数据，那么自动提交可以保证提交已经处理过的数据对应的偏移量，但可能会导致重复处理（自动提交前停止处理），或者让另一个线程处理数据，可能会让自动提交未处理的数据
* auto.commit.intervals.ms自动提交间隔，默认5s

### 显式提交偏移量

显式提交偏移，要么是想避免重复处理消息，要么是把处理消息的逻辑放在了提交之外

* 处理完事件再提交偏移量
* 提交频度是性能和重复处理消息数量的权衡
* 再均衡前，要提交当前的偏移量
* 重试：
  * 当消费者需要重试，先提交当前的偏移量，然后调用pause方法保持轮询的过程中，不返回消息，防止冲爆缓冲区，这个过程中，不断重试，不管成还是败，调用resume从轮询中获取新消息
  * 当消费者需要充实，先提交错误到一个独立的主题，一个独立的消费者群组负责读取错误信息，并重试
* 消费者维护状态：使用kafkaStream类库
* 长时间阻塞处理：使用线程池处理数据，暂停消费者，值进行轮询，不拿取数据
* 仅一次传递

## 构建数据管道

### 在Connect API和客户端API之间做出选择

写入数据或者读数据，要么使用传统消费者生产者客户端，要么使用Connect API和连接器

可以将kafka客户端内嵌到应用程序中，对于数据存储系统，要使用Connect

### kafkaConnect

Connect是kafka的一·部分，负责kafka和外部数据存储系统之间进行移动数据

connect负责运行插件，这些插件进行数据移动，connect以worker进程集群的方式进行运行，基于worker进程安装连接器插件，再用REST API配置connector，以并行的方式进行大量数据移动

具体做法：数据源connector从源数据系统读取数据，把数据对象提供给worker进程，数据池connector负责从worker进程获取数据，并把数据对象写入目标系统

在部分服务器上启动connect，在部分服务器上启动broker，配置参数有：

* bootstrap.servers:要与connect协同工作的broker服务器，连接器将会向这些broker写入或者读取数据
* group.id：相同groupid的worker属于同一个Connect集群，集群的连接器和任务可以在任何一个worker上运行
* key.converter和value.converter：指定消息的键和值的转换器，转换器可以包含指定的配置参数来规定schema

用connect的REST API配置和监控rest.host.name和rest.port连接器

### 连接器和任务

连接器插件实现了Connector API，包含：

* 连接器，负责：
  * 要运行多少个任务
  * 按照任务拆分数据复制
  * 从worker进程获取任务配置，传递下去：JDBC的连接器会连接到数据库，统计数据表数量，确定要执行多少任务，然后在“max.task”和和实际数据之间选择最小值作为任务数，为每一个任务生成一个配置，包含连接器的配置项和该任务需要复制的数据表，并返回一个映射列表，包含任务的配置，worker进程负责启动任务，配置任务，每个任务只复制指定的数据表，如果用REST API启动连接器，可能会启动任意节点的连接器，那么任务就在这个节点开始
* 任务，负责
  * 将数据移出或者移入kafka，任务初始化时会获得worker进程分配的上下文：
    * 源系统上下文包含原系统记录的偏移量（数据表的主键id或者文件中字节位置i）
    * 目标系统连接器的上下文可以对接收的数据进行处理
  * 任务完成初始化，按照连接器指定的配置进行工作，源系统任务对外部系统进行轮询，返回记录，worker将记录发给kafka，任务池任务通过worker进程接受来自kafka的任务，并写入外部系统

worker进程：

* worker进程是连接器和任务的容器，负责处理http请求（定义连接器和连接器配置），这些请求用来定义连接器和配置，也负责保存连接器配置，启动连接器和任务，并把配置信息传递给任务
* 如果有一个worker在集群中停止发送心跳，其他worker会将他的连接器和任务分给其他worker，如果有新的worker加入集群，其他worker也会将自身的连接器和任务分配给此连接器
* worker负责REST API，配置管理，可靠性，而连接器和任务负责数据移动

转化器和connect

* connect提供了数据API，包含数据对象和用于描述数据的schema：源连接器读取事件，为每个事件生成结构和schema，目标连接器获取schema和值
* worker进程如何将数据保存在kafka？
  * 配置worker进程的过程中可以选择合适的转换器，将数据保存在kafka
  * 过程是这样：连接器将生成的结构数据给worker，worker用转换器将其转换为Avro对象，Json对象，或者字符串，写入kafka

偏移量管理

* 源连接器返回给worker进程的记录包含逻辑分区和逻辑偏移量，如果数据成功发给worker，并且成功存储在kafka重，worker会记录当前偏移量，可能保存在一个主题中，如果连接器崩溃，也可以重新获取进度

## 跨集群数据镜像

镜像：集群间的数据复制

* hub架构：一个中心集群（整体数据）以及多个本地集群（部分数据）
* spoke架构：一个首领和一个跟随者
* 双活架构：多个数据中心需要共享数据并且都可以生产读取数据
  * 优点：就近服务，性能好；冗余；
  * 缺点：数据一致性难以保持（数据异步更新如何避免冲突），为了数据镜像，会产生较大的数据冗余和信道浪费，要避免循环镜像：数据中心为每一个数据创建一个主题
* 主备架构：灾备考虑
  * 优点：以实现
  * 缺点：浪费一个集群，在灾备过程中一定会有数据丢失或者重复数据
  * 失效备援的内容：
    * 数据丢失和不一致：kafka的镜像方案都是异步的，所以一定会出现这种情况
    * 失效备援后的起始偏移量：
      * 偏移量自动重置：要么去头，要么去尾
      * 复制偏移量主题：每次把偏移量提交到一个特定的主题上
        * 缺点：不保证偏移量完全一致
        * 解决办法：
          * 基于事件的失效备援：
            * 每个kafka消息都会带有一个时间信息，再配合索引可以根据时间查询偏移量，再让消费者根据这个偏移量进行处理
          * 偏移量外部映射
            * 使用外部数据存储保存集群之间的偏移量映射
    * 失效备援之后，清理旧的集群，然后从新的集群上把数据迁移过来
    * 如何发现其他集群：使用broker的元数据交换，只要链接一个broker，就可以知道其他broker的地址

### 延展集群

延展集群就是跨多个数据中心的单个kafka集群，为了防止整个数据中心出现故障，延展集群是单个集群，不需要进行镜像操作，而使用kfka内部的复制功能在集群內部进行复制工作，同步复制要求：使用机架信息，使用acks=all，mis.isr，同步复制是最大好处，保证数据100%同步，而且每个broker都发挥了作用

### MIrrorMaker

工作原理：MM包含一组消费者，都属于一个群组，从主题上读取数据，每个MM进程包含一个生产者，每个消费者分配一个线程，消费者从主题的分区上读取数据，通过生产者将数据分配到目标集群，一般每60s发送一次，就算出了问题，也只会出现60s的重复数据

#### MM的配置：

* consumer.config：指定消费者的配置文件，所有消费者公用，那就只能配置一个源集群和groupid，原集群地址和groupid必须要配置：bootstrap.servers,groupid，不能设置自动提交偏移量，因为要求消息到达目标集群后才提交偏移量，auto.offset.reset设置MM从何处开始备份，默认latest，只更新MM之后到达的消息，可改为earliest
* producer.config：生产者的配置文件，必配置的为bootstrap.servers，目标集群的地址
* new.consumer:comsumer版本
* num.streams：流配置，也就是消费者配置
* whitelist：代表需要进行镜像的主题名字，名单中所有主题都被镜像

## 监控kafka

度量指标在Java management Extensions接口可以访问，将负责收集度量指标的代理连接到kafka上，代理作为一个单独的进程运行在监控系统，连接到kafka的JMX接口

### 度量指标：

#### 非同步分区数量：

* 如果非同步分区数量保持不变，说明某个broker已经离线，非同步分区数量等于离弦的broker的分区数量，要关注这个broker
* 如果非同步分区数量波动，或者数量稳定，但是没有broker离线，可能是单个broker的性能问题，也可能是集群的问题

##### 对于集群级别的问题分为两类：

* 负载不均衡
* 资源消耗太大

对于不均衡，需要检查：分区的数量，首领分区的数量，主题流入字节速度，主题流入消息速度，在一个均衡的集群重，对不同的broker这些指标应该是近似相等的

对于性能消耗，需要检查：CPU，网络输入输出吞吐量，磁盘等待时间，磁盘使用比率

##### 对于主机级别的问题

* 硬件问题
* 进程冲突
* 本地配置不一致

### broker度量指标

* 活跃控制器的数量：只能是0或1，一个集群只应该有一个broker是控制器，必须一直是集群控制器，如果有超过1台控制器，那就要进行重启

* 请求处理器空闲率：

  kafka使用两个线程池处理来自客户端的请求：

  * 网络处理器线程池（负责网络读入和写出）
  * 请求处理器线程池（负责来自客户端的请求，从磁盘读取，像磁盘写入，数值越低，负载就越低）
  * 主题流入流出字节
  * 主题流入消息
  * 分区数量
  * 首领数量
  * 离线分区（发生宕机）
  * 请求度量指标

### 主题的度量指标

* 接受失败率
* 生产失败率
* 消息流入速度
* 请求、生产速度
* 主题流入流出字节

### 分区度量指标

* 大小
* 分片数量
* 最大最小偏移量

### JVM监控

* 监控垃圾回收的状态，垃圾回收的次数，总时间，平均时间

