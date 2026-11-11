package jetbrains.maxkir.zd8956030;

import com.intellij.openapi.diagnostic.Logger;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jetbrains.buildServer.controllers.interceptors.RequestInterceptors;
import jetbrains.buildServer.controllers.interceptors.SkippableInterceptor;
import jetbrains.buildServer.web.util.WebUtil;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.MediaType;

public final class JsonXmlMismatchInterceptor extends SkippableInterceptor implements InitializingBean, DisposableBean {
  // Inherit TeamCity's server-file appender.
  private static final Logger LOG = Logger.getInstance("jetbrains.buildServer.zd8956030");
  private static final String REQUEST_DETAILS = JsonXmlMismatchInterceptor.class.getName() + ".requestDetails";
  private final RequestInterceptors myChain;

    public JsonXmlMismatchInterceptor(RequestInterceptors chain) {
        myChain = chain;
    }

    @Override
  public synchronized void afterPropertiesSet() {
    LOG.info("ZD8956030 diagnostics enabled");
    myChain.addInterceptor(this);
  }

  @Override
  public synchronized void destroy() {
    LOG.info("ZD8956030 diagnostics disabled");
    myChain.removeInterceptor(this);
  }

  @Override
  protected boolean preHandleInternal(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (request.getAttribute(REQUEST_DETAILS) == null) {
      List<String> accept = Collections.list(request.getHeaders("Accept"));
      if (acceptsJson(accept)) {
        request.setAttribute(REQUEST_DETAILS, new RequestDetails(String.join(", ", accept), WebUtil.getRequestDump(request)));
      }
    }
    return true;
  }

  @Override
  protected void afterCompletionInternal(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    if (!(request.getAttribute(REQUEST_DETAILS) instanceof RequestDetails details)) return;

    request.removeAttribute(REQUEST_DETAILS);
    if (!isXml(response.getContentType())) return;

    LOG.warn("ZD8956030 JSON_XML_MISMATCH: Accept=" + details.acceptHeader() +
             ", Content-Type=" + response.getContentType() + ", status=" + response.getStatus() +
             ", X-Request-ID=" + response.getHeader("X-Request-ID") +
             ", original request: " + details.dump() + ", completed request: " + WebUtil.getRequestDump(request),
            ex);
  }

  private static boolean acceptsJson(List<String> headers) {
    for (String header : headers) {
      if (header != null && header.startsWith("application/json")) {
        return true;
      }
    }
    return false;
  }

  private static boolean isXml(String contentType) {
    if (contentType == null) return false;
    return contentType.contains("xml");
  }

  private record RequestDetails(String acceptHeader, String dump) {}
}
