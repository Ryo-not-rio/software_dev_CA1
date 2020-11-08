package src;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CardGameTest {
    private final MockUserInputs validMockInputs = new MockUserInputs(4, "4players_p1win.txt");
    private CardGame cardGame;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        cardGame = new CardGame(validMockInputs);
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    public void testGetNumPlayers()
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method getNumPlayers = CardGame.class.getDeclaredMethod("getNumPlayers", new Class[] {UserInputsInterface.class, boolean.class});
        getNumPlayers.setAccessible(true);
    
        assertEquals(4, getNumPlayers.invoke(cardGame, new Object[] {validMockInputs, true}));
        assertEquals(-2147483648, getNumPlayers.invoke(cardGame, 
                                                       new Object[] {new MockUserInputs(-2147483648, "_"), true}));
        assertEquals(2147483647, getNumPlayers.invoke(cardGame,
                                                      new Object[] {new MockUserInputs(2147483647, "_"), true}));
        getNumPlayers.invoke(cardGame, new Object[] {new MockUserWithException(), true});
        assertEquals("Please input a valid player number", outContent.toString().trim());
    }

    @Test
    public void testGetPackFromFile() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        
        Method getPackFromFile = CardGame.class.getDeclaredMethod("getPackFromFile", new Class[]{UserInputsInterface.class, boolean.class});
        getPackFromFile.setAccessible(true);
        ArrayList<Card> expectedPack = new ArrayList<Card>();
        for (int i=0; i<8; i++) {
            for (int j=1; j<=4; j++) {
                expectedPack.add(new Card(j));
            }
        }

        ArrayList<Card> actualPack = (ArrayList<Card>) getPackFromFile.invoke(cardGame, new Object[] {validMockInputs, true});
        for (int i=0; i<expectedPack.size(); i++) {
            assertTrue(expectedPack.get(i).isSame(actualPack.get(i)));
        }

        getPackFromFile.invoke(cardGame, new Object[] {new MockUserInputs(4, "3players.txt"), true});
        assertEquals("Please input a valid file", outContent.toString().trim());
        outContent.reset();
        getPackFromFile.invoke(cardGame, new Object[] {new MockUserInputs(4, "Obama's-last-name.txt"), true});
        assertEquals("Please input a valid file", outContent.toString().trim());
        outContent.reset();
        getPackFromFile.invoke(cardGame, new Object[] {new MockUserInputs(4, "4players_invalid.txt"), true});
        assertEquals("Please input a valid file", outContent.toString().trim());
    }

    @Test
    public void testReadFile() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        
        Method readFile = CardGame.class.getDeclaredMethod("readFile", new Class[]{String.class});
        readFile.setAccessible(true);

        ArrayList<Card> expectedPack = new ArrayList<Card>();
        for (int i=0; i<8; i++) {
            for (int j=1; j<=4; j++) {
                expectedPack.add(new Card(j));
            }
        }
        // Valid inputs
        ArrayList<Card> actualPack = (ArrayList<Card>) readFile.invoke(cardGame, validMockInputs.getFileName());
        for (int i=0; i<expectedPack.size(); i++) {
            assertTrue(expectedPack.get(i).isSame(actualPack.get(i)));
        }

        // Testing for exceptions
        Exception exception = assertThrows(InvocationTargetException.class, 
                                           () -> readFile.invoke(cardGame, "file-not-found.txt"));
        assertEquals(FileNotFoundException.class, exception.getCause().getClass());
        
        exception = assertThrows(InvocationTargetException.class, 
                                           () -> readFile.invoke(cardGame, "4players_invalid.txt"));
        assertEquals(NumberFormatException.class, exception.getCause().getClass());
    }

    @Test
    public void testValidatePackLength() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        Method validatePackLength = CardGame.class.getDeclaredMethod("validatePackLength", new Class[]{ArrayList.class});
        validatePackLength.setAccessible(true);
        ArrayList<Card> validPack = new ArrayList<Card>();
        for (int i=0; i<32; i++) validPack.add(new Card(i));
        ArrayList<Card> invalidPack = new ArrayList<Card>();
        for (int i=0; i<79; i++) invalidPack.add(new Card(i));

        validatePackLength.invoke(cardGame, validPack);

        Exception exception = assertThrows(InvocationTargetException.class, 
                                           () -> validatePackLength.invoke(cardGame, invalidPack));
        assertEquals(InvalidLengthException.class, exception.getCause().getClass());    
    }

    /**
     * generateRing not tested as impossible to test without writing the same code
     * as in the actual function.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */

    @Test
    public void testDealCards() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        Method dealCards = CardGame.class.getDeclaredMethod("dealCards");
        dealCards.setAccessible(true);
        dealCards.invoke(cardGame);

        Field gameRingField = CardGame.class.getDeclaredField("gameRing");
        gameRingField.setAccessible(true);
        ArrayList<GameObject> gameRing = (ArrayList<GameObject>) gameRingField.get(cardGame);
        Field number = GameObject.class.getDeclaredField("num");
        number.setAccessible(true);
        for (GameObject obj: gameRing) {
            int num = (int) number.get(obj);
            for (Card c: obj.getCards()) {
                assertEquals(num, c.getValue());
            }
        }
    }

    @Test
    public void testMakeSingleMove()
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException,
            SecurityException, NoSuchMethodException {
        Method makeSingleMove = CardGame.class.getDeclaredMethod("makeSingleMove", Player.class);
        makeSingleMove.setAccessible(true);

        CardGame testCardGame = new CardGame(new MockUserInputs(4, "test4players.txt"));

        Method dealCards = CardGame.class.getDeclaredMethod("dealCards");
        dealCards.setAccessible(true);
        dealCards.invoke(testCardGame);

        Field gameRingField = CardGame.class.getDeclaredField("gameRing");
        gameRingField.setAccessible(true);
        ArrayList<GameObject> gameRing = (ArrayList<GameObject>) gameRingField.get(testCardGame);
        
        makeSingleMove.invoke(testCardGame, (Player) gameRing.get(1));

        Deck expectedLeft = new Deck(1);
        expectedLeft.addCard(new Card(1));
        expectedLeft.addCard(new Card(1));
        expectedLeft.addCard(new Card(1));

        Player expectedPlayer = new Player(1);
        expectedPlayer.addCard(new Card(1));
        expectedPlayer.addCard(new Card(1));
        expectedPlayer.addCard(new Card(1));
        expectedPlayer.addCard(new Card(1));

        Deck expectedRight = new Deck(2);
        expectedRight.addCard(new Card(3));
        expectedRight.addCard(new Card(3));
        expectedRight.addCard(new Card(3));
        expectedRight.addCard(new Card(3));
        expectedRight.addCard(new Card(3));
        
        assertTrue(String.format("Left Deck failed. Expected %s but got %s", expectedLeft.cardsToStringList().toString(), gameRing.get(0).cardsToStringList().toString()),
                   gameRing.get(0).isSame(expectedLeft));
        assertTrue(String.format("Player failed. Expected %s but got %s", expectedPlayer.cardsToStringList().toString(), gameRing.get(1).cardsToStringList().toString()),
                   gameRing.get(1).isSame(expectedPlayer));
        assertTrue(String.format("Right deck failed. Expected %s but got %s", expectedRight.cardsToStringList().toString(), gameRing.get(2).cardsToStringList().toString()),
                   gameRing.get(2).isSame(expectedRight));

        ArrayList<GameObject> gameRing2 = (ArrayList<GameObject>) gameRingField.get(cardGame);
        dealCards.invoke(cardGame);
        makeSingleMove.invoke(cardGame, (Player) gameRing2.get(1));
        Field winner = CardGame.class.getDeclaredField("winner");
        winner.setAccessible(true);
        assertEquals(1, winner.get(cardGame));
    }

}
