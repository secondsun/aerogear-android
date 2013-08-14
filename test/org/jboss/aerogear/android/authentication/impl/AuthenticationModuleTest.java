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
package org.jboss.aerogear.android.authentication.impl;

import java.util.concurrent.CountDownLatch;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.http.HeaderAndBody;

/**
 * This interface sets up all of the static values for
 * {@link AGSecurityAuthenticationModuleTest}
 */
public interface AuthenticationModuleTest {

    static final String PASSING_USERNAME = "spittman";
    static final String FAILING_USERNAME = "fail";
    static final String LOGIN_PASSWORD = "password";
    static final String ENROLL_PASSWORD = "spittman";

    final class SimpleCallback implements Callback<HeaderAndBody> {

        HeaderAndBody data;
        Exception exception;
        CountDownLatch latch;

        public SimpleCallback() {
        }

        public SimpleCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(HeaderAndBody data) {
            this.data = data;
            countdown();
        }

        @Override
        public void onFailure(Exception e) {
            this.exception = e;
            countdown();
        }

        private void countdown() {
            if (latch != null) {
                latch.countDown();
            }
        }
    }

    final class VoidCallback implements Callback<Void> {

        Exception exception;
        CountDownLatch latch;

        public VoidCallback() {
        }

        public VoidCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(Void data) {
            countDown();
        }

        @Override
        public void onFailure(Exception e) {
            this.exception = e;
            countDown();
        }

        private void countDown() {
            if (latch != null) {
                latch.countDown();
            }
        }
    }
}
