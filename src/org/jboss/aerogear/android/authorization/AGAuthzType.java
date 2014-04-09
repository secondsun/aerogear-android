package org.jboss.aerogear.android.authorization;

public enum AGAuthzType implements AuthzType {
    
    OAUTH2("OAuth2");

    private final String typeDescription;

    AGAuthzType(String typeDescription) {
        this.typeDescription = typeDescription;
    }

    @Override
    public String getName() {
        return this.typeDescription;
    }
}
