/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.impl.util;

import org.junit.Test;

import java.net.URL;

import static org.jboss.aerogear.android.impl.util.UrlUtils.appendToBaseURL;
import static org.junit.Assert.assertEquals;

public class UrlUtilsTest {

    @Test
    public void testBothHaveSlash() throws Exception {
        URL baseURL = new URL("http://fakeurl.com/");
        String endpoint = "/endpoint";
        URL expectURL = new URL("http://fakeurl.com/endpoint");

        assertEquals(expectURL, appendToBaseURL(baseURL, endpoint));
    }

    @Test
    public void testOnlyBaseURLHasSlash() throws Exception {
        URL baseURL = new URL("http://fakeurl.com/");
        String endpoint = "endpoint";
        URL expectURL = new URL("http://fakeurl.com/endpoint");

        assertEquals(expectURL, appendToBaseURL(baseURL, endpoint));
    }

    @Test
    public void testOnlyEndpointHasSlash() throws Exception {
        URL baseURL = new URL("http://fakeurl.com");
        String endpoint = "/endpoint";
        URL expectURL = new URL("http://fakeurl.com/endpoint");

        assertEquals(expectURL, appendToBaseURL(baseURL, endpoint));
    }

    @Test
    public void testBothWithoutSlash() throws Exception {
        URL baseURL = new URL("http://fakeurl.com");
        String endpoint = "endpoint";
        URL expectURL = new URL("http://fakeurl.com/endpoint");

        assertEquals(expectURL, appendToBaseURL(baseURL, endpoint));
    }

}
