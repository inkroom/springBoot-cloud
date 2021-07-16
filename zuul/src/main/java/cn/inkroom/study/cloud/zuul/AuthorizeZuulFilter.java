package cn.inkroom.study.cloud.zuul;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;


/**
 * @author inkbox
 * @date 2021/7/15
 */
@Component
public class AuthorizeZuulFilter extends ZuulFilter {
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 15;
    }

    @Override
    public boolean shouldFilter() {

        System.out.println("should");
        RequestContext currentContext = RequestContext.getCurrentContext();
        if (currentContext.getRequestQueryParams() == null) return false;
        return (currentContext.getRequestQueryParams().containsKey("code"));

    }

    @Override
    public Object run() throws ZuulException {

        RequestContext currentContext = RequestContext.getCurrentContext();

        String code = currentContext.getRequest().getParameter("code");

        System.out.println("filter");
        if ("123".equals(code)) {
            return null;
        }

        currentContext.setSendZuulResponse(false);
        currentContext.setResponseStatusCode(401);
        currentContext.getResponse().setCharacterEncoding("utf-8");
        currentContext.getResponse().setContentType("text/plain;charset=utf-8");

        currentContext.setResponseBody("code不正确");

        return null;
    }
}
