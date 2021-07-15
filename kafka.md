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

心跳：消费者向群组协调器broker发送心跳，维持自身的读取权以及自身与群组的所属关系，如果较长时间无心跳，就会触发一次再均衡



kafka消费者从属消费者群组，一个群组的消费者订阅一个主题，每个消费者接受一部分分区的消息

