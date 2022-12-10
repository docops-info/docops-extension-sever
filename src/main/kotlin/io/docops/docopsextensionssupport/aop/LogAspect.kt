package io.docops.docopsextensionssupport.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class LogAspect {
    var logger: Logger = LoggerFactory.getLogger(LogAspect::class.java)

    @Suppress("TooGenericExceptionCaught")
    @Around("@within(LogExecution) && execution(public !static * *(..))")
    fun logExecutionTime(joinPoint: ProceedingJoinPoint): Any? {
        val start = System.currentTimeMillis()
        val signature = joinPoint.signature.toShortString()
        val result = try {
            with(StringBuilder("start -> Executing $signature, ")) {
                appendParameters(joinPoint.args)
                logger.info(toString())
            }
            joinPoint.proceed()
        } catch (throwable: Throwable) {
            logger.error("*** Exception during executing $signature,", throwable)
            throw throwable
        }
        val duration = System.currentTimeMillis() - start
        logger.info("end -> Finished executing: $signature, duration: $duration ms")
        return result
    }

    private fun StringBuilder.appendParameters(args: Array<Any>) {
        if(logger.isDebugEnabled) {
            append("parameters: [")
            args.forEachIndexed { i, p ->
                append(p.javaClass.simpleName).append("(").append(p.toString()).append(")")
                if (i < args.size - 1) append(", ")
            }
            append("]")
        }
    }
}