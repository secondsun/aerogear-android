package org.jboss.aerogear.android.impl.authz;

public class AuthorizationException extends Exception {

    public enum Error {INVALID_REQUEST, INVALID_CLIENT, INVALID_GRANT, UNAUTHORIZED_CLIENT, UNSUPPORTED_GRANT_TYPE, INVALID_SCOPE, OTHER;
        
        public static Error getErrorEnum(String inError) {
            for (Error error : values()) {
                if (error.name().equalsIgnoreCase(inError)) {
                    return error;
                }
            }
            return OTHER;
        }
    
    };
    
    public final String error;
    public final Error type;
    
    public AuthorizationException(String error) {
        super(error);
        this.error = error;
        type = Error.getErrorEnum(error);
    }

    public String getError() {
        return error;
    }

    public Error getType() {
        return type;
    }

    @Override
    public String toString() {
        return "AuthorizationException{" + "error=" + error + '}';
    }
    
    
    
}
