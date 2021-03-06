package cn.edu.jxnu.akka.example

import cn.edu.jxnu.akka.api.impl.HtmlParserPageRetriever
import cn.edu.jxnu.akka.common.ExceptionConstant
import cn.edu.jxnu.akka.entity.PageContent
import cn.edu.jxnu.akka.exception.RetrievalException

/**
 * 随机
 */
@deprecated
class ChaosMonkeyPageRetriever(baseUrl: String) extends HtmlParserPageRetriever(baseUrl) {

    override def fetchPageContent(url: String): PageContent = {
        if (System.currentTimeMillis % 20 == 0) throw new RetrievalException(ExceptionConstant.ETRIEVAL_CODE_MONKEY,
            ExceptionConstant.ETRIEVAL_MESSAGE_MONKEY)
        super.fetchPageContent(url)
    }
}
