package com.mistraltech.smog.examples.simple;

import com.mistraltech.smog.examples.model.Address;
import com.mistraltech.smog.examples.model.Person;
import com.mistraltech.smog.examples.model.Phone;
import com.mistraltech.smog.examples.model.PostCode;
import com.mistraltech.smog.examples.simple.matcher.PhoneMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;

import java.util.regex.Pattern;

import static com.mistraltech.smog.examples.simple.matcher.MatcherFactory.aPersonThat;
import static com.mistraltech.smog.examples.simple.matcher.MatcherFactory.aPhoneThat;
import static com.mistraltech.smog.examples.simple.matcher.MatcherFactory.aPostCodeThat;
import static com.mistraltech.smog.examples.simple.matcher.MatcherFactory.anAddressThat;
import static com.mistraltech.smog.examples.simple.matcher.MatcherFactory.anAddresseeThat;
import static com.mistraltech.smog.examples.utils.MatcherTestUtils.assertDescription;
import static com.mistraltech.smog.examples.utils.MatcherTestUtils.assertMismatch;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SimpleMatcherExamplesTest {

    @Test
    public void testSimpleMatcherSucceedsWhenMatches() {
        Matcher<Person> matcher = is(aPersonThat().hasName("bob"));

        Person input = new Person("bob", 36, new Address(21, new PostCode("out", "in")));

        assertDescription(matcher, "is a Person that (has name ('bob'))");
        assertThat(input, matcher);
    }

    @Test
    public void testSimpleMatcherFailsWhenMismatches() {
        Matcher<Person> matcher = is(aPersonThat().hasName("bob"));

        Person input = new Person("dennis", 36, new Address(21, new PostCode("out", "in")));

        assertMismatch(input, matcher, "name was 'dennis' (expected 'bob')");
    }

    @Test
    public void testSimpleMatcherFailsWhenMatchingWrongType() {
        Matcher matcher = is(anAddresseeThat());

        assertFalse(matcher.matches("a string"));
    }

    @Test
    public void testNestedMatcherSucceedsWhenMatches() {
        Matcher<Person> matcher = is(aPersonThat()
                .hasName("bob")
                .hasAddress(anAddressThat()
                        .hasHouseNumber(21)));

        Person input = new Person("bob", 36, new Address(21, new PostCode("out", "in")));

        assertDescription(matcher, "is a Person that (has name ('bob') and has address (an Address that (has houseNumber (<21>))))");
        assertThat(input, matcher);
    }

    @Test
    public void testNestedMatcherFailsWhenMismatches() {
        Matcher<Person> matcher = is(aPersonThat()
                .hasName("bob")
                .hasAddress(anAddressThat()
                        .hasHouseNumber(99)));

        Person input = new Person("bob", 36, new Address(21, new PostCode("out", "in")));

        assertMismatch(input, matcher, "address.houseNumber was <21> (expected <99>)");
    }

    @Test
    public void testDeepCompositeCustomMatcher() {
        Matcher<Person> matcher =
                is(aPersonThat()
                        .hasName("bob")
                        .hasAddress(anAddressThat()
                                .hasHouseNumber(21)
                                .hasPostCode(aPostCodeThat()
                                        .hasOuter(containsString("y")))));

        Person input = new Person("bob", 36, new Address(21, new PostCode("out", "in")));

        assertDescription(matcher, "is a Person that (has name ('bob') and has address " +
                "(an Address that (has houseNumber (<21>) and has postCode " +
                "(a Postcode that (has outer (a string containing 'y'))))))");

        assertMismatch(input, matcher, "address.postCode.outer was 'out' (expected a string containing 'y')");
    }

    @Test
    public void testDeepCompositeCustomMatcherWithManyMismatches() {
        Matcher<Person> matcher =
                is(aPersonThat()
                        .hasName("obo")
                        .hasAddress(anAddressThat()
                                .hasHouseNumber(21)
                                .hasPostCode(aPostCodeThat()
                                        .hasInner(startsWith("x"))
                                        .hasOuter(containsString("y")))));

        Person input = new Person("bob", 36, new Address(22, new PostCode("out", "in")));

        String descriptionOfMismatch = "name was 'bob' (expected 'obo')\n" +
                "     and: address.houseNumber was <22> (expected <21>)\n" +
                "     and: address.postCode.inner was 'in' (expected a string starting with 'x')\n" +
                "     and: address.postCode.outer was 'out' (expected a string containing 'y')";

        assertMismatch(input, matcher, descriptionOfMismatch);
    }

    @Test
    public void testDeepCompositeCustomMatcherWithNull() {
        Matcher<Person> matcher =
                is(aPersonThat()
                        .hasName("obo")
                        .hasAddress(anAddressThat()
                                .hasHouseNumber(21)
                                .hasPostCode(aPostCodeThat()
                                        .hasInner("x"))));

        Person input = new Person("bob", 36, null);

        String descriptionOfMismatch = "name was 'bob' (expected 'obo')\n" +
                "     and: address was null";

        assertMismatch(input, matcher, descriptionOfMismatch);
    }

    @Test
    public void testEmptyListMatching() {
        Matcher<Person> matcher =
                is(aPersonThat()
                        .hasPhoneList(IsEmptyCollection.<Phone>empty()));

        Person input = new Person("bob", 36, null, new Phone("123", "456456"), new Phone("123", "123123"));

        Pattern mismatchDescriptionPattern = Pattern.compile("phoneList .* \\(expected an empty collection\\)");

        assertMismatch(input, matcher, mismatchDescriptionPattern);
    }

    @Test
    public void testListSizeMatching() {
        Matcher<Person> matcher =
                is(aPersonThat()
                        .hasPhoneList(IsCollectionWithSize.<Phone>hasSize(1)));

        Person input = new Person("bob", 36, null, new Phone("123", "456456"), new Phone("123", "123123"));

        String descriptionOfMismatch = "phoneList collection size was <2> (expected a collection with size <1>)";

        assertMismatch(input, matcher, descriptionOfMismatch);
    }

    @Test
    public void testListContainsMatching() {
        Matcher<Person> matcher =
                is(aPersonThat()
                        .hasPhoneList(IsIterableContainingInOrder.contains(
                                new PhoneMatcher[]{
                                        aPhoneThat().hasCode("123").hasNumber("456456"),
                                        aPhoneThat().hasCode("123").hasNumber("123321")
                                }
                        )));

        Person input = new Person("bob", 36, null, new Phone("123", "456456"), new Phone("123", "123123"));

        String descriptionOfMismatch = "phoneList item 1: number was '123123' (expected '123321') " +
                "(expected iterable containing [" +
                "a Phone that (has code ('123') and has number ('456456')), " +
                "a Phone that (has code ('123') and has number ('123321'))" +
                "])";

        assertMismatch(input, matcher, descriptionOfMismatch);
    }

    @Test
    public void testCallingMatcherDirectly() {
        Person bob = new Person("Bob", 35, null);

        Matcher<Person> youngPerson = aPersonThat().hasAge(35);
        Matcher<Person> oldPerson = aPersonThat().hasAge(greaterThanOrEqualTo(36));

        assertTrue(youngPerson.matches(bob));
        assertFalse(oldPerson.matches(bob));
    }

    @Test
    public void testAnnotatedMatcherMethods() {
        Person bob = new Person("Bob", 35, null);

        Matcher<Person> aYoungPerson = aPersonThat().havingYearsOld(35);
        Matcher<Person> anOldPerson = aPersonThat().havingYearsOld(greaterThanOrEqualTo(36));

        assertThat(bob, is(aYoungPerson));
        assertThat(bob, is(not(anOldPerson)));
    }
}
