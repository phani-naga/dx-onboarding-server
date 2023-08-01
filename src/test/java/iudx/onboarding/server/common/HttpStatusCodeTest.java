package iudx.onboarding.server.common;

import iudx.onboarding.server.common.HttpStatusCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpStatusCodeTest {
  @Test
  public void testSomeMethod() {
    HttpStatusCode mockedStatusCode = mock(HttpStatusCode.class);
    when(mockedStatusCode.getValue()).thenReturn(200);
    int value = 200;
    HttpStatusCode actualStatusCode = HttpStatusCode.getByValue(value);
    assertEquals(HttpStatusCode.SUCCESS, actualStatusCode);
  }
}
