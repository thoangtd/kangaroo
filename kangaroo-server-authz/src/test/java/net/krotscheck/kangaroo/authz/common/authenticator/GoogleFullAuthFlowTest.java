/*
 * Copyright (c) 2017 Michael Krotscheck
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.krotscheck.kangaroo.authz.common.authenticator;

import net.krotscheck.kangaroo.authz.common.database.entity.OAuthToken;
import net.krotscheck.kangaroo.authz.common.database.entity.OAuthTokenType;
import net.krotscheck.kangaroo.common.hibernate.id.IdUtil;
import net.krotscheck.kangaroo.test.TestConfig;
import net.krotscheck.kangaroo.test.runner.SingleInstanceTestRunner;
import net.krotscheck.kangaroo.util.HttpUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.math.BigInteger;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.or;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.urlContains;

/**
 * This test performs a series of full login flows against google.
 *
 * @author Michael Krotscheck
 */
@RunWith(SingleInstanceTestRunner.class)
public final class GoogleFullAuthFlowTest
        extends AbstractBrowserLoginTest {

    /**
     * Reset the facebook account before every test.
     */
    @Before
    public void googleLogin() {
        WebDriver d = SELENIUM.getDriver();

        By emailInput = By.id("identifierId");
        By nextButton = By.id("identifierNext");
        By passInput = By.cssSelector("div#password input[type='password']");
        By passNext = By.id("passwordNext");

        // Login to google.
        d.get("https://accounts.google.com/ServiceLogin");
        SELENIUM.screenshot();
        new WebDriverWait(d, 10)
                .until(elementToBeClickable(emailInput));

        // Enter the login
        d.findElement(emailInput).clear();
        d.findElement(emailInput).sendKeys(TestConfig.getGoogleAccountId());
        d.findElement(nextButton).click();
        SELENIUM.screenshot();
        new WebDriverWait(d, TIMEOUT)
                .until(elementToBeClickable(passInput));

        // Enter the password
        d.findElement(passInput).clear();
        d.findElement(passInput).sendKeys(TestConfig.getGoogleAccountSecret());
        d.findElement(passNext).click();

        // Test for different conditions here.
        SELENIUM.screenshot();
        new WebDriverWait(d, TIMEOUT)
                .until(or(
                        urlContains("https://myaccount.google.com"),
                        urlContains("https://accounts.google.com/"
                                + "signin/oauth/oauthchooseaccount"),
                        urlContains("https://accounts.google.com/"
                                + "signin/v2/challenge/selection")
                ));

        // Test for the sign-in challenge
        if (d.getCurrentUrl().contains("https://accounts.google.com/"
                + "signin/v2/challenge/selection")) {
            SELENIUM.screenshot();
            d.findElement(By.cssSelector("[data-challengetype=16]")).click();

            new WebDriverWait(d, TIMEOUT)
                    .until(presenceOfElementLocated(
                            By.id("knowledgeLoginLocationInput")
                    ))
                    .sendKeys("Bellevue, WA");

            d.findElement(By.id("next")).click();

            new WebDriverWait(d, TIMEOUT)
                    .until(or(
                            urlContains("https://myaccount.google.com")
                    ));
        }
    }

    /**
     * Reset the google account before every test.
     */
    @After
    public void googleLogout() {
        WebDriver d = SELENIUM.getDriver();
        d.get("https://accounts.google.com/Logout");
        SELENIUM.screenshot();
        new WebDriverWait(d, TIMEOUT)
                .until(urlContains("https://accounts.google.com/"));
        SELENIUM.screenshot();
    }

    /**
     * Someone new to your app logs in with Google.
     */
    @Test
    public void testNewLogin() {
        String testState = RandomStringUtils.randomAlphanumeric(20);

        // Issue a request against our /authorize endpoint.
        URI requestUri = UriBuilder.fromUri(getBaseUri())
                .path("/authorize")
                .queryParam("authenticator", AuthenticatorType.Google)
                .queryParam("response_type", "code")
                .queryParam("client_id",
                        IdUtil.toString(getContext().getClient().getId()))
                .queryParam("scope", "debug")
                .queryParam("state", testState)
                .build();

        WebDriver d = SELENIUM.getDriver();
        d.get(requestUri.toString());
        SELENIUM.screenshot();
        (new WebDriverWait(d, TIMEOUT))
                .until(or(
                        urlContains("https://accounts.google.com/"
                                + "signin/oauth/oauthchooseaccount"),
                        elementToBeClickable(By.id("submit_approve_access"))
                ));
        SELENIUM.screenshot();

        // Test for the account chooser.
        if (d.getCurrentUrl().contains("https://accounts.google"
                + ".com/signin/oauth/oauthchooseaccount")) {
            d.findElement(By.cssSelector("[data-profileindex=\"0\"]"))
                    .click();
            SELENIUM.screenshot();
        } else {
            d.findElement(By.id("submit_approve_access")).click();
            SELENIUM.screenshot();
        }

        (new WebDriverWait(d, TIMEOUT))
                .until(urlContains("www.example.com"));
        SELENIUM.screenshot();

        String url = d.getCurrentUrl();
        URI uri = URI.create(url);
        MultivaluedMap<String, String> params = HttpUtil.parseQueryParams(uri);
        BigInteger code = IdUtil.fromString(params.getFirst("code"));
        assertEquals(testState, params.getFirst("state"));

        OAuthToken authToken = getSession().get(OAuthToken.class, code);
        assertEquals(authToken.getClient().getId(),
                getContext().getClient().getId());
        assertEquals(AuthenticatorType.Google,
                authToken.getIdentity().getType());
        assertEquals(OAuthTokenType.Authorization,
                authToken.getTokenType());
    }
}
