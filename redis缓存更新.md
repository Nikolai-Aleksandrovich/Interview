**先删除缓存，然后再更新数据库**，而后续的操作会把数据再装载的缓存中。**然而，这个是逻辑是错误的**。

在并发情况下，当a线程首先清空了数据库，b线程在缓存中查询，并没有查到，于是取数据库中查询，并把结果存在缓存中，这时候a线程更新了数据库，此时会出现很长时间的脏读

更新缓存的的Design Pattern有四种：Cache aside, Read through, Write through, Write behind caching

#### Cache Aside Pattern

- **失效**：应用程序先从cache取数据，没有得到，则从数据库中取数据，成功后，放到缓存中。

- **命中**：应用程序从cache中取数据，取到后返回。

- **更新**：先把数据存到数据库中，成功后，再让缓存失效。

![Cache-Aside-Design-Pattern-Flow-Diagram](https://coolshell.cn/wp-content/uploads/2016/07/Cache-Aside-Design-Pattern-Flow-Diagram-e1470471723210.png)

![Updating-Data-using-the-Cache-Aside-Pattern-Flow-Diagram-1](https://coolshell.cn/wp-content/uploads/2016/07/Updating-Data-using-the-Cache-Aside-Pattern-Flow-Diagram-1-e1470471761402.png)

存在的问题：

a线程进行读操作，没有命中缓存，于是从数据库中查询，查到数据后将数据取出来，但此时还未存储在缓存中。

b线程进行写操作，在a线程将数据取出来的时候，更新这个数据，并在缓存中进行删除

此时，a线程将数据存在缓存中，出现了脏读

最好为缓存设置过期时间

#### Read/Write Through Pattern

Read/Write Through套路是把更新数据库（Repository）的操作由缓存自己代理了，所以，可以理解为，应用认为后端就是一个单一的存储，而存储自己维护自己的Cache。**

##### Read Through

Read Through 套路就是在**查询操作中更新缓存**，也就是说，当缓存失效的时候（过期或LRU换出），Cache Aside是由调用方负责把数据加载入缓存，而**Read Through则用缓存服务自己来加载，从而对应用方是透明的。**

##### Write Through

Write Through 套路和Read Through相仿，不过是在更新数据时发生。当有数据更新的时候，如果没有命中缓存，直接更新数据库，然后返回。如果命中了缓存，则更新缓存，然后再由Cache自己更新数据库（这是一个同步操作）

![Write-through_with_no-write-allocation](https://coolshell.cn/wp-content/uploads/2016/07/460px-Write-through_with_no-write-allocation.svg_.png)

#### Write Behind Caching Pattern（Write Back）只更新缓存

在更新数据的时候，只更新缓存，不更新数据库，而我们的缓存会异步地批量更新数据库。

**好处**就是让数据的I/O操作飞快无比（因为直接操作内存 ），因为异步，write backg还可以合并对同一个数据的多次操作，所以性能的提高是相当可观的。

**缺点**：数据不是强一致性的，而且可能会丢失

Write Back实现逻辑比较复杂，因为他需要track有哪数据是被更新了的，需要刷到持久层上。操作系统的write back会在仅当这个cache需要失效的时候，才会被真正持久起来，比如，内存不够了，或是进程退出了等情况，这又叫lazy write。

![Write-back_with_write-allocation](https://coolshell.cn/wp-content/uploads/2016/07/Write-back_with_write-allocation.png)