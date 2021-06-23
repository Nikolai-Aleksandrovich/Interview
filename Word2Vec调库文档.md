## Word2Vec调库文档

## 两个文档的相似性：

python API：`similarities.docsim` – Document similarity queries

计算文档在向量空间里的相似性

主要的类是 [`Similarity`]，对给定文档集建立索引

一旦索引建立，就可以进行文档间的相似度查询，结果是一个和初始向量机一样大的向量，用浮点数表示，也可以只要求得到TopN

## How It Works

 [`Similarity`]类基于硬盘，把索引分割成数个较小的子索引，如果整个索引内存放得下，一百万文档放1GB，那么直接使用[`MatrixSimilarity`]或者[`SparseMatrixSimilarity`]类，这样把所有的索引放在内存中，但也不支持动态添加索引

一旦索引被初始化，就可以查询相似性：

```python
from gensim.test.utils import common_corpus, common_dictionary, get_tmpfile
>>>
index_tmpfile = get_tmpfile("index")
query = [(1, 2), (6, 1), (7, 2)]
>>>
index = Similarity(index_tmpfile, common_corpus, num_features=len(common_dictionary))  # build the index
similarities = index[query]  # get similarities between the query and all index documents
```

可以在一批次全部提交，会带来性能提升

```python
from gensim.test.utils import common_corpus, common_dictionary, get_tmpfile
>>>
index_tmpfile = get_tmpfile("index")
batch_of_documents = common_corpus[:]  # only as example
index = Similarity(index_tmpfile, common_corpus, num_features=len(common_dictionary))  # build the index
>>>
# the batch is simply an iterable of documents, aka gensim corpus:
for similarities in index[batch_of_documents]:
    pass
```

如果需要索引中文档对索引的相似性

```python
from gensim.test.utils import common_corpus, common_dictionary, get_tmpfile
>>>
index_tmpfile = get_tmpfile("index")
index = Similarity(index_tmpfile, common_corpus, num_features=len(common_dictionary))  # build the index
>>>
for similarities in index:  # yield similarities of the 1st indexed document, then 2nd...
    pass
```

与一个文档的语料库所存储的内存中的索引矩阵计算余弦相似性

除非整个矩阵能放进内存，不然就用[`Similarity`]替代

## KeyedVectors

因为训练好的词向量与他们的训练方法之间是独立的，于是他们可以被另外的结构所表示

结构叫做 “KeyedVectors” ，本质上是一种key到vector的映射，通常是一个{str => 1D numpy array}.

key可以是一个word，也可以是一个文档

#### 如何获得词向量？

首先训练一个完整模型，然后获得他的model.wv，其中就包含映射的向量

比如：使用word2Vec训练向量

```python
from gensim.test.utils import lee_corpus_list
from gensim.models import Word2Vec

model = Word2Vec(lee_corpus_list, vector_size=24, epochs=100)
word_vectors = model.wv
```

将结果持久化到 磁盘

```python
from gensim.models import KeyedVectors

word_vectors.save('vectors.kv')
reloaded_word_vectors = KeyedVectors.load('vectors.kv')
```

#### 用结果能干什么呢？

可以使用结果进行一系列语义/语序的自然语言处理任务，比如

###### 查看最频繁的词

```python
import gensim.downloader as api
>>>
word_vectors = api.load("glove-wiki-gigaword-100")  # load pre-trained word-vectors from gensim-data
>>>
# Check the "most similar words", using the default "cosine similarity" measure.
result = word_vectors.most_similar(positive=['woman', 'king'], negative=['man'])
most_similar_key, similarity = result[0]  # look at the first match
print(f"{most_similar_key}: {similarity:.4f}")
queen: 0.7699
```

###### 使用不同的相似性计算方法

```python
# Use a different similarity measure: "cosmul".
result = word_vectors.most_similar_cosmul(positive=['woman', 'king'], negative=['man'])
most_similar_key, similarity = result[0]  # look at the first match
print(f"{most_similar_key}: {similarity:.4f}")
queen: 0.8965
>>>
print(word_vectors.doesnt_match("breakfast cereal dinner lunch".split()))
cereal
>>>
similarity = word_vectors.similarity('woman', 'man')
similarity > 0.8
True
>>>
result = word_vectors.similar_by_word("cat")
most_similar_key, similarity = result[0]  # look at the first match
print(f"{most_similar_key}: {similarity:.4f}")
dog: 0.8798
>>>
sentence_obama = 'Obama speaks to the media in Illinois'.lower().split()
sentence_president = 'The president greets the press in Chicago'.lower().split()
>>>
similarity = word_vectors.wmdistance(sentence_obama, sentence_president)
print(f"{similarity:.4f}")
3.4893
>>>
distance = word_vectors.distance("media", "media")
print(f"{distance:.1f}")
0.0
>>>
similarity = word_vectors.n_similarity(['sushi', 'shop'], ['japanese', 'restaurant'])
print(f"{similarity:.4f}")
0.7067
>>>
vector = word_vectors['computer']  # numpy vector of a word
vector.shape
(100,)
>>>
vector = word_vectors.wv.get_vector('office', norm=True)
vector.shape
(100,)
```

###### 将人的意见与词的相关性匹配

```python
from gensim.test.utils import datapath
>>>
similarities = model.wv.evaluate_word_pairs(datapath('wordsim353.tsv'))
```

###### 词的类比

```
analogy_scores = model.wv.evaluate_word_analogies(datapath('questions-words.txt'))
```



## 使用举例

#### 初始化一个模型

```python
from gensim.test.utils import common_texts
from gensim.models import Word2Vec

model = Word2Vec(sentences=common_texts, vector_size=100, window=5, min_count=1, workers=4)
model.save("word2vec.model")
```

训练以流的形式进行，可以把任何可以迭代的数据进行训练

如果已经储存模型，之后还可以再次开始训练

```python
model = Word2Vec.load("word2vec.model")
model.train([["hello", "world"]], total_examples=1, epochs=1)
(0, 2)
```

训练好的词向量储存在一个[`KeyedVectors`]实例中，这样做的理由是，如果不需要保留模型的训练过程参数，那么只保留训练结果会更省事

```python
vector = model.wv['computer']  # get numpy vector of a word
sims = model.wv.most_similar('computer', topn=10)  # get other similar words
```

这样保存的KeyedVectors更小，更快，而且可以映射到更轻的加载，并且可以在不同的过程中共享内存中的向量

```python
from gensim.models import KeyedVectors

# Store just the words + their trained embeddings.
word_vectors = model.wv
word_vectors.save("word2vec.wordvectors")

# Load back with memory-mapping = read-only, shared across processes.
wv = KeyedVectors.load("word2vec.wordvectors", mmap='r')

vector = wv['computer']  # Get numpy vector of a word
```

可以切换model到KeyedVectors

```
word_vectors = model.wv
del model
```

#### 多词嵌入

```
from gensim.models import Phrases

# Train a bigram detector.
bigram_transformer = Phrases(common_texts)

# Apply the trained MWE detector to a corpus, using the result to train a Word2vec model.
model = Word2Vec(bigram_transformer[common_texts], min_count=1)
```





## Word2Vec教程

来自于https://rare-technologies.com/word2vec-tutorial/

#### 准备输入

gensim的word2vec期待一系列的句子作为输入，每一个句子都是一个包含词的列表

* 把输入作为python列表很方便，但是再输入很大的时候，就会占用很多内存

```python
# import modules & set up logging
import gensim, logging
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)
 
sentences = [['first', 'sentence'], ['second', 'sentence']]
# train word2vec on the two sentences
model = gensim.models.Word2Vec(sentences, min_count=1)
```

##### 节省内存

* gensim只要求顺序地输入最需要的句子，可以每次只加载一个句子，进行处理，再加载另外一个

比如：如果输入的语料库包含很多个文件，每行一个句子，与其把所有的语句加载到内存中，不如按照顺序处理输入

```python
class MySentences(object):
    def __init__(self, dirname):
        self.dirname = dirname
 
    def __iter__(self):
        for fname in os.listdir(self.dirname):
            for line in open(os.path.join(self.dirname, fname)):
                yield line.split()
 
sentences = MySentences('/some/directory') # a memory-friendly iterator
model = gensim.models.Word2Vec(sentences)
```

比如想要从文件中处理数据出来，转化为unicode，小写，移除数字，提取命名过的实体之类的，这些都可以再上边这个类中进行，迭代器和word2vec不需要知道细节

##### 细节

使用word2Vec（*Word2Vec(sentences, iter=1)*）会引起两次传值，或者更普遍的说，会引起*iter+1*次传值

第一次传值收集词和他的频率并构建一个字典树

第二次传值训练神经模型

如果使用的输入流是一个不可重复的流，可以手动设置传值

```python
model = gensim.models.Word2Vec(iter=1)  # an empty model, no training yet
model.build_vocab(some_sentences)  # can be a non-repeatable, 1-pass generator
model.train(other_sentences)  # can be a non-repeatable, 1-pass generator
```

## 训练过程

接受不同的参数，这些参数影响训练性能和速度

##### 剪枝参数

负责剪枝：在一个百万级别的语料库中，只出现一两次的词语大概率是对相似度无用的垃圾，另外，就算要训练，也没有足够的出现频率来训练有意义的模型，所以最好忽略他们：

```python
model = Word2Vec(sentences, min_count=10)  # default value is 5
```

合理的最小剪枝频率为0-100，取决于数据集的大小和行为

##### 规模大小

第NN层的大小代表着训练算法的自由度：

```
model = Word2Vec(sentences, size=200)  # default value is 100
```

更高的维度代表更好的性能，但一般都需要更大的数据集，合理的数值在10-100

##### 并发性

同时进行训练的work数量，只有使用Cython才有意义

```
model = Word2Vec(sentences, workers=4) # default = 1 worker = no parallelization
```

## 内存使用计算

在内核中，word2vec模型参数以矩阵形式存储(NumPy arrays)

内存使用情况：去重单词数量×维度×4比特×矩阵数量

三个矩阵，10w个独特的词，200维度，那么内存耗费为100,000×200×4×3 bytes = ~229MB.

另外还需要一些内存存储字典树，但占比很小

## 评估

word2vec是一个无监督学习任务，所以模型好坏要根据模型的应用场景来评估

谷歌使用一个包含20000个例子的测试集进行语义和语序测试，遵循着“A is to B as C is to D”原则：

https://raw.githubusercontent.com/RaRe-Technologies/gensim/develop/gensim/test/test_data/questions-words.txt

## 存储和加载模型

```python
model.save('/tmp/mymodel')
new_model = gensim.models.Word2Vec.load('/tmp/mymodel')
```

## 在线训练

可以加载已经训练好的模型，并进行进一步的训练

```python
model = gensim.models.Word2Vec.load('/tmp/mymodel')
model.train(more_sentences)
```

需要调整train函数的总词数，取决于要模拟的learning rate decay

## 使用模型

支持多个词相似性测试

```python
model.most_similar(positive=['woman', 'king'], negative=['man'], topn=1)
[('queen', 0.50882536)]
model.doesnt_match("breakfast cereal dinner lunch";.split())
'cereal'
model.similarity('woman', 'man')
0.73723527
```

如果需要输出的向量：

```python
model['computer']  # raw NumPy vector of a word
array([-0.00449447, -0.00310097,  0.02421786, ...], dtype=float32)
```

