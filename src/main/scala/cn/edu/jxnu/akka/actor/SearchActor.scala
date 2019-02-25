package cn.edu.jxnu.akka.actor

import akka.actor.UntypedAbstractActor
import cn.edu.jxnu.akka.actor.message.SEARCH_MESSAGE
import cn.edu.jxnu.akka.api.impl.{Executor, IndexerImpl}
import org.slf4j.LoggerFactory

/**
 * @author 梦境迷离
 * @time 2019-02-25
 */
class SearchActor extends UntypedAbstractActor {

    private val logger = LoggerFactory.getLogger(classOf[SearchActor])

    override def onReceive(message: Any) = {

        logger.info("indexing search,actor is:" + self)
        logger.info("SearchActor当前消息类型：" + message.getClass.getSimpleName)
        message match {
            //收到索引提交消息就查询出数据
            case _: SEARCH_MESSAGE => {
                val imp = new IndexerImpl()
                imp.searchAll(Executor.indexDir)
            }
            //其他消息
            case _ => {
                this.unhandled(message)
            }
        }

    }

}
