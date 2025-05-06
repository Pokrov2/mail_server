package test;

import java.util.HashSet;
import mailserver.Filter.*;
import mailserver.Model.Message;
import mailserver.Model.User;
import mailserver.Storage.UserStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MailServerTests {

    private UserStorage storage;
    private User alice;
    private User bob;

    @BeforeEach
    public void setUp() {
        storage = new UserStorage();
        alice = new User("alice");
        bob = new User("bob");
        storage.addUser(alice);
        storage.addUser(bob);
    }

    @Test
    public void testSendMessageToInbox() {
        alice.sendMessage(bob, "Hello", "Hi Bob, how are you?");

        List<Message> inbox = bob.getInbox();
        List<Message> outbox = alice.getOutbox();

        assertEquals(1, inbox.size());
        assertEquals(1, outbox.size());
        assertEquals("alice", inbox.get(0).getSender());
        assertEquals("bob", outbox.get(0).getReceiver());
    }

    @Test
    public void testSimpleSpamFilter() {
        bob.setSpamFilter(new SimpleSpamFilter());
        alice.sendMessage(bob, "spam offer", "This is a spam message");

        assertEquals(0, bob.getInbox().size());
        assertEquals(1, bob.getSpam().size());
    }

    @Test
    public void testKeywordsSpamFilter() {
        bob.setSpamFilter(new KeywordsSpamFilter(Arrays.asList("buy", "cheap")));
        alice.sendMessage(bob, "promo", "You should buy new TV now!");

        assertEquals(1, bob.getSpam().size());
        assertEquals(0, bob.getInbox().size());
    }

    @Test
    public void testRepetitionsSpamFilter() {
        bob.setSpamFilter(new RepetitionsSpamFilter(2));
        alice.sendMessage(bob, "hello", "win win win big prizes");

        assertEquals(1, bob.getSpam().size());
    }

    @Test
    public void testSenderSpamFilter() {
        bob.setSpamFilter(new SenderSpamFilter(new HashSet<>(List.of("alice"))));
        alice.sendMessage(bob, "test", "should be spam");

        assertEquals(1, bob.getSpam().size());
    }


    @Test
    public void testCompositeSpamFilter() {
        SpamFilter composite = new CompositeSpamFilter(Arrays.asList(
                new SimpleSpamFilter(),
                new KeywordsSpamFilter(List.of("lottery"))
        ));
        bob.setSpamFilter(composite);
        alice.sendMessage(bob, "win", "You won the lottery!");

        assertEquals(1, bob.getSpam().size());
    }

    @Test
    public void testUserStorage() {
        assertTrue(storage.userExists("alice"));
        assertNotNull(storage.getUser("bob"));
    }
}
