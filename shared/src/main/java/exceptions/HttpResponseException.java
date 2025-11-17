package exceptions;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class HttpResponseException extends RuntimeException {

    private final int statusCode;
    private final String statusMessage;

    public HttpResponseException(int statusCode, String statusMessage) {

        super(String.format("HTTP error: %d %s", statusCode, statusMessage));
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}
