package cn.edu.jxnu.akka.api.impl

import java.io.{File, IOException}

import cn.edu.jxnu.akka.api.Indexer
import cn.edu.jxnu.akka.common.ExceptionConstant
import cn.edu.jxnu.akka.entity.PageContent
import cn.edu.jxnu.akka.exception.IndexingException
import org.apache.lucene.document.{Document, Field}
import org.apache.lucene.index.{CorruptIndexException, DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.search.{IndexSearcher, MatchAllDocsQuery, TopDocs}
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import org.slf4j.LoggerFactory
import org.wltea.analyzer.lucene.IKAnalyzer

/**
 * 索引器实现
 */
class IndexerImpl(indexWriter: IndexWriter) extends Indexer {

    def this() = {
        this(null)
    }

    private val logger = LoggerFactory.getLogger(classOf[IndexerImpl])

    override def index(pageContent: PageContent) {
        try {
            indexWriter.addDocument(IndexerImpl.toDocument(pageContent))
        } catch {
            case ex: CorruptIndexException => {
                logger.error(ex.getMessage)
                throw new IndexingException(ExceptionConstant.INDEX_MESSAGE)
            }
            case ex: IOException => {
                logger.error(ex.getMessage)
                throw new IndexingException(ExceptionConstant.INDEX_CODE_IO, ExceptionConstant.INDEX_MESSAGE_IO)
            }
        }
    }

    override def commit() {
        try {
            indexWriter.commit()
        } catch {
            case ex: CorruptIndexException => {
                logger.error(ex.getMessage)
                throw new IndexingException(ExceptionConstant.INDEX_MESSAGE)
            }
            case ex: IOException => {
                logger.error(ex.getMessage)
                throw new IndexingException(ExceptionConstant.INDEX_CODE_IO, ExceptionConstant.INDEX_MESSAGE_IO)
            }
        }
    }

    override def close() {
        try {
            if (indexWriter != null) {
                indexWriter.close()
            }
        } catch {
            case ex: CorruptIndexException => {
                logger.error(ex.getMessage)
                throw new IndexingException(ExceptionConstant.INDEX_MESSAGE)
            }
            case ex: IOException => {
                logger.error(ex.getMessage)
                throw new IndexingException(ExceptionConstant.INDEX_CODE_IO, ExceptionConstant.INDEX_MESSAGE_IO)
            }
        }
    }

    override def searchAll(indexDir: File): Unit = {
        try {

            logger.info("Search class is " + this.getClass().getSimpleName())
            //获取搜索器
            val searcher = IndexerImpl.openSearcher(indexDir)
            //匹配所有文档的查询。前100条数据
            val result: TopDocs = searcher.search(new MatchAllDocsQuery(), 100)
            //查询的命中总数。
            logger.info("Found {} results ", result.totalHits)
            logger.info("Found {} scoreDocs ", result.scoreDocs.length)
            //遍历命中文件的编号，通过搜索器查询到原文档，并输出id
            for (scoreDoc <- result.scoreDocs) {
                val doc: Document = searcher.doc(scoreDoc.doc)
                logger.info("path=>" + doc.get("id"))
            }
            close()
        } catch {
            case ex: Exception => {
                logger.error(ex.getMessage)
                new IndexingException(ExceptionConstant.INDEX_MESSAGE)
            }
        }
    }
}


object IndexerImpl {

    /**
     * 获取索引写入器
     *
     * @param indexDir
     * @return
     */
    def openWriter(indexDir: File): IndexWriter = {
        val dir = FSDirectory.open(indexDir)
        val config = new IndexWriterConfig(Version.LUCENE_47, new IKAnalyzer(true))
        new IndexWriter(dir, config)
    }

    /**
     * 获取索引搜索器
     *
     * @param indexDir
     * @return
     */
    def openSearcher(indexDir: File): IndexSearcher = {
        val dir = FSDirectory.open(indexDir)
        val directoryReader = DirectoryReader.open(dir)
        new IndexSearcher(directoryReader)
    }

    //将页面转化为文档
    def toDocument(content: PageContent): Document = {
        val doc = new Document()
        //索引三个字段
        try {
            doc.add(new Field("id", content.getPath(), Field.Store.YES, Field.Index.NOT_ANALYZED))
            doc.add(new Field("title", content.getTitle(), Field.Store.YES, Field.Index.ANALYZED))
            //不存
            doc.add(new Field("content", content.getContent(), Field.Store.NO, Field.Index.ANALYZED))
        } catch {
            case ex: Exception => {
                throw new IndexingException(ExceptionConstant.INDEX_MESSAGE)
            }
        }
        doc
    }
}