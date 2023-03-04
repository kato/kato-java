package me.danwi.kato.common;

import me.danwi.kato.common.exception.ExceptionExtraDataHolder;
import me.danwi.kato.common.exception.ExceptionIdentify;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * kato调用异常结果表达
 */
public class ExceptionResult {

    public static final String HEADER_KEY = "Kato-Exception";

    private final String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    private String exception;
    private String message;
    private String trace;
    private String path;
    private Map<String, Object> data;

    public static ExceptionResult fromException(Throwable throwable) {
        final ExceptionResult result = new ExceptionResult();
        result.setException(throwable.getClass().getName());
        result.setMessage(throwable.getMessage());
        result.setTrace(getThrowableStackTrace(throwable));
        return result;
    }

    public Exception toException(Class<?> rootClass, String methodKey) {
        try {
            //加载异常类
            Class<?> exceptionClass = Class.forName(this.getException());
            //判断其是否能被定位
            if (exceptionClass.getAnnotation(ExceptionIdentify.class) != null) {
                //实例化异常
                Exception exceptionInstance = null;
                //以message构造
                try {
                    exceptionInstance = (Exception) exceptionClass.getConstructor(String.class).newInstance(this.getMessage());
                } catch (Exception ignored) {
                }
                //以无参构造
                if (exceptionInstance == null)
                    try {
                        exceptionInstance = (Exception) exceptionClass.getConstructor().newInstance();
                    } catch (Exception ignored) {
                    }
                //构造异常实例失败
                if (exceptionInstance == null)
                    return null;
                //如果附带的异常数据
                if (exceptionInstance instanceof ExceptionExtraDataHolder) {
                    //加载附带的数据
                    ((ExceptionExtraDataHolder) exceptionInstance).loadFromMap(this.getData());
                }
                // 补充堆栈
                final List<StackTraceElement> stackTraceElements = Arrays.stream(exceptionInstance.getStackTrace()).collect(Collectors.toList());
                stackTraceElements.add(0, new StackTraceElement(rootClass.getName(), methodKey, rootClass.getSimpleName(), 0));
                exceptionInstance.setStackTrace(stackTraceElements.toArray(new StackTraceElement[0]));
                //返回异常
                return exceptionInstance;
            }
        } catch (Exception ignored) {
            //构造异常的过程中发生异常,返回null
        }
        return null;
    }

    private static String getThrowableStackTrace(Throwable throwable) {
        StringWriter stackTrace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTrace));
        stackTrace.flush();
        return stackTrace.toString();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTrace() {
        return trace;
    }

    public void setTrace(String trace) {
        this.trace = trace;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ExceptionResult that))
            return false;
        return getTimestamp().equals(that.getTimestamp()) && Objects.equals(getException(), that.getException()) && Objects.equals(getMessage(), that.getMessage()) && Objects.equals(getTrace(), that.getTrace()) && Objects.equals(getPath(), that.getPath()) && Objects.equals(getData(), that.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTimestamp(), getException(), getMessage(), getTrace(), getPath(), getData());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExceptionResult{");
        sb.append("timestamp='").append(timestamp).append('\'');
        sb.append(", exception='").append(exception).append('\'');
        sb.append(", message='").append(message).append('\'');
        sb.append(", trace='").append(trace).append('\'');
        sb.append(", path='").append(path).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}


