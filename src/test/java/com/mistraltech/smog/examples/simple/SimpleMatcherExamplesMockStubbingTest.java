package com.mistraltech.smog.examples.simple;

import com.mistraltech.smog.examples.model.Address;
import com.mistraltech.smog.examples.model.Person;
import com.mistraltech.smog.examples.model.PostCode;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.mistraltech.smog.examples.simple.matcher.MatcherFactory.aPersonThat;
import static com.mistraltech.smog.examples.simple.matcher.MatcherFactory.aPostCodeThat;
import static com.mistraltech.smog.examples.simple.matcher.MatcherFactory.anAddressThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SimpleMatcherExamplesMockStubbingTest {
    @Test
    public void testMock() {
        Converter converter = mock(Converter.class);

        Person person = new Person("Brian", 26, new Address(73, new PostCode("out", "in")));

        when(converter.personToName(argThat(is(aPersonThat()
                .hasName(startsWith("B")).hasAddress(anAddressThat()
                        .hasPostCode(aPostCodeThat().hasInner("in"))))))).thenReturn("Billie");

        // The mock expectation should match and returns "Billie"

        assertThat(converter.personToName(person), equalTo("Billie"));
    }

    @Test
    public void testMockFailure() {
        Converter converter = mock(Converter.class);

        Person person = new Person("Brian", 26, new Address(73, new PostCode("out", "in")));

        when(converter.personToName(argThat(is(aPersonThat()
                        .hasName(startsWith("B")).hasAddress(anAddressThat()
                                        .hasHouseNumber(37)
                                        .hasPostCode(aPostCodeThat().hasInner("out"))
                        )
        )))).thenReturn("Billie");

        // The mock expectation should not match so personToName returns null

        assertThat(converter.personToName(person), Matchers.nullValue());
    }

    private class Converter {
        public String personToName(Person person) {
            return "Bertie";
        }
    }

}
