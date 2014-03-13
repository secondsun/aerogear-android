package org.jboss.aerogear.android.impl.authz;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Date;
import java.util.Objects;
import org.jboss.aerogear.android.RecordId;

public class OAUTH2AuthzSession implements Parcelable {

    @RecordId
    private String accountId = "";
    
    private String cliendId = "";
    private String accessToken = "";
    private String authorizationCode = "";
    private String refreshToken = "";
    private long expires_on = 0;
    
    private OAUTH2AuthzSession(Parcel in) {
        cliendId = in.readString();
        accessToken = in.readString();
        authorizationCode = in.readString();
        refreshToken = in.readString();
        accountId = in.readString();
        expires_on = in.readLong();
    }
    
    public OAUTH2AuthzSession() {
    }

    public String getCliendId() {
        return cliendId;
    }

    public void setCliendId(String cliendId) {
        this.cliendId = cliendId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpires_on() {
        return expires_on;
    }

    public void setExpires_on(long expires_on) {
        this.expires_on = expires_on;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.cliendId);
        hash = 71 * hash + Objects.hashCode(this.accessToken);
        hash = 71 * hash + Objects.hashCode(this.authorizationCode);
        hash = 71 * hash + Objects.hashCode(this.refreshToken);
        hash = 71 * hash + Objects.hashCode(this.accountId);
        hash = 71 * hash + (int) (this.expires_on ^ (this.expires_on >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OAUTH2AuthzSession other = (OAUTH2AuthzSession) obj;
        if (!Objects.equals(this.cliendId, other.cliendId)) {
            return false;
        }
        if (!Objects.equals(this.accessToken, other.accessToken)) {
            return false;
        }
        if (!Objects.equals(this.authorizationCode, other.authorizationCode)) {
            return false;
        }
        if (!Objects.equals(this.refreshToken, other.refreshToken)) {
            return false;
        }
        if (!Objects.equals(this.accountId, other.accountId)) {
            return false;
        }
        if (this.expires_on != other.expires_on) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AuthzSession{" + "cliendId=" + cliendId + ", accessToken=" + accessToken + ", authorizationCode=" + authorizationCode + ", refreshToken=" + refreshToken + ", accountId=" + accountId + ", expires_on=" + expires_on + '}';
    }
    
    

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cliendId);
        dest.writeString(accessToken);
        dest.writeString(authorizationCode);
        dest.writeString(refreshToken);
        dest.writeString(accountId);
        dest.writeLong(expires_on);
    }

    public static final Parcelable.Creator<OAUTH2AuthzSession> CREATOR
            = new Parcelable.Creator<OAUTH2AuthzSession>() {
                @Override
                public OAUTH2AuthzSession createFromParcel(Parcel in) {
                    return new OAUTH2AuthzSession(in);
                }

                @Override
                public OAUTH2AuthzSession[] newArray(int size) {
                    return new OAUTH2AuthzSession[size];
                }
                
            };

    public boolean tokenIsNotExpired() {
        if (expires_on == 0) {
            return true;
        }
        return (expires_on > new Date().getTime());
    }

}
