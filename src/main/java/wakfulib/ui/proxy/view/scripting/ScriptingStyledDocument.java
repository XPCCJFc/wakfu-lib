package wakfulib.ui.proxy.view.scripting;

import java.awt.Color;
import java.util.Set;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScriptingStyledDocument extends DefaultStyledDocument {

    private final AttributeSet attrKeyWords;
    private final AttributeSet attrDefault;

    private final Set<String> keywords;
    private final String docEndOfLine;

    public ScriptingStyledDocument(Set<String> keywords) {
        this.keywords = keywords;
        StyleContext cont = StyleContext.getDefaultStyleContext();
        var simpleAttributeSet = new SimpleAttributeSet();
        StyleConstants.setBold(simpleAttributeSet, true);
        attrKeyWords = cont.addAttribute(simpleAttributeSet, StyleConstants.Foreground, new Color(204, 120, 50));
        attrDefault = StyleContext.getDefaultStyleContext().addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.BLACK);
        var docEndOfLine = (String) getDocumentProperties().get("__EndOfLine__");
        if (docEndOfLine == null) {
            docEndOfLine = "\n";
            getDocumentProperties().put("__EndOfLine__", docEndOfLine);
        }
        this.docEndOfLine = docEndOfLine;
    }

    private int findLastNonWordChar(String text, int index) {
        while (--index >= 0) {
            char c = text.charAt(index);
            if (Character.isSpaceChar(c) || c == '\n') {
                break;
            }
        }
        return index;
    }

    private int findFirstNonWordChar(String text, int index) {
        while (index < text.length()) {
            char c = text.charAt(index);
            if (Character.isSpaceChar(c) || c == '\n') {
                break;
            }
            index++;
        }
        return index;
    }

    @Override
    public void insertString (int offset, String str, AttributeSet a) throws BadLocationException {
        str = str.replace(System.lineSeparator(), docEndOfLine);
        super.insertString(offset, str, a);

        String text = getText(0, getLength());
        int before = findLastNonWordChar(text, offset) + 1;
        int after = findFirstNonWordChar(text, offset + str.length());
        int wordR = before;

        while (wordR < after) {
            char currentChar = text.charAt(wordR);
            boolean isSpace = Character.isSpaceChar(currentChar) || currentChar == '\n';
            if (! isSpace) {
                var noWorld = findFirstNonWordChar(text, wordR);
                String token = text.substring(wordR, noWorld);
                if (keywords.contains(token.toUpperCase())) {
//                    log.info("Painting from {} to {} ['{}'] in color ", wordR, wordR + token.length(), text.substring(wordR, wordR + token.length()));
                    setCharacterAttributes(wordR, token.length(), attrKeyWords, true);
                } else if (token.length() > 0) {
//                    log.info("Painting from {} to {} ['{}'] in default ", wordR, wordR + token.length(), text.substring(wordR, wordR + token.length()));
                    setCharacterAttributes(wordR, token.length(), attrDefault, true);
                }
                wordR = noWorld;
            } else {
                wordR++;
            }
        }
    }

    @Override
    public void remove (int offs, int len) throws BadLocationException {
        super.remove(offs, len);

        String text = getText(0, getLength());
        int before = findLastNonWordChar(text, offs) + 1;
        if (before < 0) before = 0;
        int after = findFirstNonWordChar(text, offs);
        
        if (keywords.contains(text.substring(before, after).toUpperCase())) {
            setCharacterAttributes(before, after - before, attrKeyWords, true);
        } else {
            setCharacterAttributes(before, after - before, attrDefault, true);
        }
    }
}
