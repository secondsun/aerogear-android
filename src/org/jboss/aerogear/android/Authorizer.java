/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and
 * individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jboss.aerogear.android;

import java.util.HashMap;
import java.util.Map;
import org.jboss.aerogear.android.authorization.AuthzModule;
import org.jboss.aerogear.android.impl.authz.AuthzConfig;
import org.jboss.aerogear.android.impl.authz.AuthzTypes;
import org.jboss.aerogear.android.impl.authz.oauth2.OAuth2AuthzModule;

public class Authorizer {

    private final Map<String, AuthzModule> modules = new HashMap<String, AuthzModule>();

    /**
     *
     * authz will construct an AuthzModule for the config Object if the config
     * is a supported type. Currently only {@link AuthzTypes#OAUTH2}
     *
     * @param config a configuration object for the module
     * @return a configured module.
     * 
     * @throws IllegalArgumentException if the config object is not a supported type.
     */
    public AuthzModule authz(AuthzConfig config) {

        if (AuthzTypes.OAUTH2.equals(config.getType())) {
            modules.put(config.getName(), new OAuth2AuthzModule(config));
        } else {
            throw new IllegalArgumentException(config.getType().getName() + " is not supported");
        }

        return modules.get(config.getName());

    }

    /**
     * Removes a module from the Authorizer
     *
     * @param moduleName  the name of the module
     * @return the removed module
     */
    public AuthzModule remove(String moduleName) {
        return modules.remove(moduleName);
    }

    /**
     * Look up for a authzModule object.
     *
     * @param moduleName the name of the actual authzModule
     * @return the current authzModule or null
     */
    public AuthzModule get(String moduleName) {
        return modules.get(moduleName);
    }

}
