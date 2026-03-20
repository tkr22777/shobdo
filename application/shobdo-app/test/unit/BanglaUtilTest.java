package unit;

import org.junit.Test;
import utilities.BanglaUtil;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BanglaUtilTest {

    // ── Clean Bengali spellings — must NOT be flagged ─────────────────────────

    @Test
    public void pureBengaliWord_notFlagged() {
        assertFalse(BanglaUtil.containsForeignScript("শব্দ"));
    }

    @Test
    public void bengaliWithHyphen_notFlagged() {
        assertFalse(BanglaUtil.containsForeignScript("আড়-কাঠি"));
    }

    @Test
    public void bengaliWithSpace_notFlagged() {
        assertFalse(BanglaUtil.containsForeignScript("বেঁকে বসা"));
    }

    @Test
    public void bengaliWithCommaAndDot_notFlagged() {
        assertFalse(BanglaUtil.containsForeignScript("ক, খ, গ"));
    }

    @Test
    public void bengaliWithDigits_notFlagged() {
        assertFalse(BanglaUtil.containsForeignScript("24খানা"));
    }

    @Test
    public void bengaliWithLatinAbbreviation_notFlagged() {
        assertFalse(BanglaUtil.containsForeignScript("UV রশ্মি"));
    }

    @Test
    public void bengaliWithParentheses_notFlagged() {
        assertFalse(BanglaUtil.containsForeignScript("অজ্ঞ (স্ত্রীলিঙ্গ)"));
    }

    @Test
    public void nullInput_notFlagged() {
        assertFalse(BanglaUtil.containsForeignScript(null));
    }

    @Test
    public void emptyString_notFlagged() {
        assertFalse(BanglaUtil.containsForeignScript(""));
    }

    // ── Foreign script characters — must BE flagged ───────────────────────────

    @Test
    public void devanagariMixedWithBengali_flagged() {
        // কৃমिज — Bengali prefix with Devanagari suffix
        assertTrue(BanglaUtil.containsForeignScript("কৃমिज"));
    }

    @Test
    public void pureDevanagari_flagged() {
        assertTrue(BanglaUtil.containsForeignScript("नमस्ते"));
    }

    @Test
    public void devanagariVowelMark_flagged() {
        // vowel mark ि (U+093F) mixed into a Bengali word
        assertTrue(BanglaUtil.containsForeignScript("সिলাई"));
    }

    @Test
    public void arabicMixedWithBengali_flagged() {
        assertTrue(BanglaUtil.containsForeignScript("জاسوس"));
    }

    @Test
    public void pureArabic_flagged() {
        assertTrue(BanglaUtil.containsForeignScript("مرحبا"));
    }

    @Test
    public void kannadaMixedWithBengali_flagged() {
        assertTrue(BanglaUtil.containsForeignScript("অসಿদ্ধ"));
    }

    @Test
    public void gurmukhiMixedWithBengali_flagged() {
        assertTrue(BanglaUtil.containsForeignScript("সਿੱਖ"));
    }

    @Test
    public void cyrillicCharacter_flagged() {
        assertTrue(BanglaUtil.containsForeignScript("বাংলাД"));
    }

    @Test
    public void greekCharacter_flagged() {
        assertTrue(BanglaUtil.containsForeignScript("বাংলাΩ"));
    }
}
