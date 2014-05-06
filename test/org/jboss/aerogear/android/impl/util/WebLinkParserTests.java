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

import org.robolectric.RobolectricTestRunner;
import java.util.List;
import org.jboss.aerogear.android.impl.pipeline.paging.WebLink;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RobolectricTestRunner.class)
public class WebLinkParserTests {

    @Test
    public void testStandard() throws ParseException {
        final String testString = "</TheBook/chapter2>;" +
                "rel=\"previous\"; title*=UTF-8'de'letztes%20Kapitel,\n" +
                "</TheBook/chapter4>;" +
                "rel=\"next\"; title*=UTF-8'de'n%c3%a4chstes%20Kapitel";

        List<WebLink> result = WebLinkParser.parse(testString);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("/TheBook/chapter4", result.get(1).getUri());
        Assert.assertEquals(2, result.get(0).getParameters().size());
        Assert.assertEquals("next", result.get(1).getParameters().get("rel"));
    }
}
