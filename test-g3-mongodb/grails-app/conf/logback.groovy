import grails.util.BuildSettings
import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

import java.nio.charset.Charset

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')

        pattern =
                '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                        '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%m%n%wex' // Message
    }
}

def loggerList = ['STDOUT']

def targetDir = BuildSettings.TARGET_DIR
if (Environment.isDevelopmentMode() && targetDir != null) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %logger - %msg%n"
        }
    }
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
    root(ERROR, ['STDOUT', 'FULL_STACKTRACE'])


    logger('grails.app.controllers', DEBUG, loggerList, false)
    logger('grails.app.services', DEBUG, loggerList, false)
    logger('grails.app.jobs', DEBUG, loggerList, false)
    logger('grails.app.domain', DEBUG, loggerList, false)
    logger('grails.app.taglibs', DEBUG, loggerList, false)
    logger('grails.app.init.test.g3.mongodb.BootStrap', DEBUG, loggerList, false)

    logger('grails.plugin.mongogee', DEBUG, loggerList, false)
    logger('test.g3.mongodb', DEBUG, loggerList, false)


}
else {
    root(INFO, ['STDOUT'])
}
