package net.hidearea.core.exception;

/**
 * リソース未検出例外
 *
 * リクエストされたリソースが見つからない場合にスローします。
 */
public class ResourceNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String resourceType;
    private final Object resourceId;

    public ResourceNotFoundException(ErrorCode errorCode, String resourceType, Object resourceId) {
        super(String.format("%s not found: %s", resourceType, resourceId));
        this.errorCode = errorCode;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.resourceType = null;
        this.resourceId = null;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Object getResourceId() {
        return resourceId;
    }
}
