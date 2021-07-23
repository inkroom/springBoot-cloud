package cn.inkroom.study.cloud.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.reactive.result.view.ViewResolver;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author inkbox
 * @date 2021/7/22
 */
@Component
@Order(-1)//这里必须要，否则无法生效
public class JsonErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public JsonErrorWebExceptionHandler(ServerCodecConfigurer serverCodecConfigurer, ErrorAttributes errorAttributes, ResourceProperties resourceProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, applicationContext);
        super.setMessageWriters(serverCodecConfigurer.getWriters());
        logger.info("错误处理器");
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), new HandlerFunction<ServerResponse>() {
            @Override
            public Mono<ServerResponse> handle(ServerRequest request) {
                logger.info("错误加载2 error={}", errorAttributes.getErrorAttributes(request, true));
                Map<String, Object> errorAttributes1 = getErrorAttributes(request, false);
                return ServerResponse.status(getHttpStatus(errorAttributes1))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .syncBody("{\"id\":\"contract\",\"predicates\":[{\"name\":\"Path\",\"args\":{\"patterns\":\"/**\"}}],\"filters\":[{\"name\":\"PreserveHostHeader\",\"args\":{}}],\"uri\":\"lb://contract\",\"order\":0}");
            }
        });
    }

    /**
     * Get the HTTP error status information from the error map.
     *
     * @param errorAttributes the current error information
     * @return the error HTTP status
     */
    protected HttpStatus getHttpStatus(Map<String, Object> errorAttributes) {
        int statusCode = (int) errorAttributes.get("status");
        return HttpStatus.valueOf(statusCode);
    }
}
