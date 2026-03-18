package wakfu.sniff.logic;

import lombok.Getter;
import wakfulib.exception.AuthenticationException;
import wakfulib.exception.CharacterNotFoundException;
import wakfulib.exception.ServerVersionMismatch;
import wakfulib.exception.WorldNotFoundException;
import wakfulib.internal.Inject;
import wakfulib.internal.Version;
import wakfulib.internal.registration.VersionRegistry;
import wakfulib.logic.Session;
import wakfulib.logic.event.annotation.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class BasicWorldConnector {
    public static int PORT = 5568;
    public static int PROX_PORT = 5569;

    //mock
    public record Character() {
        public String getName() {
            return "";
        }
    }

    @Inject @Getter
    Session session;
    private final String token;
    @Getter
    private Character selected;
    private final AtomicBoolean choosing = new AtomicBoolean(false);
    private List<Character> characters;
    private final Function<List<Character>, Character> characterChooser;

    public BasicWorldConnector(String token, String characterName) {
        this(token,  (m) -> {
            Optional<Character> character = m.stream().filter(p -> p.getName().equalsIgnoreCase(characterName)).findFirst();
            if (character.isPresent()) {
                return character.get();
            } else {
                try {
                    throw new CharacterNotFoundException(characterName);
                } catch (CharacterNotFoundException e) {
                    e.printStackTrace();
                    System.exit(-1);
                    return null;
                }
            }
        });
    }

    public BasicWorldConnector(String token, Function<List<Character>, Character> characterChooser) {
        this.token = token;
        this.characterChooser = characterChooser;
    }

    public BasicWorldConnector(String token) {
        this(token, "");
    }

//    @EventHandler
//    public void onConnection(ClientIpMessage result) {
//        ClientVersionRequestMessage message = VersionRegistry.messageInstance(ClientVersionRequestMessage.class);
//        message.setVersion(Version.getCurrent());
//        session.send(message);
//        ClientPublicKeyRequestMessage message2 = VersionRegistry.messageInstance(ClientPublicKeyRequestMessage.class);
//        message2.archTarget = 1;
//        session.send(message2);
//    }
//
//    @EventHandler
//    public void onVersion(ClientVersionResultMessage resultMessage) throws ServerVersionMismatch {
//        if(!resultMessage.isMatch()) {
//            throw new ServerVersionMismatch(Version.getCurrent(), resultMessage.getVersion());
//        }
//    }
//
//    @EventHandler
//    public void onKey(ClientPublicKeyResultMessage resultMessage) {
//        ClientAuthenticationTokenMessage message = VersionRegistry.messageInstance(ClientAuthenticationTokenMessage.class);
//        message.setToken(token);
//        session.send(message);
//    }
//
//    @EventHandler
//    public void onWorldConfirm(WorldSelectionResultMessage resultMessage) throws WorldNotFoundException {
//        if(resultMessage.getErrorCode() != 0) {
//            throw new WorldNotFoundException(resultMessage.getErrorCode());
//        }
//    }
//
//    @EventHandler
//    public void onConfirm(ClientAuthenticationResultsMessage resultsMessage) throws AuthenticationException {
//        if (resultsMessage.getResultCode() != 0) {
//            throw new AuthenticationException(LoginError.getErrorFromId(resultsMessage.getResultCode()).name());
//        }
//    }
//
//    @EventHandler
//    public void onCharacter(CharactersListMessage charactersListMessage) {
//        characters = new ArrayList<>(charactersListMessage.getCharacters());
//
//        assert characters.size() > 0;
//
//        if (selected == null && ! choosing.get()) {
//            choosing.set(true);
//            selected = chooseCharacter(characters);
//            CharacterSelectionRequestMessage res = VersionRegistry.messageInstance(CharacterSelectionRequestMessage.class);
//            res.setCharName(selected.getName());
//            res.setCharId(selected.getUuid());
//            session.send(res);
//            choosing.set(false);
//        }
//    }

    /**
     * You can override this method to implement your own character selection logic as
     * it choose the first character in the list in this simple implementations
     * @param characters all the characters of this account
     * @return the selected character.
     */
    protected Character chooseCharacter(List<Character> characters) {
        return characterChooser.apply(characters);
    }

//    @EventHandler
//    public void charInfo(CharacterInformationMessage message) {
//        selected.mergeParts(message.getCharacterParts());
//    }

}
